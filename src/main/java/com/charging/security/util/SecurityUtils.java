package com.charging.security.util;

import com.charging.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前登录用户 ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "用户未登录");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new BusinessException(401, "用户信息异常");
    }

    /**
     * 获取当前登录用户角色（不含 ROLE_ 前缀）
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "用户未登录");
        }
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElseThrow(() -> new BusinessException(401, "用户角色信息缺失"));
    }

    /**
     * 判断当前用户是否为管理员
     */
    public static boolean isAdmin() {
        return "ADMIN".equals(getCurrentUserRole());
    }

    /**
     * 判断当前用户是否为运营商
     */
    public static boolean isOperator() {
        return "OPERATOR".equals(getCurrentUserRole());
    }

    /**
     * 判断当前用户是否为普通用户
     */
    public static boolean isUser() {
        return "USER".equals(getCurrentUserRole());
    }
}
