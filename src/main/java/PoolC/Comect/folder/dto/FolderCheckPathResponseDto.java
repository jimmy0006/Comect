package PoolC.Comect.folder.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class FolderCheckPathResponseDto {

    private int valid;

    @Builder
    public FolderCheckPathResponseDto(int valid){
        this.valid=valid;
    }
}