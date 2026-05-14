package com.jiaoshoujia.framework.aspectj;

public final class DataScopeContextHolder {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    private DataScopeContextHolder() {}

    public static void set(String sqlCondition) {
        CONTEXT.set(sqlCondition);
    }

    public static String get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
