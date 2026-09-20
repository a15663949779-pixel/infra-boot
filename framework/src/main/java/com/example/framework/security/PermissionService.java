package com.example.framework.security;

import com.example.common.security.LoginUser;
import com.example.common.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @WJL
 * @date 2026/9/15 11:17
 * @description
 * 自定义权限校验服务，Bean 名称指定为 "ss"（Security Service 简写）
 */
@Service("ss")
public class PermissionService {

    private static final String ALL_PERMISSION = "*:*:*";

    public boolean hasPermi(String permission) {
        if (!StringUtils.hasText(permission)) {
            return false;
        }

        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || CollectionUtils.isEmpty(loginUser.getPermissions())) {
            return false;
        }

        // 超级管理员直接放行
        if (isSuperAdmin(loginUser)
                || loginUser.getPermissions().contains(ALL_PERMISSION)) {
            return true;
        }

        return loginUser.getPermissions().contains(permission.trim());
    }

    private boolean isSuperAdmin(LoginUser loginUser) {
        return Long.valueOf(1L).equals(loginUser.getUserId());
    }
}