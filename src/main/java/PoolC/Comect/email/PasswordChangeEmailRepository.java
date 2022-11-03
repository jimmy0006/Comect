
package PoolC.Comect.email;

import PoolC.Comect.user.domain.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PasswordChangeEmailRepository extends MongoRepository<PasswordChangeEmail,ObjectId>{
    @Override
    public Optional<PasswordChangeEmail> findById(ObjectId objectId);

    public Optional<PasswordChangeEmail> findByEmail(String email);

}