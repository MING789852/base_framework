package com.xm.util.cache.params;

import lombok.Data;

import java.time.Duration;

@Data
public class ExpireParams {
    private Duration duration;
    private long ttl;
    private String unit;
}
