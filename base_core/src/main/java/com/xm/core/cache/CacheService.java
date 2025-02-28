package com.xm.core.cache;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface CacheService {
    Object get(String key);
    void set(String key, Object value);
    void set(String key, Object value, long timeout, TimeUnit unit);
    void delete(String key);
    Set<String> keys(String pattern);
    void put(String key, String hasKey, Object value);
    Object get(String key, String hasKey);
}
