package com.jiaoshoujia.common.utils;

import cn.hutool.core.util.StrUtil;

public class StringUtils extends StrUtil {

    public static boolean isEmpty(String str) {
        return StrUtil.isEmpty(str);
    }

    public static boolean isNotEmpty(String str) {
        return StrUtil.isNotEmpty(str);
    }

    public static String format(String template, Object... params) {
        return StrUtil.format(template, params);
    }
}
