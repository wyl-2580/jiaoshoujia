package com.jiaoshoujia.common.enums;

public enum DataScopeType {

    ALL("1", "全部数据"),

    CUSTOM("2", "自定义"),

    DEPT("3", "本部门"),

    DEPT_AND_BELOW("4", "本部门及以下"),

    SELF("5", "仅本人");

    private final String code;
    private final String description;

    DataScopeType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static DataScopeType fromCode(String code) {
        for (DataScopeType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown DataScopeType code: " + code);
    }
}
