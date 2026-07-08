package com.jiaoshoujia.common.utils;

import com.jiaoshoujia.common.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static String getUsername() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            throw new UnauthorizedException(MessageUtils.message("auth.user.not.found"));
        }
        return authentication.getName();
    }

    public static Long getUserId() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            throw new UnauthorizedException(MessageUtils.message("auth.user.not.found"));
        }
        Object principal = authentication.getPrincipal();
        if (principal != null) {
            try {
                java.lang.reflect.Method method = principal.getClass().getMethod("getUserId");
                Object result = method.invoke(principal);
                if (result instanceof Long) {
                    return (Long) result;
                }
            } catch (Exception ignored) {
            }
        }
        throw new UnauthorizedException(MessageUtils.message("auth.userid.not.found"));
    }

    /**
     * 是否为内置的三员账号（系统管理员=1、安全管理员=3、审计管理员=4）。
     * 三员账号受保护，不允许被删除 / 停用 / 越权重置密码，以保障三权分立不被破坏。
     */
    public static boolean isAdmin(Long userId) {
        return userId != null && (userId == 1L || userId == 3L || userId == 4L);
    }

    public static String encryptPassword(String password) {
        return PASSWORD_ENCODER.encode(password);
    }

    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }
}
