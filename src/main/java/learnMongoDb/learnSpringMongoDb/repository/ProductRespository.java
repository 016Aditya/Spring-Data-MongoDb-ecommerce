package learnMongoDb.learnSpringMongoDb.repository;

import learnMongoDb.learnSpringMongoDb.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRespository extends MongoRepository<Product,String> {

}
