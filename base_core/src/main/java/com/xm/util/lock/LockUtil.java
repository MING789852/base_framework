package com.xm.util.lock;


import com.xm.core.lock.LockService;
import com.xm.util.bean.SpringBeanUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LockUtil {

    private final static LockService lockService;

    static {
        lockService = SpringBeanUtil.getBeanByClass(LockService.class);
    }

    public static <T> T lock(String lockCode, Supplier<T> executeSupplier){
        return lockService.lock(lockCode,executeSupplier);
    }

    public static <T> T lock(String lockCode, Supplier<T> executeSupplier, Consumer<Exception> exceptionConsumer){
        return lockService.lock(lockCode,executeSupplier,exceptionConsumer);
    }

    public static <T> T tryLock(String lockCode, String lockMsg, Supplier<T> executeSupplier){
        return lockService.tryLock(lockCode,lockMsg,executeSupplier);
    }

    public static <T> T tryLock(String lockCode, String lockMsg, Supplier<T> executeSupplier, Consumer<Exception> exceptionConsumer){
        return lockService.tryLock(lockCode,lockMsg,executeSupplier,exceptionConsumer);
    }
}
