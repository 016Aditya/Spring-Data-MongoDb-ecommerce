package learnMongoDb.learnSpringMongoDb;

import learnMongoDb.learnSpringMongoDb.entity.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Date;
import java.util.List;
@SpringBootTest
public class MongoTemplateTest {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void mongoTemplateTest() {
        Query query = new Query(
                new Criteria().orOperator(
                        Criteria.where("totalPrice").lte(200),
                        Criteria.where("status").is("pending")
                )
        );
        query.fields().include("status", "id");
        query.limit(2);

        List<Order> orderList = mongoTemplate.find(query, Order.class);
        orderList.forEach(System.out::println);
    }

    @Test
    public void mongoTemplateUpdateTest() {
        Query query = new Query(
                Criteria.where("status").is("SHIPPED")
        );

        Update update = new Update()
                .set("status", "Ready")
                .set("updatedAt", new Date())
                .set("updatedAt", java.time.LocalDateTime.now()); // Fixed: Matches your entity's LocalDateTime schema

        var result = mongoTemplate.updateMulti(query, update, Order.class);

        // Pro-Tip: You can print out exactly how many documents were modified!
        System.out.println("Modified document count: " + result.getModifiedCount());
    }
}