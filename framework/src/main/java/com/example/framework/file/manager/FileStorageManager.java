package com.example.framework.file.manager;

import com.example.common.exception.BusinessException;
import com.example.common.utils.FileUtils;
import com.example.framework.file.client.RemoteStorageClient;
import com.example.framework.file.config.FileProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Component
public class FileStorageManager {

    private final FileProperties properties;
    private final RemoteStorageClient remoteClient;

    public FileStorageManager(FileProperties properties, RemoteStorageClient remoteClient) {
        this.properties = properties;
        this.remoteClient = remoteClient;
    }

    /**
     * 校验文件后缀和大小
     */
    public void validateFile(MultipartFile file) {
        String extension = FileUtils.getExtension(file.getOriginalFilename());
        String allowed = properties.getAllowedExtensions();
        if (allowed != null && !allowed.isEmpty()) {
            String[] allowedList = allowed.split(",");
            boolean isAllowed = false;
            for (String ext : allowedList) {
                if (ext.trim().equalsIgnoreCase(extension)) {
                    isAllowed = true;
                    break;
                }
            }
            if (!isAllowed) {
                throw new BusinessException("不支持的文件类型: " + extension);
            }
        }
    }

    /**
     * 生成相对存储路径（businessType/yyyy/MM/dd/uuid.ext）
     */
    public String generateRelativePath(String businessType, String extension) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String fileName = uuid + (extension.isEmpty() ? "" : "." + extension);
        return businessType + "/" + datePath + "/" + fileName;
    }

    /**
     * 保存文件到本地磁盘
     *
     * @param relativePath 相对路径
     * @param inputStream 文件流
     * @return 本地物理文件绝对路径
     */
    public Path saveToLocal(String relativePath, InputStream inputStream) {
        Path basePath = Paths.get(properties.getBasePath()).toAbsolutePath().normalize();
        Path targetPath = basePath.resolve(relativePath).normalize();

        // 防止路径穿越
        if (!targetPath.startsWith(basePath)) {
            throw new BusinessException("非法的文件路径");
        }

        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath;
        } catch (IOException e) {
            log.error("文件保存失败: {}", relativePath, e);
            throw new BusinessException("文件保存失败: " + e.getMessage());
        }
    }

    /**
     * 同步上传到远程存储
     */
    public String syncToRemote(String relativePath) {
        if (!properties.getRemote().isEnabled()) {
            return null;
        }
        try {
            Path localPath = getLocalPath(relativePath);
            if (Files.exists(localPath)) {
                InputStream inputStream = Files.newInputStream(localPath);
                return remoteClient.upload(relativePath, inputStream);
            }
        } catch (IOException e) {
            log.error("远程同步失败: {}", relativePath, e);
        }
        return null;
    }

    /**
     * 从远程存储拉取并缓存到本地
     */
    public Path pullFromRemote(String relativePath) {
        if (!properties.getRemote().isEnabled()) {
            throw new BusinessException("远程存储未启用，无法回源");
        }
        InputStream inputStream = remoteClient.download(relativePath);
        if (inputStream == null) {
            throw new BusinessException("远程文件不存在");
        }
        return saveToLocal(relativePath, inputStream);
    }

    /**
     * 检查本地文件是否存在
     */
    public boolean existsLocal(String relativePath) {
        Path localPath = getLocalPath(relativePath);
        return Files.exists(localPath);
    }

    /**
     * 删除本地物理文件
     */
    public void deleteLocal(String relativePath) {
        Path localPath = getLocalPath(relativePath);
        try {
            if (Files.exists(localPath)) {
                Files.delete(localPath);
                log.info("删除本地文件: {}", localPath);
            }
        } catch (IOException e) {
            log.error("删除本地文件失败: {}", relativePath, e);
        }
    }

    /**
     * 删除远程存储文件
     */
    public void deleteRemote(String relativePath) {
        if (properties.getRemote().isEnabled()) {
            remoteClient.delete(relativePath);
        }
    }

    /**
     * 获取本地物理文件绝对路径
     */
    public Path getLocalPath(String relativePath) {
        return Paths.get(properties.getBasePath()).toAbsolutePath().normalize().resolve(relativePath).normalize();
    }

    /**
     * 获取 Web 访问 URL
     */
    public String getWebUrl(String relativePath) {
        return properties.getAccessPrefix() + relativePath;
    }

    /**
     * 获取本地存储基础路径
     */
    public Path getBasePath() {
        return Paths.get(properties.getBasePath()).toAbsolutePath().normalize();
    }

    /**
     * 扫描本地磁盘所有文件的相对路径
     */
    public List<String> scanLocalFiles() {
        Path basePath = getBasePath();
        List<String> relativePaths = new ArrayList<>();
        if (!Files.exists(basePath)) {
            return relativePaths;
        }
        try (Stream<Path> stream = Files.walk(basePath)) {
            stream.filter(Files::isRegularFile)
                    .forEach(path -> relativePaths.add(basePath.relativize(path).toString().replace('\\', '/')));
        } catch (IOException e) {
            log.error("扫描本地文件失败", e);
        }
        return relativePaths;
    }
}
