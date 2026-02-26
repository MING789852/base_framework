package com.xm.util.cache;

import cn.hutool.core.util.StrUtil;
import com.xm.util.cache.params.ExpireParams;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class CacheConfigUtil {

    private static final Duration defaultDuration = Duration.of(1, ChronoUnit.HOURS);

    public static ExpireParams getExpireParams(String ttlWithUnit) {
        ExpireParams expireParams=new ExpireParams();
        expireParams.setDuration(defaultDuration);
        expireParams.setUnit("h");
        expireParams.setTtl(1);
        expireParams.setUnLimitTime(false);

        //判断是否无限缓存
        if (StrUtil.isNotBlank(ttlWithUnit)) {
            if (ttlWithUnit.contains("u")){
                expireParams.setUnLimitTime(true);
                expireParams.setTtl(0);
                expireParams.setUnit("u");
                return expireParams;
            }
            String numberStr = ttlWithUnit.replaceAll("\\D", "");
            //判断数字是否为空
            if (StrUtil.isNotBlank(numberStr)) {
                String unit = ttlWithUnit.replaceAll("\\d", "");
                ChronoUnit chronoUnit;
                switch (unit) {
                    case "d":
                        chronoUnit = ChronoUnit.DAYS;
                        break;
                    case "h":
                        chronoUnit = ChronoUnit.HOURS;
                        break;
                    case "m":
                        chronoUnit = ChronoUnit.MINUTES;
                        break;
                    case "s":
                        chronoUnit = ChronoUnit.SECONDS;
                        break;
                    default:
                        unit = "h";
                        chronoUnit = ChronoUnit.HOURS;
                }
                //数字化为整数
                BigDecimal bigDecimal=new BigDecimal(numberStr);
                long ttl = bigDecimal.longValue();

                expireParams.setTtl(ttl);
                expireParams.setUnit(unit);
                expireParams.setDuration(Duration.of(ttl,chronoUnit));

                return expireParams;
            }
        }
        return expireParams;
    }
}
