package PoolC.Comect.email;

import PoolC.Comect.user.domain.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EmailRepository extends MongoRepository<Email, ObjectId> {
    @Override
    public Optional<Email> findById(ObjectId objectId);

    public Optional<Email> findByEmail(String email);
}
