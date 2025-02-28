package com.xm.lock;

import com.xm.advice.exception.exception.CommonException;
import com.xm.core.lock.LockService;
import com.xm.util.bean.SpringBeanUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class RedissonLockService implements LockService {
    private final static RedissonClient redissonClient;

    static {
        redissonClient= SpringBeanUtil.getBeanByClass(RedissonClient.class);
    }

    @Override
    public <T> T lock(String lockCode, Supplier<T> executeSupplier, Consumer<Exception> exceptionConsumer) {
        RLock rLock=redissonClient.getLock(lockCode);
        try {
            rLock.lock();
            return executeSupplier.get();
        }catch (Exception e){
            exceptionConsumer.accept(e);
            return null;
        }finally {
            rLock.unlock();
        }
    }

    @Override
    public <T> T tryLock(String lockCode, String lockMsg, Supplier<T> executeSupplier, Consumer<Exception> exceptionConsumer) {
        RLock rLock=redissonClient.getLock(lockCode);
        if (!rLock.tryLock()){
            throw new CommonException(lockMsg);
        }
        try {
            return executeSupplier.get();
        }catch (Exception e){
            exceptionConsumer.accept(e);
            return null;
        }finally {
            rLock.unlock();
        }
    }
}
