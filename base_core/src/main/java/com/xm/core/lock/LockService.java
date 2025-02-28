package com.xm.core.lock;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface LockService {
    <T> T lock(String lockCode, Supplier<T> executeSupplier, Consumer<Exception> exceptionConsumer);
    <T> T tryLock(String lockCode,String lockMsg, Supplier<T> executeSupplier, Consumer<Exception> exceptionConsumer);
}
