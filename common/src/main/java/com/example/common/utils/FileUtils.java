package com.example.common.utils;

import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

public final class FileUtils {

    private FileUtils() {}

    private static final String[] SIZE_UNITS = {"B", "KB", "MB", "GB", "TB"};
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    /**
     * 提取文件后缀名（小写，不含点）
     */
    public static String getExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }

    /**
     * 路径安全清洗，防止 ../ 路径穿越
     */
    public static String sanitizePath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        return path.replace("..", "").replace("/", "").replace("\\", "");
    }

    /**
     * 格式化文件大小为可读字符串
     */
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }
        int unitIndex = 0;
        double size = bytes;
        while (size >= 1024 && unitIndex < SIZE_UNITS.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return DECIMAL_FORMAT.format(size) + " " + SIZE_UNITS[unitIndex];
    }

    /**
     * 设置下载响应头（RFC5987 标准，支持中文文件名）
     */
    public static void setDownloadResponseHeader(HttpServletResponse response, String filename) {
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        String encodedName = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName);
    }
}
