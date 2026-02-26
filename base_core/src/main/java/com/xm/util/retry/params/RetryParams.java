package com.xm.util.retry.params;

import lombok.Data;

@Data
public class RetryParams {
    //最大重试次数
    private int maxRetryCount = 3;
    //初始间隔 单位毫秒
    private int exponentialBackOffInitialInterval = 5000;
    //乘数
    private int exponentialBackOffMultiplier = 2;
    //最大间隔 单位毫秒
    private int exponentialBackOffMaxInterval = 30000;

    public RetryParams(int maxRetryCount, int exponentialBackOffInitialInterval, int exponentialBackOffMultiplier, int exponentialBackOffMaxInterval) {
        this.maxRetryCount = maxRetryCount;
        this.exponentialBackOffInitialInterval = exponentialBackOffInitialInterval;
        this.exponentialBackOffMultiplier = exponentialBackOffMultiplier;
        this.exponentialBackOffMaxInterval = exponentialBackOffMaxInterval;
    }

    public RetryParams() {
    }
}
