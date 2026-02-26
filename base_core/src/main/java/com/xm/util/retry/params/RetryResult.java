package com.xm.util.retry.params;

import lombok.Data;

@Data
public class RetryResult<T> {

    boolean isSuccess;

    T returnData;

    String retryMsg;

    boolean isRetry;


    public static <T> RetryResult<T> success(T returnData) {
        RetryResult<T> retryResult = new RetryResult<>();
        retryResult.setSuccess(true);
        retryResult.setRetry(false);
        retryResult.setReturnData(returnData);
        return retryResult;
    }

    public static <T> RetryResult<T> failStop(String retryMsg) {
        RetryResult<T> retryResult = new RetryResult<>();
        retryResult.setSuccess(false);
        retryResult.setRetry(false);
        retryResult.setRetryMsg(retryMsg);
        return retryResult;
    }

    public static <T> RetryResult<T> failRetry(String retryMsg) {
        RetryResult<T> retryResult = new RetryResult<>();
        retryResult.setSuccess(false);
        retryResult.setRetry(true);
        retryResult.setRetryMsg(retryMsg);
        return retryResult;
    }

}
