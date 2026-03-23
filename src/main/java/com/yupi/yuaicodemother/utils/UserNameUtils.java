package com.yupi.yuaicodemother.utils;

import cn.hutool.core.util.StrUtil;

/**
 * 用户昵称处理工具
 */
public final class UserNameUtils {

    public static final String DEFAULT_USER_NAME = "无名";

    private UserNameUtils() {
    }

    /**
     * 统一用户展示名称，避免把占位昵称直接展示给前端。
     *
     * @param userName    用户昵称
     * @param userAccount 用户账号
     * @return 优先返回有效昵称，否则回退到账号名
     */
    public static String resolveDisplayName(String userName, String userAccount) {
        if (StrUtil.isNotBlank(userName) && !DEFAULT_USER_NAME.equals(userName.trim())) {
            return userName.trim();
        }
        return StrUtil.blankToDefault(StrUtil.trim(userAccount), DEFAULT_USER_NAME);
    }
}
