package com.jiaoshoujia.framework.web;

import com.jiaoshoujia.common.constant.HttpStatus;
import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.exception.BusinessException;
import com.jiaoshoujia.common.exception.UnauthorizedException;
import com.jiaoshoujia.common.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public R<?> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public R<?> handleUnauthorizedException(UnauthorizedException e) {
        return R.fail(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public R<?> handleAccessDeniedException(AccessDeniedException e) {
        return R.fail(HttpStatus.FORBIDDEN, MessageUtils.message("auth.no.permission"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return R.fail(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(BindException.class)
    public R<?> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return R.fail(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return R.fail(HttpStatus.ERROR, MessageUtils.message("not.supported.method"));
    }

    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return R.fail(MessageUtils.message("system.error"));
    }
}
