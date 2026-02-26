package com.xm.util.template;

import com.xm.advice.exception.exception.CommonException;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.id.SnowIdUtil;
import com.xm.util.pdf.HtmlToPdfUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

@Slf4j
public class TemplateUtil {
    private final static Configuration configuration;

    static {
        configuration = SpringBeanUtil.getBeanByName("classPathResourceFreemarkerConfiguration",Configuration.class);
    }

    public static String renderStrByFtl(String ftlName, Object dataModel) throws Exception {
        Template template = configuration.getTemplate(ftlName);
        try (StringWriter stringWriter = new StringWriter()){
            template.process(dataModel,stringWriter);
            return stringWriter.toString();
        }
    }

    public static File renderFileByFtl(String ftlName, String targetFilePath, Object dataModel) throws Exception {
        Template template = configuration.getTemplate(ftlName);
        File temp = new File(targetFilePath);
        if (!temp.exists()){
            boolean newFile = temp.createNewFile();
            if (!newFile){
                throw new CommonException("文件创建失败");
            }
        }
        try (FileWriter fileWriter = new FileWriter(temp)) {
            template.process(dataModel,fileWriter);
        }
        return temp;
    }

    public static String renderStrByStrTemplate(String strTemplate, Object dataModel) throws Exception {
        Template template=new Template(SnowIdUtil.getSnowId(),strTemplate,configuration);
        try (StringWriter stringWriter = new StringWriter()){
            template.process(dataModel,stringWriter);
            return stringWriter.toString();
        }
    }
}
