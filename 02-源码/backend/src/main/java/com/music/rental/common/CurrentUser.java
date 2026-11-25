package com.music.rental.common;

import com.music.rental.entity.SysUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** 当前登录用户工具类 */
public class CurrentUser {

    public static SysUser get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SysUser u)) {
            throw new BizException("未登录或登录已过期");
        }
        return u;
    }

    public static Long id() {
        return get().getId();
    }

    public static String role() {
        return get().getRoleCode();
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(role());
    }

    public static boolean isStudent() {
        return "STUDENT".equals(role());
    }
}
