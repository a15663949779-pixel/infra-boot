package com.example.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.system.domain.dto.CleanupLogQueryDTO;
import com.example.system.domain.vo.SysFileCleanupLogVO;

public interface FileCleanupService {

    /**
     * 方案2：清理上传超时的文件（upload_status=0 且超过阈值时间）
     */
    void cleanupUploadingTimeout(String mode);

    /**
     * 方案1：磁盘扫描孤儿文件（磁盘有文件但数据库无对应记录）
     */
    void cleanupOrphanFiles(String mode);

    /**
     * 分页查询清理日志
     */
    Page<SysFileCleanupLogVO> listCleanupLogs(CleanupLogQueryDTO queryDTO);
}
