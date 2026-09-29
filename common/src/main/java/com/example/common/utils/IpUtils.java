package com.example.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public final class IpUtils {

    private IpUtils() {
    }

    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) return "unknown";
        String ip = request.getHeader("x-forwarded-for");
        if (!isValidIp(ip)) ip = request.getHeader("Proxy-Client-IP");
        if (!isValidIp(ip)) ip = request.getHeader("WL-Proxy-Client-IP");
        if (!isValidIp(ip)) ip = request.getHeader("X-Real-IP");
        if (!isValidIp(ip)) ip = request.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : (ip != null && ip.contains(",") ? ip.split(",")[0].trim() : ip);
    }

    private static boolean isValidIp(String ip) {
        return StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip);
    }
}
