package com.xm.util.retry;

import com.xm.advice.exception.exception.CommonException;
import com.xm.util.retry.params.RetryParams;
import com.xm.util.retry.params.RetryResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.*;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class RetryUtil {
    public static RetryTemplate getRetryTemplate(RetryParams params,String tag){
        if (params==null){
            params=new RetryParams();
        }
        // 重试策略：最多重试3次
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.registerListener(new RetryListener() {
            //重试开始前的初始化
            @Override
            public <T, E extends Throwable> boolean open(RetryContext retryContext, RetryCallback<T, E> retryCallback) {
                return true; // 必须返回true
            }

            //重试结束时的收尾(无论成功失败)
            @Override
            public <T, E extends Throwable> void close(RetryContext retryContext, RetryCallback<T, E> retryCallback, Throwable throwable) {
                log.info("【RetryTemplate】【{}】结束,是否成功->{},总执行次数->{}", tag, throwable==null,retryContext.getRetryCount()+1);
            }
            //每次重试失败时的处理
            @Override
            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                log.error("【RetryTemplate】【{}】开始失败重试，重试次数->{},异常->{}",tag, context.getRetryCount(), throwable!=null?throwable.getMessage():"NULL");
            }
        });
        int maxRetryCount = params.getMaxRetryCount();
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(RetryException.class, true);
        retryableExceptions.put(TerminatedRetryException.class, false);
        SimpleRetryPolicy retryPolicy=new SimpleRetryPolicy(maxRetryCount,retryableExceptions);
        // 退避策略：初始间隔x秒，乘数y，最大间隔z秒
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(params.getExponentialBackOffInitialInterval());
        backOffPolicy.setMultiplier(params.getExponentialBackOffMultiplier());
        backOffPolicy.setMaxInterval(params.getExponentialBackOffMaxInterval());
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }

    public static <R> R retry(Function<RetryContext, RetryResult<R>> function,String tag, RetryParams params){
        RetryTemplate retryTemplate = getRetryTemplate(params,tag);
        RetryResult<R> retryResult=retryTemplate.execute(retryContext -> {
            RetryResult<R> apply = function.apply(retryContext);
            if (apply.isSuccess()){
                return apply;
            }else {
                if (apply.isRetry()){
                    //抛出异常重试
                    throw new RetryException(apply.getRetryMsg());
                }else {
                    //停止重试
                    throw new TerminatedRetryException(apply.getRetryMsg());
                }
            }
        });
        if (retryResult.isSuccess()){
            return retryResult.getReturnData();
        }else {
            throw new CommonException(retryResult.getRetryMsg());
        }
    }
}
