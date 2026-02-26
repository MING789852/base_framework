package com.xm.lock;

import com.xm.advice.exception.exception.CommonException;
import com.xm.core.lock.LockService;
import com.xm.util.bean.SpringBeanUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
@DependsOn("SpringBeanUtil")
public class RedissonLockService implements LockService {
    private final static RedissonClient redissonClient;

    static {
        redissonClient= SpringBeanUtil.getBeanByClass(RedissonClient.class);
    }

    @Override
    public String getLockPrefix() {
        return "lock:";
    }

    @Override
    public <T> T lock(String lockCode, Supplier<T> executeSupplier) {
        RLock rLock=redissonClient.getLock(getLockPrefix()+lockCode);
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
        RLock rLock=redissonClient.getLock(getLockPrefix()+lockCode);
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
