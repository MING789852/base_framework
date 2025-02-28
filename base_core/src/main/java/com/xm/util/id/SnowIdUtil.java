package com.xm.util.id;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.xm.util.bean.SpringBeanUtil;

public class SnowIdUtil {
    private static SnowflakeGenerator snowflakeGenerator;

    static {
        snowflakeGenerator= SpringBeanUtil.getBeanByClass(SnowflakeGenerator.class);
    }


    public static String getSnowId(){
        return snowflakeGenerator.next().toString();
    }
}
