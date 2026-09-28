package com.example.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_file_cleanup_log")
public class SysFileCleanupLog {

    @TableId(type = IdType.AUTO)
    private Long logId;

    private String cleanupType;

    private String cleanupMode;

    private Integer totalScanned;

    private Integer totalCleaned;

    private Integer totalFailed;

    private String cleanedDetail;

    private Long executionTime;

    private Integer status;

    private String errorMessage;

    private LocalDateTime createTime;
}
