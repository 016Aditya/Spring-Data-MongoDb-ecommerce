package learnMongoDb.learnSpringMongoDb.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation. Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.ConnectionBuilder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@Document(collection = "orders")
@CompoundIndex(name = "idx_quantity_status", def = "{'quantity':-1,'status':1}")
@CompoundIndex(name = "idx_address_city", def = "{'address.city': -1}")

public class Order {

    @Id
    private String id;

    @Indexed
    private String userId;

    private Integer quantity;
    private Double totalPrice;

    @Indexed
    private String status;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Address address;

    @DBRef
    private List<Product> products;

}