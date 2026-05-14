package com.jiaoshoujia.common.exception;

import java.io.Serial;

public class UnauthorizedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UnauthorizedException() {
        super("认证失败，请重新登录");
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
