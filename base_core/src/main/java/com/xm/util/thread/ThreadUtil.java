package com.xm.util.thread;

import com.xm.util.bean.SpringBeanUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
public class ThreadUtil {
    @Getter
    private final static ThreadPoolTaskExecutor executor;

    static {
        executor= SpringBeanUtil.getBeanByName("xmTaskExecutor",ThreadPoolTaskExecutor.class);
    }
}
