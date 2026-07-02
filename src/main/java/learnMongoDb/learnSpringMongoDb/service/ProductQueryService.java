package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.dto.ProductSuggestionDto;
import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepository;

    public Page<Product> search(String keyword, int page, int size) {
        Pageable pg = PageRequest.of(page, size);
        // Spring's ContainingIgnoreCase automatically escapes and handles wildcard wrapping safely
        return productRepository.findByNameContainingIgnoreCase(keyword.trim(), pg);
    }

    public List<ProductSuggestionDto> suggestions(String q) {
        if (q == null || q.trim().length() < 2) {
            return List.of();
        }

        // CRITICAL: Prevent Regex Injection attacks
        String safeRegex = Pattern.quote(q.trim());

        // Hardcoded max size to 6 for snappy performance
        Pageable pg = PageRequest.of(0, 6);

        List<Product> rawProducts = productRepository.findSuggestionsByNameRegex(safeRegex, pg);

        // Map immediately to lightweight DTOs
        return rawProducts.stream()
                .map(p -> new ProductSuggestionDto(p.getId(), p.getName(), p.getCategory(), p.getImageUrl()))
                .collect(Collectors.toList());
    }
}