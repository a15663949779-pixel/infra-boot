package com.example.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysFileVO {

    private Long fileId;

    private String originalName;

    private String fileName;

    private String filePath;

    private String fileUrl;

    private String fileExtension;

    private Long fileSize;

    private String fileSizeFormat;

    private String contentType;

    private String fileMd5;

    private String storageType;

    private Integer remoteSyncStatus;

    private String businessType;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private String remark;
}
