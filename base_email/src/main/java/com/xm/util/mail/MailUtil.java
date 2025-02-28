package com.xm.util.mail;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.annotation.Comment;
import com.xm.configuration.mail.MailProperty;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.mail.convert.MailTableValueConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.CollectionUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class MailUtil {

    public static <T> List<ModelAndTitleMailMapping> generationFieldNameAndTitleMapping(Class<T> clazz, Map<String, MailTableValueConvert> convertMap){
        Field[] declaredFields=clazz.getDeclaredFields();
        List<ModelAndTitleMailMapping> fieldNameAndTitleMappingList=new ArrayList<>();
        for (Field field:declaredFields){
            if (field.isAnnotationPresent(Comment.class)) {
                ModelAndTitleMailMapping fieldNameAndTitleMapping=new ModelAndTitleMailMapping();

                //获取注解里面的列名
                Comment comment = field.getDeclaredAnnotation(Comment.class);
                String title = comment.value();

                fieldNameAndTitleMapping.setField(field);
                fieldNameAndTitleMapping.setTitle(title);

                if (CollectionUtil.isNotEmpty(convertMap)){
                    fieldNameAndTitleMapping.setMailTableValueConvert(convertMap.get(field.getName()));
                }

                fieldNameAndTitleMappingList.add(fieldNameAndTitleMapping);
            }
        }
        return fieldNameAndTitleMappingList;
    }

    public static <T> String getTableTextByCommentAnnotation(List<ModelAndTitleMailMapping> fieldNameAndTitleMappingList, List<T> dataList) throws Exception {
        if (CollectionUtil.isEmpty(dataList)){
            log.error("[生成表格]数据不能为空");
            return null;
        }
        if (CollectionUtils.isEmpty(fieldNameAndTitleMappingList)){
            log.error("[生成表格]映射不能为空");
            return null;
        }

        if (CollectionUtil.isEmpty(fieldNameAndTitleMappingList)){
            log.error("[生成表格]列名不能为空");
            return null;
        }

        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("<table border=\"1\"; cellspacing=\"0\" cellpadding=\"5\">");
        //设置表头
        stringBuilder.append("<tr>");
        for (ModelAndTitleMailMapping mapping:fieldNameAndTitleMappingList){
            stringBuilder.append("<th>");
            stringBuilder.append(mapping.getTitle());
            stringBuilder.append("</th>");
        }
        stringBuilder.append("</tr>");


        //设置数据
        for (T data:dataList){
            stringBuilder.append("<tr>");
            for (ModelAndTitleMailMapping mapping:fieldNameAndTitleMappingList){
                stringBuilder.append("<td>");
                mapping.getField().setAccessible(true);
                Object value=mapping.getField().get(data);
                MailTableValueConvert convert=mapping.getMailTableValueConvert();
                if (convert!=null){
                    stringBuilder.append(convert.convert(value));
                }else {
                    if (value==null){
                        value="";
                    }
                    stringBuilder.append(value);
                }
                stringBuilder.append("</td>");
            }
            stringBuilder.append("</tr>");
        }



        stringBuilder.append("</table>");

        return stringBuilder.toString();
    }


    public static String getTableText(List<String> colNames, List<List<String>> data){
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("<table border=\"1\"; cellspacing=\"0\" cellpadding=\"5\">");
        if (CollectionUtils.isEmpty(colNames)){
            log.error("[生成表格]列名不能为空");
            return null;
        }
        if (CollectionUtils.isEmpty(data)){
            log.error("[生成表格]数据不能为空");
            return null;
        }
        //设置表头
        stringBuilder.append("<tr>");
        for (String colName:colNames){
            stringBuilder.append("<th>");
            stringBuilder.append(colName);
            stringBuilder.append("</th>");
        }
        stringBuilder.append("</tr>");

        //设置数据
        for (List<String> list:data){
            if (CollectionUtils.isEmpty(list)){
                continue;
            }
            stringBuilder.append("<tr>");
            for (String item:list){
                stringBuilder.append("<td>");
                stringBuilder.append(item);
                stringBuilder.append("</td>");
            }
            stringBuilder.append("</tr>");
        }

        stringBuilder.append("</table>");

        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        List<String> colNames=new ArrayList<>();
        colNames.add("A");
        colNames.add("B");
        colNames.add("C");
        List<List<String>> datas=new ArrayList<>();
        for (int i=0;i<20;i++){
            List<String> data=new ArrayList<>();
            data.add("1");
            data.add("2");
            data.add("3");
            datas.add(data);
        }
        System.out.println(MailUtil.getTableText(colNames,datas));
    }
}
