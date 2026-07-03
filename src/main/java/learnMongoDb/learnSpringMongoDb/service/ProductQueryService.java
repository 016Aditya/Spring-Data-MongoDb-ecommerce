package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.config.SynonymConfig;
import learnMongoDb.learnSpringMongoDb.dto.ProductSuggestionDto;
import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;
    private final SynonymConfig synonymConfig; // ── Injected Synonym Config ──

    private static final int MAX_QUERY_LENGTH = 100;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_CANDIDATE_POOL = 300; // Limit DB fetch before Java ranking

    // ─── 1. FULL PAGE SEARCH (Criteria Match + Java Relevance Ranking) ───
    public Page<Product> search(String keyword, int page, int size) {
        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, safeSize);

        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }

        String trimmed = keyword.trim();
        if (trimmed.length() > MAX_QUERY_LENGTH) {
            throw new IllegalArgumentException("Search query too long");
        }

        String[] tokens = trimmed.split("\\s+");

        // 1. Build the exact Boolean AND-of-ORs Criteria
        List<Criteria> tokenSlotCriteria = Arrays.stream(tokens)
                .map(token -> buildSlotCriteria(token, false)) // false = general 'contains' regex
                .toList();

        Criteria finalCriteria = tokenSlotCriteria.size() == 1
                ? tokenSlotCriteria.get(0)
                : new Criteria().andOperator(tokenSlotCriteria);

        // 2. Fetch the Candidate Pool (Limit to 300 to protect memory)
        Query query = Query.query(finalCriteria).limit(MAX_CANDIDATE_POOL);
        List<Product> candidates = mongoTemplate.find(query, Product.class);

        // 3. Lightweight Relevance Ranking (Scores & Sorts in Memory)
        candidates.sort((p1, p2) -> {
            int score1 = calculateRelevance(p1, tokens);
            int score2 = calculateRelevance(p2, tokens);
            return Integer.compare(score2, score1); // Descending order (highest score first)
        });

        // 4. Manual Pagination of the sorted list
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), candidates.size());

        List<Product> paginatedList = (start <= end)
                ? candidates.subList(start, end)
                : List.of();

        return new PageImpl<>(paginatedList, pageable, candidates.size());
    }

    // ─── 2. SUGGESTIONS DROPDOWN (Prefix Regex Matching limit 6) ───
    public List<ProductSuggestionDto> suggestions(String q) {
        if (q == null || q.trim().length() < 2) {
            return List.of();
        }

        String trimmed = q.trim();
        if (trimmed.length() > MAX_QUERY_LENGTH) {
            return List.of();
        }

        String[] tokens = trimmed.split("\\s+");
        List<Criteria> tokenSlotCriteria = Arrays.stream(tokens)
                .map(token -> buildSlotCriteria(token, true)) // true = prefix/word-boundary regex
                .toList();

        Criteria finalCriteria = tokenSlotCriteria.size() == 1
                ? tokenSlotCriteria.get(0)
                : new Criteria().andOperator(tokenSlotCriteria);

        Query query = Query.query(finalCriteria);
        query.fields().include("id", "name", "category", "imageUrl");
        query.limit(6); // Capped at 6 for snappy UI speed

        List<Product> rawProducts = mongoTemplate.find(query, Product.class);

        return rawProducts.stream()
                .map(p -> new ProductSuggestionDto(p.getId(), p.getName(), p.getCategory(), p.getImageUrl()))
                .collect(Collectors.toList());
    }

    // ─── HELPER: Build Criteria per Token ───
    private Criteria buildSlotCriteria(String token, boolean isPrefix) {
        Set<String> expandedTerms = synonymConfig.expand(token);

        List<Criteria> fieldMatches = new ArrayList<>();

        for (String term : expandedTerms) {
            String safe = Pattern.quote(term);

            // If it's a suggestion typeahead, we want it to match the START of a word.
            // e.g. "iph" matches "iPhone" but not "microphone".
            // (^|\s) ensures the match happens at the beginning of the field or after a space.
            String regex = isPrefix ? "(^|\\s)" + safe : safe;

            fieldMatches.add(Criteria.where("name").regex(regex, "i"));
            fieldMatches.add(Criteria.where("brand").regex(regex, "i"));
            fieldMatches.add(Criteria.where("category").regex(regex, "i"));
            fieldMatches.add(Criteria.where("subcategory").regex(regex, "i"));
        }

        return new Criteria().orOperator(fieldMatches);
    }

    // ─── HELPER: Custom Relevance Engine ───
    private int calculateRelevance(Product product, String[] tokens) {
        int score = 0;
        String name = product.getName() != null ? product.getName().toLowerCase() : "";
        String brand = product.getBrand() != null ? product.getBrand().toLowerCase() : "";
        String cat = product.getCategory() != null ? product.getCategory().toLowerCase() : "";
        String subCat = product.getSubcategory() != null ? product.getSubcategory().toLowerCase() : "";

        // 1. Text Matching Weights
        for (String token : tokens) {
            String t = token.toLowerCase();

            // Name matches carry the highest weight
            if (name.startsWith(t) || name.contains(" " + t)) score += 100;
            else if (name.contains(t)) score += 80;

            // Brand & Category matches
            if (brand.equalsIgnoreCase(t)) score += 50;
            else if (brand.startsWith(t)) score += 40;

            if (cat.equalsIgnoreCase(t)) score += 30;
            if (subCat.equalsIgnoreCase(t)) score += 20;
        }

        // 2. Merchandising Boost
        if (product.isFeatured()) {
            score += 15;
        }

        // 3. User Review Signals
        if (product.getAverageRating() != null) {
            score += (int) (product.getAverageRating() * 2);
        }

        // 4. Stock Availability Signals (Conversion Boost & OOS Penalty)
        if (product.getStock() > 0) {
            score += 5;
            if (product.getStock() <= 3) {
                score -= 5; // Negates the stock boost if inventory is critically low
            }
        } else {
            score -= 20; // Heavy penalty drops out-of-stock items straight to the bottom
        }

        return score;
    }
}