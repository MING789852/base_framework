package com.xm.util.common;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.xm.advice.exception.exception.CommonException;


import java.util.Date;

public class DateXUtil {

    /**
     * 当天是否未月末前n天范围内
     * @param day
     * @return
     */
    public static Boolean judgePreDayEndOfMonthRange(int day){
        if (day<1){
            throw new CommonException("参数错误,提前天数需大于等于1");
        }
        day=day-1;
        Date now = new Date();
        //获取当月最后一天
        Date end= DateUtil.endOfMonth(now);
        Date preEnd=DateUtil.offset(end, DateField.DAY_OF_MONTH,-day);

        String nowStr=DateUtil.format(now,"yyyyMMdd");
        String preEndStr=DateUtil.format(preEnd,"yyyyMMdd");

        if (Integer.parseInt(nowStr)>=Integer.parseInt(preEndStr)){
            return true;
        }else {
            return false;
        }
    }


    /**
     * 当天是否未月末前n天
     * @param day
     * @return
     */
    public static Boolean judgePreDayEndOfMonth(int day){
        if (day<1){
            throw new CommonException("参数错误,提前天数需大于等于1");
        }
        day=day-1;
        Date now = new Date();
        //获取当月最后一天
        Date end= DateUtil.endOfMonth(now);
        Date preEnd=DateUtil.offset(end, DateField.DAY_OF_MONTH,-day);

        String nowStr=DateUtil.format(now,"yyyyMMdd");
        String preEndStr=DateUtil.format(preEnd,"yyyyMMdd");

        if (Integer.parseInt(nowStr)==Integer.parseInt(preEndStr)){
            return true;
        }else {
            return false;
        }
    }


    public static String formatDate(Date date){
        String format="yyyy-MM-dd HH:mm:ss";
        return DateUtil.format(date,format);
    }
}
