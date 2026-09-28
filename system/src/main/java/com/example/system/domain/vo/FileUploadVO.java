package com.example.system.domain.vo;

import lombok.Data;

@Data
public class FileUploadVO {

    private Long fileId;

    private String originalName;

    private String fileUrl;

    private String fileSizeFormat;

    private String fileExtension;
}
