package com.xm.util.zip;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class CompressUtil {

    public static void compressZip(List<CompressZipInputStreamEntry> compressZipInputStreamEntryList, OutputStream zipTargetOutputStream){
        if (CollectionUtil.isEmpty(compressZipInputStreamEntryList)){
            return;
        }
        ZipOutputStream zipOutputStream=null;
        try {
            zipOutputStream=new ZipOutputStream(zipTargetOutputStream);
            for (CompressZipInputStreamEntry compressZipEntry:compressZipInputStreamEntryList){
                //写入文件名
                ZipEntry zipEntry=new ZipEntry(compressZipEntry.getName());
                zipOutputStream.putNextEntry(zipEntry);
                //每次写入5MB
                InputStream inputStream = compressZipEntry.getInputStream();
                byte[] bytes = new byte[5*1024*1024];
                int len;
                while ((len=inputStream.read(bytes))!=-1) {
                    zipOutputStream.write(bytes,0,len);
                }
                inputStream.close();
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
        }catch (Exception e){
            String msg= StrUtil.format("压缩ZIP失败->{}", ExceptionUtil.stacktraceToString(e));
            throw new CommonException(msg);
        }
    }

    public static byte[] compressZip(List<CompressZipBytesEntry> compressZipBytesEntryList) {
        if (CollectionUtil.isEmpty(compressZipBytesEntryList)){
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream=null;
        ZipOutputStream zipOutputStream=null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            zipOutputStream=new ZipOutputStream(byteArrayOutputStream);
            for (CompressZipBytesEntry compressZipEntry:compressZipBytesEntryList){
                ZipEntry zipEntry=new ZipEntry(compressZipEntry.getName());
                zipOutputStream.putNextEntry(zipEntry);
                byte[] bytes = compressZipEntry.getBytes();
                zipOutputStream.write(bytes);
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            log.info("压缩后大小->{}字节",byteArray.length);
            return byteArray;
        }catch (Exception e){
            String msg= StrUtil.format("压缩ZIP失败->{}", ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        }finally {
            try {
                if (zipOutputStream!=null){
                    zipOutputStream.flush();
                    zipOutputStream.close();
                }
                if (byteArrayOutputStream!=null){
                    byteArrayOutputStream.flush();
                    byteArrayOutputStream.close();
                }
            }catch (Exception e){
                String msg= StrUtil.format("关闭ZIP流失败->{}", ExceptionUtil.stacktraceToString(e));
                log.error(msg);
            }
        }
    }
}
