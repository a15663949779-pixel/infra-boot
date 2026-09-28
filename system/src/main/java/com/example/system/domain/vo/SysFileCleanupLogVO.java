package com.example.system.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysFileCleanupLogVO {

    private Long logId;

    private String cleanupType;

    private String cleanupTypeDesc;

    private String cleanupMode;

    private String cleanupModeDesc;

    private Integer totalScanned;

    private Integer totalCleaned;

    private Integer totalFailed;

    private String cleanedDetail;

    private Long executionTime;

    private Integer status;

    private String statusDesc;

    private String errorMessage;

    private LocalDateTime createTime;
}
