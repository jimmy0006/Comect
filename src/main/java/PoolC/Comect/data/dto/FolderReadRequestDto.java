package PoolC.Comect.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class FolderReadRequestDto {

    private String userEmail;
    private String path;

    @Builder
    public FolderReadRequestDto(String userEmail, String path){
        this.userEmail=userEmail;
        this.path=path;
    }
}
