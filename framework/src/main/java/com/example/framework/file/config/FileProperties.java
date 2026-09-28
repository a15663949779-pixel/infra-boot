package com.example.framework.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    /**
     * 本地存储基础路径
     */
    private String basePath = "./upload/";

    /**
     * Web 访问路径前缀
     */
    private String accessPrefix = "/profile/";

    /**
     * 单个文件最大大小（默认 50MB）
     */
    private String maxSize = "50MB";

    /**
     * 允许上传的文件后缀（逗号分隔，空表示不限制）
     */
    private String allowedExtensions = "";

    /**
     * 远程存储配置
     */
    private RemoteProperties remote = new RemoteProperties();

    /**
     * 文件清理配置
     */
    private CleanupProperties cleanup = new CleanupProperties();

    @Data
    public static class RemoteProperties {
        /**
         * 是否启用远程存储
         */
        private boolean enabled = false;
    }

    @Data
    public static class CleanupProperties {
        /**
         * 方案1：磁盘孤儿文件扫描 cron 表达式（默认每月1号凌晨3点）
         */
        private String orphanCron = "0 0 3 1 * ?";

        /**
         * 方案2：上传超时清理 cron 表达式（默认每天凌晨2点）
         */
        private String timeoutCron = "0 0 2 * * ?";

        /**
         * 上传超时阈值（分钟），超过此时间仍处于上传中的文件将被清理
         */
        private int uploadingTimeoutMinutes = 60;

        /**
         * 排除最近 N 分钟内创建的文件（避免误清正在上传的文件）
         */
        private int excludeRecentMinutes = 30;

        /**
         * 是否启用定时清理
         */
        private boolean enabled = true;
    }
}
