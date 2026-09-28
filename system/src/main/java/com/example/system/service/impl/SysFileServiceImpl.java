package com.example.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.exception.BusinessException;
import com.example.common.utils.FileUtils;
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

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements SysFileService {

    private final FileStorageManager fileStorageManager;

    public SysFileServiceImpl(FileStorageManager fileStorageManager) {
        this.fileStorageManager = fileStorageManager;
    }

    @Override
    public FileUploadVO upload(MultipartFile file, String businessType) {
        fileStorageManager.validateFile(file);
        String extension = FileUtils.getExtension(file.getOriginalFilename());
        String relativePath = fileStorageManager.generateRelativePath(businessType, extension);

        try {
            fileStorageManager.saveToLocal(relativePath, file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }

        SysFile sysFile = buildSysFile(file, relativePath, extension);
        sysFile.setUploadStatus(0);
        save(sysFile);

        if (fileStorageManager.syncToRemote(relativePath) != null) {
            sysFile.setRemoteSyncStatus(1);
        }
        sysFile.setUploadStatus(1);
        updateById(sysFile);

        return convertToUploadVO(sysFile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FileUploadVO> uploadBatch(List<MultipartFile> files, String businessType) {
        List<Path> createdPaths = new ArrayList<>();
        List<SysFile> sysFileList = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                fileStorageManager.validateFile(file);
                String extension = FileUtils.getExtension(file.getOriginalFilename());
                String relativePath = fileStorageManager.generateRelativePath(businessType, extension);

                Path localPath;
                try {
                    localPath = fileStorageManager.saveToLocal(relativePath, file.getInputStream());
                    createdPaths.add(localPath);
                } catch (IOException e) {
                    cleanupCreatedFiles(createdPaths);
                    throw new BusinessException("批量上传失败: " + e.getMessage());
                }

                SysFile sysFile = buildSysFile(file, relativePath, extension);
                sysFile.setUploadStatus(0);
                sysFileList.add(sysFile);
            }

            saveBatch(sysFileList);

            registerTransactionSynchronization(createdPaths);

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
        }
    }

    @Override
    public void download(Long fileId, HttpServletResponse response) {
        SysFile sysFile = getById(fileId);
        if (sysFile == null || sysFile.getDeleted() == 1) {
            throw new BusinessException(404, "文件资源不存在");
        }

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

        List<String> filePaths = files.stream()
                .map(SysFile::getFilePath)
                .collect(Collectors.toList());

        removeBatchByIds(fileIds);

        for (String filePath : filePaths) {
            fileStorageManager.deleteLocal(filePath);
            fileStorageManager.deleteRemote(filePath);
        }
    }

    @Override
    public Page<SysFileVO> list(FileQueryDTO queryDTO) {
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
        return convertToVO(sysFile);
    }

    private SysFile buildSysFile(MultipartFile file, String relativePath, String extension) {
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
        sysFile.setBusinessType("default");
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
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    cleanupCreatedFiles(createdPaths);
                }
            }
        });
    }
}
