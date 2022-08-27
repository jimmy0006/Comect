package PoolC.Comect.folder.service;

import PoolC.Comect.common.CustomException;
import PoolC.Comect.common.ErrorCode;
import PoolC.Comect.folder.domain.Link;
import PoolC.Comect.folder.domain.Folder;
import PoolC.Comect.folder.dto.FolderInfo;
import PoolC.Comect.folder.dto.LinkInfo;
import PoolC.Comect.folder.repository.FolderRepository;
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
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    public void folderCreate(String userEmail, String path, String folderName){
        validateEmail(userEmail);
        User user = getUserByEmail(userEmail);
        Folder folder = new Folder(folderName);
        folderRepository.folderCreate(user.getRootFolderId(), path, folder);
    }

    public Folder folderRead(String userEmail, String path){
        validateEmail(userEmail);
        User user = getUserByEmail(userEmail);
        Folder folder=folderRepository.folderRead(user.getRootFolderId(),path);
        return folder;
    }

    public void folderUpdate(String userEmail, String path, String folderName){
        validateEmail(userEmail);
        User user = getUserByEmail(userEmail);
        folderRepository.folderUpdate(user.getRootFolderId(),path,folderName);
    }

    @Transactional
    public void folderDelete(String userEmail, List<String> paths){
        validateEmail(userEmail);
        User user = getUserByEmail(userEmail);
        for(String path:paths) {
            folderRepository.folderDelete(user.getRootFolderId(), path);
        }
    }

    @Transactional
    public void folderMove(String userEmail, List<String> originalPaths, String modifiedPath){
        validateEmail(userEmail);
        User user = getUserByEmail(userEmail);
        for(String originalPath:originalPaths){
            Folder folder=folderRepository.folderRead(user.getRootFolderId(),originalPath);
            folderRepository.folderDelete(user.getRootFolderId(),originalPath);
            folderRepository.folderCreate(user.getRootFolderId(),modifiedPath,folder);
        }
    }

    public boolean folderCheckPath(String userEmail, String path){
        validateEmail(userEmail);
        User user = getUserByEmail(userEmail);
        return folderRepository.checkPathFolder(user.getRootFolderId(), path);
    }

    public void linkCreate(String userEmail, String path, String name, String url, String imageUrl, List<String> keywords, String isPublic){
        validateEmail(userEmail);
        User user = getUserByEmail(userEmail);
        Link link=new Link(name,imageUrl,url,keywords,isPublic);
        folderRepository.linkCreate(user.getRootFolderId(), path, link);
    }

    public void linkUpdate(String id,String email,String path,String name,String url,String imageUrl,List<String> keywords,String isPublic){
        validateEmail(email);
        User user = getUserByEmail(email);
        Link link=new Link(name,imageUrl,url,keywords,isPublic);
        folderRepository.linkUpdate(user.getRootFolderId(), path,new ObjectId(id), link);
    }

    @Transactional
    public void linkDelete(String email,List<String> paths,List<String> ids){
        validateEmail(email);
        User user = getUserByEmail(email);
        for(int i=0;i<paths.size();++i){
            folderRepository.linkDelete(user.getRootFolderId(), paths.get(i),new ObjectId(ids.get(i)));
        }
    }

    @Transactional
    public void linkMove(String userEmail, List<String> originalPaths, List<String> originalIds,String modifiedPath){
        validateEmail(userEmail);
        User user = getUserByEmail(userEmail);
        for(int i=0;i<originalPaths.size();++i){
            String originalPath=originalPaths.get(i);
            String originalId=originalIds.get(i);
            Link link=folderRepository.linkRead(user.getRootFolderId(),originalPath,new ObjectId(originalId));
            folderRepository.linkDelete(user.getRootFolderId(),originalPath,new ObjectId(originalId));
            folderRepository.linkCreate(user.getRootFolderId(),modifiedPath,link);
        }
    }

    private User getUserByEmail(String userEmail){
        Optional<User> user = userRepository.findByEmail(userEmail);
        return user.orElseThrow(()->new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateEmail(String userEmail){
        String regx = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Pattern pattern = Pattern.compile(regx);
        Matcher matcher = pattern.matcher(userEmail);
        if(!matcher.matches()){
            throw new CustomException(ErrorCode.EMAIL_NOT_VALID);
        }
    }

}