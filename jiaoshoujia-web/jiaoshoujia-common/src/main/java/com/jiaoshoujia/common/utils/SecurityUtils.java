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

    public static boolean isAdmin(Long userId) {
        return userId != null && userId == 1L;
    }

    public static String encryptPassword(String password) {
        return PASSWORD_ENCODER.encode(password);
    }

    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }
}
