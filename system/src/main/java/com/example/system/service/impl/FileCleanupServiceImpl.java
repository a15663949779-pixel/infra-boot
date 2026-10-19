package com.example.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.framework.file.config.FileProperties;
import com.example.framework.file.manager.FileStorageManager;
import com.example.framework.file.manager.FileLifecycleCoordinator;
import com.example.framework.security.PermissionService;
import org.springframework.security.access.AccessDeniedException;
import com.example.system.domain.dto.CleanupLogQueryDTO;
import com.example.system.domain.entity.SysFile;
import com.example.system.domain.entity.SysFileCleanupLog;
import com.example.system.domain.vo.SysFileCleanupLogVO;
import com.example.system.mapper.SysFileCleanupLogMapper;
import com.example.system.mapper.SysFileMapper;
import com.example.system.service.FileCleanupService;
import com.example.system.service.SysFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileCleanupServiceImpl implements FileCleanupService {

    private final SysFileService sysFileService;
    private final SysFileMapper sysFileMapper;
    private final SysFileCleanupLogMapper cleanupLogMapper;
    private final FileStorageManager fileStorageManager;
    private final FileProperties fileProperties;
    private final FileLifecycleCoordinator lifecycle;
    private final PermissionService permissionService;

    public FileCleanupServiceImpl(SysFileService sysFileService,
                                  SysFileMapper sysFileMapper,
                                  SysFileCleanupLogMapper cleanupLogMapper,
                                  FileStorageManager fileStorageManager,
                                  FileProperties fileProperties,
                                  FileLifecycleCoordinator lifecycle,
                                  PermissionService permissionService) {
        this.sysFileService = sysFileService;
        this.sysFileMapper = sysFileMapper;
        this.cleanupLogMapper = cleanupLogMapper;
        this.fileStorageManager = fileStorageManager;
        this.fileProperties = fileProperties;
        this.lifecycle = lifecycle;
        this.permissionService = permissionService;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void cleanupUploadingTimeout(String mode) {
        authorizeManual(mode);
        try (var task = lifecycle.tryLockCleanup()) {
            if (task == null) return;
            doCleanupUploadingTimeout(mode);
        }
    }

    private void doCleanupUploadingTimeout(String mode) {
        long startTime = System.currentTimeMillis();
        FileProperties.CleanupProperties cleanup = fileProperties.getCleanup();
        int timeoutMinutes = cleanup.getUploadingTimeoutMinutes();
        int excludeRecentMinutes = cleanup.getExcludeRecentMinutes();

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
        LocalDateTime excludeThreshold = LocalDateTime.now().minusMinutes(excludeRecentMinutes);

        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFile::getUploadStatus, 0)
                .lt(SysFile::getCreateTime, timeoutThreshold)
                .lt(SysFile::getCreateTime, excludeThreshold);

        List<SysFile> timeoutFiles = sysFileMapper.selectList(wrapper);
        int totalScanned = timeoutFiles.size();
        int totalCleaned = 0;
        int totalFailed = 0;
        StringBuilder detailBuilder = new StringBuilder("[");

        for (SysFile file : timeoutFiles) {
            try (var lease = lifecycle.tryLockFile(file.getFilePath())) {
                if (lease == null) continue;
                // This read must run outside a repeatable-read transaction: an upload may just have committed.
                SysFile current = sysFileMapper.selectById(file.getFileId());
                if (current == null || !Objects.equals(current.getUploadStatus(), 0)
                        || current.getCreateTime() == null || !current.getCreateTime().isBefore(timeoutThreshold)
                        || !current.getCreateTime().isBefore(excludeThreshold)) continue;
                fileStorageManager.deleteLocal(file.getFilePath());
                fileStorageManager.deleteRemote(file.getFilePath());
                sysFileService.removeById(file.getFileId());
                totalCleaned++;
                if (detailBuilder.length() > 1) {
                    detailBuilder.append(",");
                }
                detailBuilder.append(file.getFileId());
            } catch (Exception e) {
                totalFailed++;
                log.error("清理超时上传文件失败, fileId={}", file.getFileId(), e);
            }
        }
        detailBuilder.append("]");

        long executionTime = System.currentTimeMillis() - startTime;
        saveLog("UPLOAD_TIMEOUT", mode, totalScanned, totalCleaned, totalFailed,
                detailBuilder.toString(), executionTime, totalFailed == 0, null);

        log.info("上传超时清理完成: 扫描={}, 清理={}, 失败={}, 耗时={}ms",
                totalScanned, totalCleaned, totalFailed, executionTime);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void cleanupOrphanFiles(String mode) {
        authorizeManual(mode);
        try (var task = lifecycle.tryLockCleanup()) {
            if (task == null) return;
            doCleanupOrphanFiles(mode);
        }
    }

    private void doCleanupOrphanFiles(String mode) {
        long startTime = System.currentTimeMillis();
        int totalCleaned = 0;
        int totalFailed = 0;
        StringBuilder detailBuilder = new StringBuilder("[");
        Instant cutoff = Instant.now().minus(Math.max(1, fileProperties.getCleanup().getExcludeRecentMinutes()), ChronoUnit.MINUTES);

        try {
            List<String> diskFiles = fileStorageManager.scanLocalFiles();
            Set<String> dbFilePaths = new HashSet<>();

            LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysFile::getDeleted, 0)
                    .select(SysFile::getFilePath);
            List<SysFile> dbFiles = sysFileMapper.selectList(wrapper);
            for (SysFile f : dbFiles) {
                dbFilePaths.add(f.getFilePath());
            }

            List<String> orphanFiles = diskFiles.stream()
                    .filter(diskPath -> !dbFilePaths.contains(diskPath))
                    .collect(Collectors.toList());

            int totalScanned = orphanFiles.size();

            for (String orphanPath : orphanFiles) {
                try (var lease = lifecycle.tryLockFile(orphanPath)) {
                    if (lease == null) continue;
                    var localPath = fileStorageManager.getLocalPath(orphanPath);
                    if (!Files.exists(localPath) || !Files.getLastModifiedTime(localPath).toInstant().isBefore(cutoff)) continue;
                    Long references = sysFileMapper.selectCount(new LambdaQueryWrapper<SysFile>()
                            .eq(SysFile::getFilePath, orphanPath));
                    if (references > 0) continue;
                    fileStorageManager.deleteLocal(orphanPath);
                    totalCleaned++;
                    if (detailBuilder.length() > 1) {
                        detailBuilder.append(",");
                    }
                    detailBuilder.append("\"").append(orphanPath).append("\"");
                } catch (Exception e) {
                    totalFailed++;
                    log.error("清理孤儿文件失败: {}", orphanPath, e);
                }
            }
            detailBuilder.append("]");

            long executionTime = System.currentTimeMillis() - startTime;
            saveLog("ORPHAN_SCAN", mode, totalScanned, totalCleaned, totalFailed,
                    detailBuilder.toString(), executionTime, totalFailed == 0, null);

            log.info("孤儿文件扫描完成: 扫描={}, 清理={}, 失败={}, 耗时={}ms",
                    totalScanned, totalCleaned, totalFailed, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            saveLog("ORPHAN_SCAN", mode, 0, 0, 0, null, executionTime, false, e.getMessage());
            log.error("孤儿文件扫描异常", e);
        }
    }

    @Override
    public Page<SysFileCleanupLogVO> listCleanupLogs(CleanupLogQueryDTO queryDTO) {
        if (!permissionService.hasPermi("system:file:cleanup")) {
            throw new AccessDeniedException("没有文件清理权限");
        }
        Page<SysFileCleanupLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SysFileCleanupLog> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getCleanupType() != null && !queryDTO.getCleanupType().isEmpty()) {
            wrapper.eq(SysFileCleanupLog::getCleanupType, queryDTO.getCleanupType());
        }
        if (queryDTO.getCleanupMode() != null && !queryDTO.getCleanupMode().isEmpty()) {
            wrapper.eq(SysFileCleanupLog::getCleanupMode, queryDTO.getCleanupMode());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysFileCleanupLog::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByDesc(SysFileCleanupLog::getCreateTime);
        Page<SysFileCleanupLog> logPage = cleanupLogMapper.selectPage(page, wrapper);

        Page<SysFileCleanupLogVO> voPage = new Page<>(logPage.getCurrent(), logPage.getSize(), logPage.getTotal());
        voPage.setRecords(logPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    private void saveLog(String cleanupType, String mode, int scanned, int cleaned, int failed,
                         String detail, long executionTime, boolean success, String errorMessage) {
        SysFileCleanupLog logEntity = new SysFileCleanupLog();
        logEntity.setCleanupType(cleanupType);
        logEntity.setCleanupMode(mode);
        logEntity.setTotalScanned(scanned);
        logEntity.setTotalCleaned(cleaned);
        logEntity.setTotalFailed(failed);
        logEntity.setCleanedDetail(detail);
        logEntity.setExecutionTime(executionTime);
        logEntity.setStatus(success ? 1 : 0);
        logEntity.setErrorMessage(errorMessage);
        cleanupLogMapper.insert(logEntity);
    }

    private SysFileCleanupLogVO convertToVO(SysFileCleanupLog logEntity) {
        SysFileCleanupLogVO vo = new SysFileCleanupLogVO();
        vo.setLogId(logEntity.getLogId());
        vo.setCleanupType(logEntity.getCleanupType());
        vo.setCleanupTypeDesc("ORPHAN_SCAN".equals(logEntity.getCleanupType()) ? "磁盘孤儿扫描" : "上传超时清理");
        vo.setCleanupMode(logEntity.getCleanupMode());
        vo.setCleanupModeDesc("AUTO".equals(logEntity.getCleanupMode()) ? "定时任务" : "手动触发");
        vo.setTotalScanned(logEntity.getTotalScanned());
        vo.setTotalCleaned(logEntity.getTotalCleaned());
        vo.setTotalFailed(logEntity.getTotalFailed());
        vo.setCleanedDetail(logEntity.getCleanedDetail());
        vo.setExecutionTime(logEntity.getExecutionTime());
        vo.setStatus(logEntity.getStatus());
        vo.setStatusDesc(logEntity.getStatus() == 1 ? "成功" : "失败");
        vo.setErrorMessage(logEntity.getErrorMessage());
        vo.setCreateTime(logEntity.getCreateTime());
        return vo;
    }

    private void authorizeManual(String mode) {
        if (!"AUTO".equals(mode) && !permissionService.hasPermi("system:file:cleanup")) {
            throw new AccessDeniedException("没有文件清理权限");
        }
    }
}
