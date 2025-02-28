package com.xm.configuration.id;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnowIDIdGenerator implements IdentifierGenerator {


    private final SnowflakeGenerator snowflakeGenerator;


    @Override
    public Number nextId(Object entity) {
        //根据bizKey调用分布式ID生成
        Long id=snowflakeGenerator.next();
        log.info("开始自动生成ID->{}",id);
        return id;
    }

    @Override
    public String nextUUID(Object entity) {
        Long id=snowflakeGenerator.next();
        log.info("开始自动生成ID->{}",id);
        return id.toString();
    }
}
