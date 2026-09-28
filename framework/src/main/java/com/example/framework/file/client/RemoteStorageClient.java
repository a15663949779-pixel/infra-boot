package com.example.framework.file.client;

import java.io.InputStream;

/**
 * 远程存储客户端接口（预留接入 OSS/MinIO）
 */
public interface RemoteStorageClient {

    /**
     * 上传文件到远程存储
     *
     * @param filePath 相对路径
     * @param inputStream 文件流
     * @return 远程存储 URL
     */
    String upload(String filePath, InputStream inputStream);

    /**
     * 从远程存储下载文件
     *
     * @param filePath 相对路径
     * @return 文件流
     */
    InputStream download(String filePath);

    /**
     * 删除远程存储文件
     *
     * @param filePath 相对路径
     */
    void delete(String filePath);
}
