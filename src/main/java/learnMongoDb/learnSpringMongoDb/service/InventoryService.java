package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.error.ResourceNotFoundException;
import learnMongoDb.learnSpringMongoDb.exception.InventoryConflictException;
import learnMongoDb.learnSpringMongoDb.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * InventoryService — Commit 3: Production Hardening
 *
 * Sole owner of ALL inventory mutations. Replaces the Commit 2
 * read-modify-write pattern with a single atomic MongoDB
 * findAndModify operation guarded by a conditional filter.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final MongoTemplate     mongoTemplate;

    public Product atomicDecreaseStock(String productId, int quantity) {
        Query query = new Query(
                Criteria.where("_id").is(productId)
                        .and("stock").gte(quantity)
        );
        Update update = new Update().inc("stock", -quantity);
        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
        Product updated = mongoTemplate.findAndModify(query, update, options, Product.class);

        if (updated == null) {
            log.warn("Atomic stock deduction FAILED — productId={} requested={}", productId, quantity);
            throw new InventoryConflictException(productId);
        }

        updateAvailability(productId, updated.getStock());
        updated.setInStock(updated.getStock() > 0);

        log.info("Atomic stock deduction SUCCESS — productId={} -{} → stock={}",
                productId, quantity, updated.getStock());
        return updated;
    }

    public Product atomicIncreaseStock(String productId, int quantity) {
        Query query = new Query(Criteria.where("_id").is(productId));
        Update update = new Update().inc("stock", quantity);
        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
        Product updated = mongoTemplate.findAndModify(query, update, options, Product.class);

        if (updated == null) {
            throw new ResourceNotFoundException("Product not found during rollback: " + productId);
        }

        updateAvailability(productId, updated.getStock());
        updated.setInStock(updated.getStock() > 0);

        log.info("Atomic stock increase (rollback) — productId={} +{} → stock={}",
                productId, quantity, updated.getStock());
        return updated;
    }

    private void updateAvailability(String productId, int currentStock) {
        Query query = new Query(Criteria.where("_id").is(productId));
        Update update = new Update().set("inStock", currentStock > 0);
        mongoTemplate.updateFirst(query, update, Product.class);
    }

    public Map<String, Integer> atomicDecreaseStockBatch(Map<String, Integer> productQuantities) {
        Deque<Map.Entry<String, Integer>> succeeded = new ArrayDeque<>();
        Map<String, Integer> deductedAmounts = new LinkedHashMap<>();

        try {
            for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
                String productId = entry.getKey();
                int    quantity  = entry.getValue();
                atomicDecreaseStock(productId, quantity);
                succeeded.push(entry);
                deductedAmounts.put(productId, quantity);
            }
            log.info("Atomic batch deduction SUCCESS for all {} item(s)", productQuantities.size());
            return deductedAmounts;

        } catch (InventoryConflictException ex) {
            log.error("Atomic batch deduction FAILED at productId={} — rolling back {} prior success(es)",
                    ex.getProductId(), succeeded.size());
            while (!succeeded.isEmpty()) {
                Map.Entry<String, Integer> entry = succeeded.pop();
                try {
                    atomicIncreaseStock(entry.getKey(), entry.getValue());
                } catch (Exception rollbackEx) {
                    log.error("CRITICAL: rollback failed for productId={} amount={} — {}",
                            entry.getKey(), entry.getValue(), rollbackEx.getMessage());
                }
            }
            throw ex;
        }
    }

    public void rollbackBatch(Map<String, Integer> deductedAmounts) {
        for (Map.Entry<String, Integer> entry : deductedAmounts.entrySet()) {
            try {
                atomicIncreaseStock(entry.getKey(), entry.getValue());
            } catch (Exception ex) {
                log.error("CRITICAL: post-order-failure rollback failed for productId={} — {}",
                        entry.getKey(), ex.getMessage());
            }
        }
    }
}