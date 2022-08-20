package PoolC.Comect.data.service;

import PoolC.Comect.data.domain.Link;
import PoolC.Comect.data.repository.DataRepository;
import PoolC.Comect.data.domain.Folder;
import PoolC.Comect.user.domain.User;
import PoolC.Comect.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
public class DataService {

    private final DataRepository dataRepository;
    private final UserRepository userRepository;

    @Transactional
    public void folderCreate(String userEmail, String path, String folderName) throws IllegalAccessException{
        validateEmail(userEmail);
        User user = getUserByEmail(userEmail);
        Folder folder = new Folder(folderName);
        dataRepository.folderCreate(user.getRootFolderId(), path, folder);
    }

    @Transactional
    public List<Link> folderReadLink(String userEmail, String path) throws IllegalAccessException{
        validateEmail(userEmail);
        User user = getUserByEmail(userEmail);
        List<Link> links = dataRepository.folderReadLink(user.getRootFolderId(),path);
        return links;
    }

    @Transactional
    public List<String> folderReadFolder(String userEmail, String path) throws IllegalAccessException{
        validateEmail(userEmail);
        User user = getUserByEmail(userEmail);
        List<String> folderNames=dataRepository.folderReadFolder(user.getRootFolderId(),path);
        return folderNames;
    }

    @Transactional
    public void folderUpdate(String userEmail, String path, String folderName) throws IllegalAccessException{
        validateEmail(userEmail);
        User user = getUserByEmail(userEmail);
        dataRepository.folderUpdate(user.getRootFolderId(),path,folderName);
    }

    @Transactional
    public void folderDelete(String userEmail, String path) throws IllegalAccessException{
        validateEmail(userEmail);
        User user = getUserByEmail(userEmail);
        dataRepository.folderDelete(user.getRootFolderId(),path);
    }

    @Transactional
    public void folderMove(String userEmail, String originalPath, String modifiedPath) throws IllegalAccessException{
        validateEmail(userEmail);
        User user = getUserByEmail(userEmail);
        Folder folder=dataRepository.folderRead(user.getRootFolderId(),originalPath);
        dataRepository.folderDelete(user.getRootFolderId(),originalPath);
        dataRepository.folderCreate(user.getRootFolderId(),modifiedPath,folder);
    }

    private User getUserByEmail(String userEmail) throws IllegalAccessException{
        Optional<User> user = userRepository.findByEmail(userEmail);
        return user.orElseThrow(() ->new IllegalAccessException("존재하지 않은 유저입니다"));
    }

    private void validateEmail(String userEmail){
        String regx = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Pattern pattern = Pattern.compile(regx);
        Matcher matcher = pattern.matcher(userEmail);
        if(!matcher.matches()){
            throw new IllegalStateException("형식에 맞지 않는 이메일입니다");
        }
    }

}