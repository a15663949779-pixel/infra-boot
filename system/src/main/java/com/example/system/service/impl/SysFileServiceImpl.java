package com.example.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.exception.BusinessException;
import com.example.common.utils.FileUtils;
import com.example.common.utils.SecurityUtils;
import com.example.framework.security.PermissionService;
import com.example.framework.file.manager.FileLifecycleCoordinator;
import com.example.framework.file.manager.FileStorageManager;
import com.example.system.domain.dto.FileQueryDTO;
import com.example.system.domain.entity.SysFile;
import com.example.system.domain.vo.FileUploadVO;
import com.example.system.domain.vo.SysFileVO;
import com.example.system.mapper.SysFileMapper;
import com.example.system.service.SysFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements SysFileService {

    private final FileStorageManager fileStorageManager;
    private final FileLifecycleCoordinator lifecycle;
    private final PermissionService permissionService;

    public SysFileServiceImpl(FileStorageManager fileStorageManager, FileLifecycleCoordinator lifecycle,
                              PermissionService permissionService) {
        this.fileStorageManager = fileStorageManager;
        this.lifecycle = lifecycle;
        this.permissionService = permissionService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadVO upload(MultipartFile file, String businessType) {
        authorizeUpload(file, businessType);
        fileStorageManager.validateFile(file);
        String extension = FileUtils.getExtension(file.getOriginalFilename());
        String relativePath = fileStorageManager.generateRelativePath(businessType, extension);

        List<Path> createdPaths = new ArrayList<>();
        Runnable release = holdUntilCompletion(lifecycle.lockUpload(relativePath));
        registerTransactionSynchronization(createdPaths);
        try {
            createdPaths.add(fileStorageManager.getLocalPath(relativePath));
            fileStorageManager.saveToLocal(relativePath, file.getInputStream());
            SysFile sysFile = buildSysFile(file, relativePath, extension, businessType);
            sysFile.setUploadStatus(0);
            save(sysFile);
            if (fileStorageManager.syncToRemote(relativePath) != null) {
                sysFile.setRemoteSyncStatus(1);
            }
            sysFile.setUploadStatus(1);
            updateById(sysFile);
            return convertToUploadVO(sysFile);
        } catch (IOException e) {
            cleanupCreatedFiles(createdPaths);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        } catch (RuntimeException e) {
            cleanupCreatedFiles(createdPaths);
            throw e;
        } finally {
            release.run();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FileUploadVO> uploadBatch(List<MultipartFile> files, String businessType) {
        requirePermission("system:file:upload");
        List<Path> createdPaths = new ArrayList<>();
        List<SysFile> sysFileList = new ArrayList<>();
        List<Runnable> releases = new ArrayList<>();
        releases.add(holdUntilCompletion(lifecycle.lockBatchUpload()));
        registerTransactionSynchronization(createdPaths);

        try {
            for (MultipartFile file : files) {
                authorizeUpload(file, businessType);
                fileStorageManager.validateFile(file);
                String extension = FileUtils.getExtension(file.getOriginalFilename());
                String relativePath = fileStorageManager.generateRelativePath(businessType, extension);
                releases.add(holdUntilCompletion(lifecycle.lockUpload(relativePath)));

                try {
                    createdPaths.add(fileStorageManager.getLocalPath(relativePath));
                    fileStorageManager.saveToLocal(relativePath, file.getInputStream());
                } catch (IOException e) {
                    cleanupCreatedFiles(createdPaths);
                    throw new BusinessException("批量上传失败: " + e.getMessage());
                }

                SysFile sysFile = buildSysFile(file, relativePath, extension, businessType);
                sysFile.setUploadStatus(0);
                sysFileList.add(sysFile);
            }

            saveBatch(sysFileList);

            for (SysFile sysFile : sysFileList) {
                if (fileStorageManager.syncToRemote(sysFile.getFilePath()) != null) {
                    sysFile.setRemoteSyncStatus(1);
                }
                sysFile.setUploadStatus(1);
            }
            updateBatchById(sysFileList);

            return sysFileList.stream()
                    .map(this::convertToUploadVO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            cleanupCreatedFiles(createdPaths);
            throw e;
        } finally {
            releases.forEach(Runnable::run);
        }
    }

    @Override
    public void download(Long fileId, HttpServletResponse response) {
        SysFile sysFile = getById(fileId);
        if (sysFile == null || sysFile.getDeleted() == 1) {
            throw new BusinessException(404, "文件资源不存在");
        }
        checkAccess(sysFile, "system:file:download");

        Path localPath = fileStorageManager.getLocalPath(sysFile.getFilePath());

        if (!Files.exists(localPath)) {
            try {
                fileStorageManager.pullFromRemote(sysFile.getFilePath());
            } catch (Exception e) {
                throw new BusinessException("文件在服务器上已丢失: " + e.getMessage());
            }
        }

        FileUtils.setDownloadResponseHeader(response, sysFile.getOriginalName());

        try (InputStream inputStream = Files.newInputStream(localPath)) {
            inputStream.transferTo(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Long> fileIds) {
        List<SysFile> files = listByIds(fileIds);
        if (files.isEmpty()) {
            return;
        }
        files.forEach(file -> checkAccess(file, "system:file:delete"));

        List<String> filePaths = files.stream()
                .map(SysFile::getFilePath)
                .collect(Collectors.toList());

        removeBatchByIds(fileIds);

        afterCommit(() -> filePaths.forEach(filePath -> {
            try {
                fileStorageManager.deleteLocal(filePath);
                fileStorageManager.deleteRemote(filePath);
            } catch (RuntimeException ex) {
                log.error("文件记录已删除，物理文件待清理: {}", filePath, ex);
            }
        }));
    }

    @Override
    public Page<SysFileVO> list(FileQueryDTO queryDTO) {
        requirePermission("system:file:list");
        Page<SysFile> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getOriginalName() != null && !queryDTO.getOriginalName().isEmpty()) {
            wrapper.like(SysFile::getOriginalName, queryDTO.getOriginalName());
        }
        if (queryDTO.getBusinessType() != null && !queryDTO.getBusinessType().isEmpty()) {
            wrapper.eq(SysFile::getBusinessType, queryDTO.getBusinessType());
        }
        if (queryDTO.getFileExtension() != null && !queryDTO.getFileExtension().isEmpty()) {
            wrapper.eq(SysFile::getFileExtension, queryDTO.getFileExtension());
        }
        if (queryDTO.getBeginTime() != null) {
            wrapper.ge(SysFile::getCreateTime, queryDTO.getBeginTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(SysFile::getCreateTime, queryDTO.getEndTime());
        }

        wrapper.orderByDesc(SysFile::getCreateTime);

        Page<SysFile> filePage = page(page, wrapper);

        Page<SysFileVO> voPage = new Page<>(filePage.getCurrent(), filePage.getSize(), filePage.getTotal());
        voPage.setRecords(filePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public SysFileVO getDetail(Long fileId) {
        SysFile sysFile = getById(fileId);
        if (sysFile == null || sysFile.getDeleted() == 1) {
            throw new BusinessException(404, "文件不存在");
        }
        checkAccess(sysFile, "system:file:query");
        return convertToVO(sysFile);
    }

    @Override
    public void validateAvatar(Long fileId) {
        SysFile file = getById(fileId);
        if (file == null || SecurityUtils.getUserId() == null
                || !Objects.equals(file.getOwnerId(), SecurityUtils.getUserId())
                || !"avatar".equals(file.getBusinessType()) || !Objects.equals(file.getUploadStatus(), 1)) {
            throw new AccessDeniedException("只能使用自己上传完成的头像");
        }
    }

    private SysFile buildSysFile(MultipartFile file, String relativePath, String extension, String businessType) {
        SysFile sysFile = new SysFile();
        sysFile.setOriginalName(file.getOriginalFilename());
        sysFile.setFileName(relativePath.substring(relativePath.lastIndexOf('/') + 1));
        sysFile.setFilePath(relativePath);
        sysFile.setFileUrl(fileStorageManager.getWebUrl(relativePath));
        sysFile.setFileExtension(extension);
        sysFile.setFileSize(file.getSize());
        sysFile.setFileSizeFormat(FileUtils.formatFileSize(file.getSize()));
        sysFile.setContentType(file.getContentType());
        sysFile.setStorageType("LOCAL");
        sysFile.setRemoteSyncStatus(0);
        sysFile.setUploadStatus(0);
        sysFile.setBusinessType(businessType);
        sysFile.setOwnerId(SecurityUtils.getUserId());
        sysFile.setDeleted(0);
        return sysFile;
    }

    private FileUploadVO convertToUploadVO(SysFile sysFile) {
        FileUploadVO vo = new FileUploadVO();
        vo.setFileId(sysFile.getFileId());
        vo.setOriginalName(sysFile.getOriginalName());
        vo.setFileUrl(sysFile.getFileUrl());
        vo.setFileSizeFormat(sysFile.getFileSizeFormat());
        vo.setFileExtension(sysFile.getFileExtension());
        return vo;
    }

    private SysFileVO convertToVO(SysFile sysFile) {
        SysFileVO vo = new SysFileVO();
        vo.setFileId(sysFile.getFileId());
        vo.setOriginalName(sysFile.getOriginalName());
        vo.setFileName(sysFile.getFileName());
        vo.setFilePath(sysFile.getFilePath());
        vo.setFileUrl(sysFile.getFileUrl());
        vo.setFileExtension(sysFile.getFileExtension());
        vo.setFileSize(sysFile.getFileSize());
        vo.setFileSizeFormat(sysFile.getFileSizeFormat());
        vo.setContentType(sysFile.getContentType());
        vo.setFileMd5(sysFile.getFileMd5());
        vo.setStorageType(sysFile.getStorageType());
        vo.setRemoteSyncStatus(sysFile.getRemoteSyncStatus());
        vo.setBusinessType(sysFile.getBusinessType());
        vo.setCreateBy(sysFile.getCreateBy());
        vo.setCreateTime(sysFile.getCreateTime());
        vo.setUpdateBy(sysFile.getUpdateBy());
        vo.setUpdateTime(sysFile.getUpdateTime());
        vo.setRemark(sysFile.getRemark());
        return vo;
    }

    private void cleanupCreatedFiles(List<Path> createdPaths) {
        for (Path path : createdPaths) {
            try {
                Files.deleteIfExists(path);
                log.info("清理已创建文件: {}", path);
            } catch (IOException e) {
                log.error("清理文件失败: {}", path, e);
            }
        }
    }

    private void registerTransactionSynchronization(List<Path> createdPaths) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    cleanupCreatedFiles(createdPaths);
                }
            }
        });
    }

    private Runnable holdUntilCompletion(FileLifecycleCoordinator.Lease lease) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return lease::close;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) { lease.close(); }
        });
        return () -> { };
    }

    private void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { action.run(); }
        });
    }

    private void requirePermission(String permission) {
        if (SecurityUtils.getUserId() == null || !permissionService.hasPermi(permission)) {
            throw new AccessDeniedException("没有文件操作权限");
        }
    }

    private void checkAccess(SysFile file, String permission) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null || (!Objects.equals(file.getOwnerId(), userId)
                && !permissionService.hasPermi(permission))) {
            throw new AccessDeniedException("没有该文件的访问权限");
        }
    }

    private void authorizeUpload(MultipartFile file, String businessType) {
        if (SecurityUtils.getUserId() == null) throw new AccessDeniedException("未登录");
        if (businessType == null || !businessType.matches("[a-zA-Z0-9_-]{1,50}")) {
            throw new BusinessException("非法的文件业务类型");
        }
        if (!"avatar".equals(businessType)) {
            requirePermission("system:file:upload");
            return;
        }
        if (file.isEmpty() || file.getSize() > 2 * 1024 * 1024
                || !Set.of("png", "jpg", "jpeg", "gif").contains(FileUtils.getExtension(file.getOriginalFilename()))) {
            throw new BusinessException("头像仅支持不超过2MB的 JPG/PNG/GIF 图片");
        }
        try (InputStream input = file.getInputStream();
             javax.imageio.stream.ImageInputStream image = javax.imageio.ImageIO.createImageInputStream(input)) {
            if (image == null) throw new BusinessException("无效的头像图片");
            var readers = javax.imageio.ImageIO.getImageReaders(image);
            if (!readers.hasNext()) throw new BusinessException("无效的头像图片");
            var reader = readers.next();
            try {
                reader.setInput(image);
                String format = reader.getFormatName().toLowerCase(java.util.Locale.ROOT);
                if (!Set.of("png", "jpeg", "gif").contains(format)
                        || reader.getWidth(0) > 4096 || reader.getHeight(0) > 4096) {
                    throw new BusinessException("头像格式不支持或尺寸超过4096像素");
                }
            } finally { reader.dispose(); }
        } catch (IOException ex) {
            throw new BusinessException("无效的头像图片");
        }
    }
}
