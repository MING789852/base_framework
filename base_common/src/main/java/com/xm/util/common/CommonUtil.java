package com.xm.util.common;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.util.common.params.RetryResult;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class CommonUtil {

    private final static ThreadLocal<Integer> RetryCount = new ThreadLocal<>();

    private final static Pattern numberRegPattern= Pattern.compile("^([+-]?(0|([1-9]\\d*)))(\\.\\d+)?$");

    private final static Pattern percentRegPattern=Pattern.compile("^\\d+(\\.\\d+)?%$");

    // 最大重试次数
    private final static int retryMax = 3;

    // 3秒后重试
    private final static int awaitRetry = 3;

    public static boolean isNumber(String number){
        if (StrUtil.isBlank(number)){
            return false;
        }
        return  numberRegPattern.matcher(number).matches();
    }

    public static BigDecimal getBigDecimalWithScale(BigDecimal num,Integer scale, RoundingMode roundingMode){
        if (num==null){
            return BigDecimal.ZERO;
        }
        return num.setScale(scale,roundingMode);
    }

    public static boolean isPercentage(String percent) {
        if (StrUtil.isBlank(percent)){
            return false;
        }
        // 正则表达式匹配百分比格式
        return percentRegPattern.matcher(percent).matches();
    }

    public static String bigDecimalToPercent(BigDecimal bigDecimal){
        DecimalFormat decimalFormat=new DecimalFormat("0.00%");
        if (bigDecimal==null){
            return "0.00%";
        }
        return decimalFormat.format(bigDecimal);
    }

    public static String bigDecimalToPercentDWithScale(BigDecimal bigDecimal,Integer scale){
        String defaultPattern="0.00%";
        if (scale>=0){
            defaultPattern=StrUtil.format("0.{}%",StrUtil.repeat("0",scale));
        }
        DecimalFormat decimalFormat=new DecimalFormat(defaultPattern);
        return decimalFormat.format(bigDecimal);
    }

    public static BigDecimal percentToBigDecimal(String percent) {
        DecimalFormat decimalFormat=new DecimalFormat("0.00%");
        try {
            return new BigDecimal(decimalFormat.parse(percent).toString());
        } catch (ParseException e) {
            return null;
        }
    }

    public static <K,V> Map<K,V> mergedMap(List<Map<K,V>> mapList){
        if (CollectionUtil.isEmpty(mapList)){
            return new HashMap<>();
        }else {
            return mapList.stream().flatMap(item->item.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }

    public static BigDecimal subtract(BigDecimal minuend,BigDecimal subtrahend){
        //被减数为null时置为0
        if (minuend==null){
            minuend =  BigDecimal.ZERO;
        }
        //减数为null时置为0
        if (subtrahend==null){
            subtrahend = BigDecimal.ZERO;
        }
        return minuend.subtract(subtrahend);
    }

    public static BigDecimal add(BigDecimal one,BigDecimal two){
        if (one==null){
            one =  BigDecimal.ZERO;
        }
        if (two==null){
            two = BigDecimal.ZERO;
        }
        return one.add(two);
    }

    public static BigDecimal divisionWithoutDenominatorZero(BigDecimal dividend,BigDecimal divisor){
        //分子为空时
        if (dividend==null){
            return BigDecimal.ZERO;
        }
        //分母为空或分母为零时
        if (divisor==null||BigDecimal.ZERO.compareTo(divisor)==0){
            return BigDecimal.ZERO;
        }
        return dividend.divide(divisor,4, RoundingMode.HALF_UP);
    }


    public static BigDecimal divisionWithoutDenominatorZeroWithScale(BigDecimal dividend,BigDecimal divisor,Integer scale){
        //分子为空时
        if (dividend==null){
            return BigDecimal.ZERO;
        }
        //分母为空或分母为零时
        if (divisor==null||BigDecimal.ZERO.compareTo(divisor)==0){
            return BigDecimal.ZERO;
        }
        return dividend.divide(divisor,scale, RoundingMode.HALF_UP);
    }

    /**
     * 执行的方法重试
     * 注：不要在多线程中调用此方法
     */
    public static <R> R retry(Supplier<RetryResult<R>> supplier, String tag){
        Integer currentRetryCount = RetryCount.get();
        if (currentRetryCount==null){
            RetryCount.set(1);
            currentRetryCount = 1;
        }

        try {
            RetryResult<R> retryResult=supplier.get();
            if (retryResult.isRetry()){
                if (currentRetryCount<=retryMax) {
                    Thread.sleep(awaitRetry*1000);
                    String msg = StrUtil.format("【{}】失败原因->{},开始第{}次重试",
                            tag,retryResult.getRetryMsg(), currentRetryCount);
                    log.error(msg);
                    RetryCount.set(currentRetryCount+1);
                    return retry(supplier,tag);
                } else {
                    return  null;
                }
            }else {
                return retryResult.getReturnData();
            }
        }catch (Exception e){
            String msg =StrUtil.format("【{}】执行重试第{}次失败，原因->{}",tag,currentRetryCount,ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(e.getMessage());
        }
    }

    public static <T> void fillDefaultValueInObject(T object, Class<T> tClass, List<String> fieldNameList){
        if (object==null){
            return;
        }
        if (CollectionUtil.isEmpty(fieldNameList)){
            return;
        }
        Field[] fields=ReflectUtil.getFields(tClass);
        if (fields==null){
            return;
        }
        try {
            Date now=new Date();
            for (Field field:fields){
                if (fieldNameList.contains(field.getName())){
                    field.setAccessible(true);
                    Class<?> type = field.getType();
                    Object value = field.get(object);
                    if (value==null){
                        if (type.equals(String.class)) {
                            field.set(object,"");
                        }
                        if (type.equals(Date.class)){
                            field.set(object,now);
                        }
                        if (type.equals(BigDecimal.class)){
                            field.set(object,BigDecimal.ZERO);
                        }
                        if (type.equals(Integer.class)){
                            field.set(object,0);
                        }
                        if (type.equals(Boolean.class)){
                            field.set(object,false);
                        }
                    }
                }
            }
        } catch (Exception e) {
            String msg= StrUtil.format("填充默认值失败,{}", ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        }
    }

    /**
     * 去除对象中的字符类型属性的前后空格
     */
    public static <T> void StrTrimInObject(T object, Class<T> tClass){
        if (object==null){
            return;
        }
        Field[] fields=ReflectUtil.getFields(tClass);
        if (fields==null){
            return;
        }
        try {
            for (Field field:fields){
                field.setAccessible(true);
                Object value=field.get(object);
                if (field.get(object) instanceof String){
                    String valueStr= value.toString();
                    valueStr=valueStr.trim();
                    field.set(object,valueStr);
                }
            }
        } catch (Exception e) {
            String msg= StrUtil.format("去除对象字符串对象前后空格失败,{}", ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        }
    }

    public static List<Integer> splitNumber(int number){
        List<Integer> list = new LinkedList<>();
        for (int i = number; i > 0; i /= 10) {
            list.add(i % 10);
        }
        return CollectionUtil.reverse(list);
    }

    public static String generateAlphabeticalVersion(int index) {
        if (index < 0 || index > 25) {
            throw new IllegalArgumentException("Index must be between 0 and 25");
        }
        return String.valueOf((char) ('A' + index));
    }

    public static String updateAlphabeticalVersion(String version) {
        char versionChar = version.charAt(0);
        versionChar= (char) (versionChar+1);
        if ('A'<=versionChar&&versionChar<='Z') {
            return String.valueOf(versionChar);
        }else {
            throw new IllegalArgumentException("版本更新需要在A-Z之间");
        }
    }

    // 将 Image 对象转换为字节数组
    public static byte[] imageToByteArray(Image image) {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            ImageIO.write(imageToBufferedImage(image), "png", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 将 Image 对象转换为 BufferedImage 对象
    private static BufferedImage imageToBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.drawImage(image, 0, 0, null);
        graphics2D.dispose();
        return bufferedImage;
    }

    public static String getMethodPath(Class<?> clazz,String methodName){
        return StrUtil.format("{}.{}",clazz.getName(),methodName);
    }
}
