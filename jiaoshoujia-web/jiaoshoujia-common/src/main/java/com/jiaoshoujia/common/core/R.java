package com.jiaoshoujia.common.core;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serial;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final int SUCCESS_CODE = 200;
    public static final int FAIL_CODE = 500;
    public static final String SUCCESS_MSG = "操作成功";
    public static final String FAIL_MSG = "操作失败";

    private int code;
    private String msg;
    private T data;

    public R() {
    }

    private R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return new R<>(SUCCESS_CODE, SUCCESS_MSG, null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(SUCCESS_CODE, SUCCESS_MSG, data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(SUCCESS_CODE, msg, data);
    }

    public static <T> R<T> fail() {
        return new R<>(FAIL_CODE, FAIL_MSG, null);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(FAIL_CODE, msg, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
