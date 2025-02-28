package com.xm.params;

import lombok.Data;

import java.util.concurrent.TimeUnit;

@Data
public class CacheObject<T> {
    T data;
    long expire;
    TimeUnit unit;

    public CacheObject(T data, long expire,TimeUnit unit) {
        this.data = data;
        this.expire = expire;
        this.unit = unit;
    }

    public long getNanos(){
        return unit.toNanos(expire);
    }
}
