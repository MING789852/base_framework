package com.xm.configuration.thread;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

/**
 * 自定义异常处理类
 */
@Slf4j
public class CommonAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    //手动处理捕获的异常
    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
        log.error("ExceptionMessage:{}", throwable.getMessage());
        log.error("MethodName:{}", method.getName());
        for (Object param : obj) {
            log.error("Parameter:{}", param);
        }
    }
}