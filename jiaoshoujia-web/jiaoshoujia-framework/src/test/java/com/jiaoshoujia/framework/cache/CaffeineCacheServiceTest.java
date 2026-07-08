package com.jiaoshoujia.framework.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Caffeine 缓存服务测试")
class CaffeineCacheServiceTest {

    @Test
    @DisplayName("计数器应在过期后重新计数")
    void counterExpiresAfterConfiguredTimeout() throws InterruptedException {
        CaffeineCacheService cacheService = new CaffeineCacheService();
        String key = "rate_limit:test";

        assertEquals(1, cacheService.increment(key));
        cacheService.expire(key, 50, TimeUnit.MILLISECONDS);
        assertEquals(2, cacheService.increment(key));

        Thread.sleep(80);

        assertEquals(1, cacheService.increment(key));
    }
}
