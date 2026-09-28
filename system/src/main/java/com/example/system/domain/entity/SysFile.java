package com.example.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
public class SysFile extends BaseEntity {

    @TableId(type = IdType.AUTO)
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

    private Integer uploadStatus;

    private String businessType;

    @TableField(exist = false)
    private String createBy;

    @TableField(exist = false)
    private String updateBy;

    @TableField(exist = false)
    private String remark;
}
