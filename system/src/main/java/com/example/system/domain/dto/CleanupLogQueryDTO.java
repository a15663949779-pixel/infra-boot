package com.example.system.domain.dto;

import lombok.Data;

@Data
public class CleanupLogQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String cleanupType;

    private String cleanupMode;

    private Integer status;
}
