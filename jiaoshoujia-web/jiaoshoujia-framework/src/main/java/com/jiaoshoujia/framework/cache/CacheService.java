package com.jiaoshoujia.framework.cache;

import java.util.concurrent.TimeUnit;

public interface CacheService {

    <T> void set(String key, T value, long timeout, TimeUnit unit);

    <T> void set(String key, T value);

    <T> T get(String key);

    boolean delete(String key);

    boolean hasKey(String key);

    long increment(String key);

    void expire(String key, long timeout, TimeUnit unit);
}
