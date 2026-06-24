package learnMongoDb.learnSpringMongoDb.repository;

import learnMongoDb.learnSpringMongoDb.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmailAndPhoneNumber(String email, String phoneNumber);
}