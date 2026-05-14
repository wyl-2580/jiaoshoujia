package com.jiaoshoujia.common.utils;

import jakarta.servlet.http.HttpServletRequest;

public final class IpUtils {

    private IpUtils() {
    }

    private static final String UNKNOWN = "unknown";

    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // X-Forwarded-For may contain multiple IPs; the first one is the real client IP
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index).trim();
            }
            return ip;
        }
        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    public static boolean isInternalIp(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return false;
        }
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return true;
        }
        // 10.x.x.x
        if (ip.startsWith("10.")) {
            return true;
        }
        // 172.16.x.x - 172.31.x.x
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    return second >= 16 && second <= 31;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        // 192.168.x.x
        return ip.startsWith("192.168.");
    }

    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip);
    }
}
