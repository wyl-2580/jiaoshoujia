package com.jiaoshoujia.framework.cache;

import java.util.concurrent.TimeUnit;

public interface CacheService {

    <T> void set(String key, T value, long timeout, TimeUnit unit);

    <T> void set(String key, T value);

    <T> T get(String key);

    boolean delete(String key);

    /**
     * 原子性获取并删除。用于 refresh token 轮换等需要保证单次消费的场景。
     * @return 旧值，若 key 不存在或已过期则返回 null
     */
    <T> T getAndDelete(String key);

    boolean hasKey(String key);

    long increment(String key);

    void expire(String key, long timeout, TimeUnit unit);
}
