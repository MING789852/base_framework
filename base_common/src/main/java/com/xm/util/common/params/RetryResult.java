package com.xm.util.common.params;

import lombok.Data;

@Data
public class RetryResult<T> {
    T returnData;

    boolean isRetry;

    String retryMsg;
}
