package com.xm.util.common;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class CommonUtil {

    private final static Pattern numberRegPattern= Pattern.compile("^([+-]?(0|([1-9]\\d*)))(\\.\\d+)?$");

    private final static Pattern percentRegPattern=Pattern.compile("^\\d+(\\.\\d+)?%$");


    public static void main(String[] args) {
        System.out.println(Arrays.toString(CommonUtil.bigDecimalRatioSimplifier(new BigDecimal("462"), new BigDecimal("1071"))));
    }

    /**
     * 将枚举的指定字段转换为Map
     * @param enumClass 枚举类
     * @param keyGetter key的getter方法引用
     * @param valueGetter value的getter方法引用
     */
    public static <E extends Enum<E>, K, V> Map<K, V> enumsToMap(
            Class<E> enumClass,
            Function<E, K> keyGetter,
            Function<E, V> valueGetter) {

        return Arrays.stream(enumClass.getEnumConstants())
                .collect(Collectors.toMap(keyGetter, valueGetter, (k1, k2) -> k1));
    }

    public static String insertIntervalStr(String originalString,String intervalStr,int interval){
        // 输入验证
        if (originalString == null || originalString.isEmpty()) {
            return originalString; // 或者返回空字符串 ""
        }
        if (interval <= 0) {
            throw new IllegalArgumentException("间隔参数interval必须大于0");
        }
        if (intervalStr == null) {
            intervalStr = ""; // 如果间隔字符串为null，按空字符串处理
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < originalString.length(); i++) {
            sb.append(originalString.charAt(i));
            // 在指定间隔位置（且不是最后一个字符）插入换行符
            if ((i + 1) % interval == 0&& (i + 1)!=originalString.length()) {
                sb.append(intervalStr);
            }
        }
        return sb.toString();
    }

    public static BigDecimal[] bigDecimalRatioSimplifier(BigDecimal num1,BigDecimal num2){
        return bigDecimalRatioSimplifierWithScale(num1, num2,0);
    }

    public static BigDecimal[] bigDecimalRatioSimplifierWithScale(BigDecimal num1,BigDecimal num2, int resultScale){
        // 3. 计算缩放因子（模拟GCD）
        // 将BigDecimal转换为BigInteger，通过缩放小数点后位数
        int scale = Math.max(num1.scale(), num2.scale()); // 确定最大小数位数
        BigInteger numerator = num1.scaleByPowerOfTen(scale).toBigInteger(); // 放大并转为BigInteger
        BigInteger denominator = num2.scaleByPowerOfTen(scale).toBigInteger();
        BigInteger gcd = numerator.gcd(denominator); // 计算GCD

        // 4. 简化比例
        // 将GCD转回BigDecimal，并除以相同的缩放因子
        BigDecimal gcdDecimal = new BigDecimal(gcd).scaleByPowerOfTen(-scale); // 注意这里是负指数，表示缩小
        BigDecimal simplifiedNum1 =CommonUtil.divisionWithoutDenominatorZeroWithScale(num1,gcdDecimal,resultScale); // 除法，此处应可整除故无需舍入
        BigDecimal simplifiedNum2 =CommonUtil.divisionWithoutDenominatorZeroWithScale(num2,gcdDecimal,resultScale);
        return new BigDecimal[]{simplifiedNum1, simplifiedNum2};
    }


    //当前时间是否大于目前时间的n点
    public static boolean isAfterTagetDateHour(Date inputeDate,Date targetDate,int hour){
        // 将传入的Date对象转换为LocalDateTime
        LocalDateTime inputDateTime = inputeDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // 直接将时间设置为当天的指定小时
        LocalDateTime targetTimeToday = targetDate.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate().atTime(hour, 0);

        // 比较输入时间是否严格在当天指定小时之后
        return inputDateTime.isAfter(targetTimeToday);
    }

    public static Date validDateFormat(String date,String format){
        if (StrUtil.isBlank(date)){
            return null;
        }
        try {
            return DateUtil.parse(date, format);
        } catch (Exception e) {
            return null;
        }
    }

    // 比较两个BigDecimal
    public static int compareTo(BigDecimal one,BigDecimal two){
        if (one==null){
            one=BigDecimal.ZERO;
        }
        if (two==null){
            two=BigDecimal.ZERO;
        }
        return one.compareTo(two);
    }

    // 是否数字字符串
    public static boolean isNumber(String number){
        if (StrUtil.isBlank(number)){
            return false;
        }
        return  numberRegPattern.matcher(number).matches();
    }

    // BigDecimal保留scale位
    public static BigDecimal getBigDecimalWithScale(BigDecimal num,Integer scale, RoundingMode roundingMode){
        if (num==null){
            return BigDecimal.ZERO;
        }
        return num.setScale(scale,roundingMode);
    }
    // 是否百分比字符串
    public static boolean isPercentage(String percent) {
        if (StrUtil.isBlank(percent)){
            return false;
        }
        // 正则表达式匹配百分比格式
        return percentRegPattern.matcher(percent).matches();
    }
    // 对象转map
    public static List<Map<String,Object>> toMapList(List<?> list){
        if (CollectionUtil.isEmpty(list)){
            return new ArrayList<>();
        }
       return list.stream().map(BeanUtil::beanToMap).collect(Collectors.toList());
    }

    //BigDecimal转百分比字符串
    public static String bigDecimalToPercent(BigDecimal bigDecimal){
        DecimalFormat decimalFormat=new DecimalFormat("0.00%");
        if (bigDecimal==null){
            return "0.00%";
        }
        return decimalFormat.format(bigDecimal);
    }
    //BigDecimal转百分比字符串
    public static String bigDecimalToPercentDWithScale(BigDecimal bigDecimal,Integer scale){
        String defaultPattern="0.00%";
        if (scale>=0){
            defaultPattern=StrUtil.format("0.{}%",StrUtil.repeat("0",scale));
        }
        DecimalFormat decimalFormat=new DecimalFormat(defaultPattern);
        return decimalFormat.format(bigDecimal);
    }
    //百分比转BigDecimal
    public static BigDecimal percentToBigDecimal(String percent) {
        DecimalFormat decimalFormat=new DecimalFormat("0.00%");
        try {
            return new BigDecimal(decimalFormat.parse(percent).toString());
        } catch (ParseException e) {
            return null;
        }
    }
    //合并map
    public static <K,V> Map<K,V> mergedMap(List<Map<K,V>> mapList){
        if (CollectionUtil.isEmpty(mapList)){
            return new HashMap<>();
        }else {
            return mapList.stream().flatMap(item->item.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }
    //乘法并兼容特殊值
    public static BigDecimal multiply(BigDecimal one,BigDecimal two){
        if (one==null){
            one =  BigDecimal.ZERO;
        }
        if (two==null){
            two = BigDecimal.ZERO;
        }
        return one.multiply(two);
    }
    //减法并兼容特殊值
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

    //加法并兼容特殊值
    public static BigDecimal add(BigDecimal one,BigDecimal two){
        if (one==null){
            one =  BigDecimal.ZERO;
        }
        if (two==null){
            two = BigDecimal.ZERO;
        }
        return one.add(two);
    }

    // 除法并兼容特殊值
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

    // 除法并兼容特殊值
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

    //分割数字
    public static List<Integer> splitNumber(int number){
        List<Integer> list = new LinkedList<>();
        for (int i = number; i > 0; i /= 10) {
            list.add(i % 10);
        }
        return CollectionUtil.reverse(list);
    }

    // 生成字母版本
    public static String generateAlphabeticalVersion(int index) {
        if (index < 0 || index > 25) {
            throw new IllegalArgumentException("Index must be between 0 and 25");
        }
        return String.valueOf((char) ('A' + index));
    }

    // 更新字母版本
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

    //获取行列的key
    public static String getRowColKey(Integer rowIndex, Integer colIndex){
        return StrUtil.format("{}_{}",rowIndex,colIndex);
    }

    //初始化Date的时间
    public static Date initTimeWithDate(Date date, Integer hour, Integer minute, Integer second,Integer millisecond){
        Calendar calendar = DateUtil.calendar(date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        return calendar.getTime();
    }

    //数据处理NULL值并填充默认值
    public static  <T> void validDataNullAndFill(Object data, Class<T> clazz, List<String> ignoreFieldList){
        if (data==null){
            return;
        }
        if (clazz==null){
            return;
        }
        Field[] fields = ReflectUtil.getFields(clazz);
        if (ignoreFieldList==null){
            ignoreFieldList=new ArrayList<>();
        }
        try {
            if (fields!=null){
                for (Field field:fields){
                    field.setAccessible(true);
                    if (ignoreFieldList.contains(field.getName())){
                        continue;
                    }
                    Object value=field.get(data);
                    Class<?> type = field.getType();
                    if (value==null){
                        if (type.equals(String.class)){
                            field.set(data,"");
                        }
                        if (type.equals(Integer.class)){
                            field.set(data,0);
                        }
                        if (type.equals(Date.class)){
                            field.set(data,new Date());
                        }
                        if (type.equals(BigDecimal.class)){
                            field.set(data,BigDecimal.ZERO);
                        }
                    }
                }
            }
        }catch (Exception e){
            throw new CommonException("数据处理NULL值并填充默认值操作异常");
        }
    }


    //对象属性字符串去除前后空格
    public static <T> void objStrTrim(T data, Class<T> clazz) {
        if (data==null){
            return;
        }
        if (clazz==null){
            return;
        }
        Field[] fields = ReflectUtil.getFields(clazz);
        try {
            if (fields!=null){
                for (Field field:fields){
                    field.setAccessible(true);
                    Object value=field.get(data);
                    if (value!=null){
                        if (value instanceof String){
                            String valueStr= StrUtil.trim(value.toString());
                            field.set(data,valueStr);
                        }
                    }
                }
            }
        }catch (Exception e){
            throw new CommonException("对象属性字符串去除前后空格操作异常"+e.getMessage());
        }
    }
}
