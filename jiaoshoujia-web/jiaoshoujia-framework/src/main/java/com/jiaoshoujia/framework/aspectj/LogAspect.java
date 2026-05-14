package com.jiaoshoujia.framework.aspectj;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiaoshoujia.common.annotation.Log;
import com.jiaoshoujia.common.enums.BusinessType;
import com.jiaoshoujia.common.enums.OperatorType;
import com.jiaoshoujia.common.utils.IpUtils;
import com.jiaoshoujia.common.utils.SecurityUtils;
import com.jiaoshoujia.common.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public LogAspect(ApplicationEventPublisher eventPublisher, ObjectMapper objectMapper) {
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    public record OperLogEvent(
            String title,
            BusinessType businessType,
            OperatorType operatorType,
            String method,
            String requestMethod,
            String requestUrl,
            String ip,
            String operatorName,
            String requestParams,
            String responseResult,
            int status,
            String errorMsg,
            long costTime,
            LocalDateTime operTime
    ) {}

    @Around("@annotation(logAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, Log logAnnotation) throws Throwable {
        long startTime = System.currentTimeMillis();
        String errorMsg = null;
        int status = 0;
        Object result = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            errorMsg = e.getMessage();
            status = 1;
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            try {
                recordLog(joinPoint, logAnnotation, result, errorMsg, status, costTime);
            } catch (Exception e) {
                log.warn("Failed to record operation log: {}", e.getMessage());
            }
        }
    }

    private void recordLog(ProceedingJoinPoint joinPoint, Log logAnnotation,
                           Object result, String errorMsg, int status, long costTime) {
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        String method = className + "." + methodName + "()";

        HttpServletRequest request = ServletUtils.getRequest();
        String requestMethod = request != null ? request.getMethod() : "";
        String requestUrl = request != null ? request.getRequestURI() : "";
        String ip = request != null ? IpUtils.getIpAddr(request) : "";

        String operatorName;
        try {
            operatorName = SecurityUtils.getUsername();
        } catch (Exception e) {
            operatorName = "anonymous";
        }

        String requestParams = null;
        if (logAnnotation.isSaveRequestData()) {
            requestParams = truncate(argsToString(joinPoint.getArgs()));
        }

        String responseResult = null;
        if (logAnnotation.isSaveResponseData() && result != null) {
            responseResult = truncate(toJson(result));
        }

        OperLogEvent event = new OperLogEvent(
                logAnnotation.title(),
                logAnnotation.businessType(),
                logAnnotation.operatorType(),
                method,
                requestMethod,
                requestUrl,
                ip,
                operatorName,
                requestParams,
                responseResult,
                status,
                errorMsg,
                costTime,
                LocalDateTime.now()
        );
        eventPublisher.publishEvent(event);
    }

    private String argsToString(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof HttpServletRequest) {
                continue;
            }
            paramMap.put("arg" + i, arg);
        }
        return toJson(paramMap);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    private String truncate(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > 2000 ? text.substring(0, 2000) + "..." : text;
    }
}
