package com.jiaoshoujia.framework.aspectj;

import com.jiaoshoujia.common.annotation.RateLimiter;
import com.jiaoshoujia.common.exception.BusinessException;
import com.jiaoshoujia.common.utils.IpUtils;
import com.jiaoshoujia.common.utils.ServletUtils;
import com.jiaoshoujia.framework.cache.CacheService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimiterAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterAspect.class);

    private final CacheService cacheService;

    public RateLimiterAspect(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Around("@annotation(rateLimiter)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        HttpServletRequest request = ServletUtils.getRequest();
        String ip = request != null ? IpUtils.getIpAddr(request) : "unknown";

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String key = "rate_limit:" + ip + ":" + method.getDeclaringClass().getName() + "." + method.getName();

        long count = cacheService.increment(key);
        if (count == 1) {
            cacheService.expire(key, 1, TimeUnit.SECONDS);
        }

        long maxCount = (long) Math.ceil(rateLimiter.qps());
        if (count > maxCount) {
            log.warn("Rate limit exceeded for key [{}], count={}, qps={}", key, count, rateLimiter.qps());
            throw new BusinessException(rateLimiter.message());
        }

        return joinPoint.proceed();
    }
}
