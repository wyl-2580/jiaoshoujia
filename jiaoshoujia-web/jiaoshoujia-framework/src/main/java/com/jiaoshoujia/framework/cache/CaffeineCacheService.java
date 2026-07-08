package com.jiaoshoujia.framework.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
@ConditionalOnProperty(name = "app.cache.type", havingValue = "caffeine", matchIfMissing = true)
public class CaffeineCacheService implements CacheService {

    private record CacheEntry(Object value, long expireAt) {
        boolean isExpired() {
            return expireAt > 0 && System.currentTimeMillis() > expireAt;
        }
    }

    private static class CounterEntry {
        private final AtomicLong value = new AtomicLong(0);
        private volatile long expireAt = -1;

        boolean isExpired() {
            return expireAt > 0 && System.currentTimeMillis() > expireAt;
        }

        long incrementAndGet() {
            return value.incrementAndGet();
        }

        void expireAfter(long timeout, TimeUnit unit) {
            expireAt = System.currentTimeMillis() + unit.toMillis(timeout);
        }
    }

    private final Cache<String, CacheEntry> cache;
    private final Cache<String, CounterEntry> counters;

    public CaffeineCacheService() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .build();
        this.counters = Caffeine.newBuilder()
                .maximumSize(10_000)
                .build();
    }

    @Override
    public <T> void set(String key, T value, long timeout, TimeUnit unit) {
        long expireAt = System.currentTimeMillis() + unit.toMillis(timeout);
        cache.put(key, new CacheEntry(value, expireAt));
    }

    @Override
    public <T> void set(String key, T value) {
        cache.put(key, new CacheEntry(value, -1));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry entry = cache.getIfPresent(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            cache.invalidate(key);
            return null;
        }
        return (T) entry.value();
    }

    @Override
    public boolean delete(String key) {
        cache.invalidate(key);
        counters.invalidate(key);
        return true;
    }

    @Override
    public boolean hasKey(String key) {
        CacheEntry entry = cache.getIfPresent(key);
        if (entry == null) {
            return false;
        }
        if (entry.isExpired()) {
            cache.invalidate(key);
            return false;
        }
        return true;
    }

    @Override
    public long increment(String key) {
        CounterEntry counter = counters.asMap().compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new CounterEntry();
            }
            return existing;
        });
        return counter.incrementAndGet();
    }

    @Override
    public void expire(String key, long timeout, TimeUnit unit) {
        CacheEntry entry = cache.getIfPresent(key);
        if (entry != null && !entry.isExpired()) {
            long expireAt = System.currentTimeMillis() + unit.toMillis(timeout);
            cache.put(key, new CacheEntry(entry.value(), expireAt));
        }
        CounterEntry counter = counters.getIfPresent(key);
        if (counter != null) {
            if (counter.isExpired()) {
                counters.invalidate(key);
            } else {
                counter.expireAfter(timeout, unit);
            }
        }
    }
}
