package com.example.system.task;

import com.example.framework.file.config.FileProperties;
import com.example.system.service.FileCleanupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "file.cleanup", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FileCleanupTask {

    private final FileCleanupService fileCleanupService;

    public FileCleanupTask(FileCleanupService fileCleanupService) {
        this.fileCleanupService = fileCleanupService;
    }

    /**
     * 方案2：每日清理上传超时文件
     */
    @Scheduled(cron = "${file.cleanup.timeout-cron:0 0 2 * * ?}")
    public void executeTimeoutCleanup() {
        log.info("定时任务开始: 上传超时文件清理");
        try {
            fileCleanupService.cleanupUploadingTimeout("AUTO");
            log.info("定时任务完成: 上传超时文件清理");
        } catch (Exception e) {
            log.error("定时任务异常: 上传超时文件清理", e);
        }
    }

    /**
     * 方案1：每月磁盘孤儿文件扫描
     */
    @Scheduled(cron = "${file.cleanup.orphan-cron:0 0 3 1 * ?}")
    public void executeOrphanScan() {
        log.info("定时任务开始: 磁盘孤儿文件扫描");
        try {
            fileCleanupService.cleanupOrphanFiles("AUTO");
            log.info("定时任务完成: 磁盘孤儿文件扫描");
        } catch (Exception e) {
            log.error("定时任务异常: 磁盘孤儿文件扫描", e);
        }
    }
}
