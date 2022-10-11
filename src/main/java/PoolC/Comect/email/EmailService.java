package PoolC.Comect.email;

import PoolC.Comect.common.exception.CustomException;
import PoolC.Comect.common.exception.ErrorCode;
import PoolC.Comect.user.domain.User;
import PoolC.Comect.user.repository.UserRepository;
import PoolC.Comect.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailService {
    private final EmailRepository emailRepository;
    private final UserRepository userRepository;
    private final RegisterMail registerMail;

    public void emailSend(String email) {
        validateDuplicateUser(email);

        emailRepository.findByEmail(email).ifPresent((emailAuth)->emailRepository.delete(emailAuth));
        Email emailAuth=new Email(email);
        registerMail.sendSimpleMessage(email,emailAuth.getId().toHexString());
        emailRepository.save(emailAuth);
    }

    public Email findOneEmail(String email){
        return emailRepository.findByEmail(email).orElseThrow(()->new CustomException(ErrorCode.EMAIL_NOT_FOUND));
    }

    public Email findOneId(String id){
        return emailRepository.findById(new ObjectId(id)).orElseThrow(()->new CustomException(ErrorCode.EMAIL_NOT_FOUND));
    }

    public void emailCheck(String id){
        Email emailAuth = findOneId(id);
        User user = userRepository.findByEmail(emailAuth.getEmail()).orElseThrow(() -> {
            throw new CustomException(ErrorCode.EMAIL_AUTH_FAILED);
        });
        emailAuthCheck(emailAuth.getEmail());
        user.setRoles(Collections.singletonList("ROLE_AU"));
        userRepository.save(user);
        emailRepository.delete(emailAuth);
    }

    public void emailAuthCheck(String email){
        Email emailAuth = findOneEmail(email);
        if(!emailAuth.isValid()){
            throw new CustomException(ErrorCode.EMAIL_AUTH_FAILED);
        }
    }

    public void validateDuplicateUser(String email){
        if(!userRepository.findByEmail(email).isEmpty()){
            throw new CustomException(ErrorCode.EMAIL_EXISTS);
        }
    }
}