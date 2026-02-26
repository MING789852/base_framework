package com.xm.lock;

import com.xm.advice.exception.exception.CommonException;
import com.xm.core.lock.LockService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class LocalLockService implements LockService {

    public static final Map<String, Lock> lockMap=new  ConcurrentHashMap<>();


    private synchronized Lock getRLock(String key){
        return lockMap.computeIfAbsent(key, k -> new ReentrantLock());
    }

    @Override
    public String getLockPrefix() {
        return "lock:";
    }

    @Override
    public <T> T lock(String lockCode, Supplier<T> executeSupplier) {
        Lock rLock = getRLock(getLockPrefix()+lockCode);
        try {
            rLock.lock();
            if (executeSupplier!=null){
                return executeSupplier.get();
            }
            return null;
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }finally {
            rLock.unlock();
        }
    }

    @Override
    public <T> T lock(String lockCode, Supplier<T> executeSupplier, Consumer<Exception> exceptionConsumer) {
        try {
            return lock(lockCode,executeSupplier);
        }catch (Exception e){
            if (exceptionConsumer!=null){
                exceptionConsumer.accept(e);
            }
            return null;
        }
    }

    @Override
    public <T> T tryLock(String lockCode, String lockMsg, Supplier<T> executeSupplier) {
        Lock rLock = getRLock(getLockPrefix()+lockCode);
        if (!rLock.tryLock()){
            throw new CommonException(lockMsg);
        }
        try {
            if (executeSupplier!=null){
                return executeSupplier.get();
            }
            return null;
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }finally {
            rLock.unlock();
        }
    }

    @Override
    public <T> T tryLock(String lockCode, String lockMsg, Supplier<T> executeSupplier, Consumer<Exception> exceptionConsumer) {
        try {
            return tryLock(lockCode,lockMsg,executeSupplier);
        }catch (Exception e){
            if (exceptionConsumer!=null){
                exceptionConsumer.accept(e);
            }
            return null;
        }
    }
}
