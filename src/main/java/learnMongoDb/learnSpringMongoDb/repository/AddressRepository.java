package learnMongoDb.learnSpringMongoDb.repository;

import learnMongoDb.learnSpringMongoDb.entity.Address;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends MongoRepository<Address, String> {

    /** All addresses belonging to a user, newest first (Spring Data derived query). */
    List<Address> findByUserIdOrderByCreatedAtDesc(String userId);

    /** The current default address for a user, if any. */
    Optional<Address> findByUserIdAndDefaultAddressTrue(String userId);

    /** Ownership check — used before update / delete. */
    Optional<Address> findByIdAndUserId(String id, String userId);

    /** Count addresses per user — may be used to enforce a cap in future. */
    long countByUserId(String userId);
}