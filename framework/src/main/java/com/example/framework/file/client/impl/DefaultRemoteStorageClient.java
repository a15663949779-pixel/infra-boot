package com.example.framework.file.client.impl;

import com.example.framework.file.client.RemoteStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * 默认远程存储客户端实现（仅打印日志，预留接入 OSS/MinIO）
 */
@Slf4j
@Component
public class DefaultRemoteStorageClient implements RemoteStorageClient {

    @Override
    public String upload(String filePath, InputStream inputStream) {
        log.info("[RemoteStorage] upload: {}", filePath);
        return null;
    }

    @Override
    public InputStream download(String filePath) {
        log.info("[RemoteStorage] download: {}", filePath);
        return null;
    }

    @Override
    public void delete(String filePath) {
        log.info("[RemoteStorage] delete: {}", filePath);
    }
}
