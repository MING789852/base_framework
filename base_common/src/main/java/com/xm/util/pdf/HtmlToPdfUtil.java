package com.xm.util.pdf;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.configuration.pdf.HtmlToPdfEnv;
import com.xm.util.bean.SpringBeanUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Date;

@Slf4j
public class HtmlToPdfUtil {

    private final static Configuration configuration;

    private final static HtmlToPdfEnv defaultEnv;

    static {
        configuration = SpringBeanUtil.getBeanByName("classPathResourceFreemarkerConfiguration",Configuration.class);
        defaultEnv = SpringBeanUtil.getBeanByClass(HtmlToPdfEnv.class);
        initEnv(defaultEnv);
    }
    public static HtmlToPdfResult convertToPdf(String ftlName, Object dataModel,String outPutPdfName){
        return convertToPdf(ftlName, dataModel, outPutPdfName,defaultEnv,false);
    }

    public static void initEnv(HtmlToPdfEnv env){
        String htmlTempPath = env.getHtmlTempPath();
        String pdfPath = env.getPdfPath();
        File htmlDir=new File(htmlTempPath);
        if (!htmlDir.exists()){
            if (!htmlDir.mkdirs()){
                String msg= StrUtil.format("创建html临时文件失败->{}",htmlTempPath);
                throw new CommonException(msg);
            }
        }
        File pdfDir=new File(pdfPath);
        if (!pdfDir.exists()){
            if (!pdfDir.mkdirs()){
                String msg=StrUtil.format("创建pdf临时文件失败->{}",pdfPath);
                throw new CommonException(msg);
            }
        }
    }

    public static void releaseHtmlToPdfResult(HtmlToPdfResult result){
        if (result!=null&&StrUtil.isNotBlank(result.getPdfFilePath())){
            FileUtil.del(result.getPdfFilePath());
        }
    }

    public static HtmlToPdfResult convertToPdf(String ftlName, Object dataModel,String outPutPdfName,HtmlToPdfEnv env,boolean initEnvFlag){
        if (initEnvFlag){
            initEnv(env);
        }
        try {
            Date now=new Date();
            String formatDate = DateUtil.format(now, "yyyyMMdd");
            String pdfTempFilePath=env.getPdfPath() + outPutPdfName+".pdf";
            String htmlTempFilePath=env.getHtmlTempPath() + formatDate + "_" + IdUtil.fastSimpleUUID()+".html";
            Template template = configuration.getTemplate(ftlName);
            File htmlTempFile = new File(htmlTempFilePath);
            if (!htmlTempFile.exists()){
                boolean newFile = htmlTempFile.createNewFile();
                if (!newFile){
                    throw new CommonException("html临时文件创建失败");
                }
            }
            try (FileWriter fileWriter = new FileWriter(htmlTempFile)) {
                template.process(dataModel,fileWriter);
            }
            String wkhtmltopdfCmd = env.getWkHtmlToPdfBinPath();
            ProcessBuilder processBuilder=new ProcessBuilder(wkhtmltopdfCmd,htmlTempFile.getAbsolutePath(),pdfTempFilePath);
            Process process = processBuilder.start();
            log(process.getInputStream(), process.getErrorStream());
            int exitCode = process.waitFor();
            if (exitCode!=0){
                throw new CommonException("转换失败,exitCode非0");
            }
            if (htmlTempFile.delete()){
                log.info("html临时文件->{}删除成功",htmlTempFile.getAbsoluteFile());
            }
            HtmlToPdfResult result=new HtmlToPdfResult();
            result.setPdfFilePath(pdfTempFilePath);
            result.setInputStream(FileUtil.getInputStream(pdfTempFilePath));
            return result;
        } catch (Exception e) {
            String msg= StrUtil.format("转换html失败->{}", ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        }
    }

    private static void log(InputStream inputStream, InputStream errorStream) {
        HtmlToPdfLogInterceptor output = new HtmlToPdfLogInterceptor(inputStream);
        HtmlToPdfLogInterceptor error = new HtmlToPdfLogInterceptor(errorStream);
        ThreadPoolTaskExecutor executor = SpringBeanUtil.getBeanByClass(ThreadPoolTaskExecutor.class);
        executor.execute(output);
        executor.execute(error);
    }
}
