package com.xm.util.smb;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SmbUtil {
    private static final Map<String,SmbInstance> instanceMap=new ConcurrentHashMap<>();

    public static SmbInstance getInstance(String host, String username, String password){
        String key= StrUtil.format("{}_{}",host,username);
        SmbInstance smbInstance=instanceMap.get(key);
        if (smbInstance==null){
            log.info("SMB实例创建->{}",key);
            smbInstance=new SmbInstance(host, username, password);
            instanceMap.put(key,smbInstance);
        }else {
            log.info("SMB实例已存在->{}",key);
        }
        return smbInstance;
    }

    public static void main(String[] args) throws InterruptedException {
//        SmbUtil.getInstance("192.168.1.232","mis","mis").upload("信息部","/","图片1.png", FileUtil.getInputStream("F:\\pcb图\\图片1.png"));
//        SmbUtil.getInstance("192.168.1.232","mis","mis").delete("信息部","/临时文件夹/","图片1.png");
//        while (true){
//
//            SmbUtil.getInstance("10.1.20.200","gs","gs").upload("test","哈哈哈/2222/","图片1.png", FileUtil.getInputStream("F:\\pcb图\\图片1.png"));
//            SmbUtil.getInstance("10.1.20.200","gs","gs").upload("test","哈哈哈/2222/","图片2.png",FileUtil.getInputStream("F:\\pcb图\\图片2.png"));
//            SmbUtil.getInstance("10.1.20.200","gs","gs").upload("test","哈哈哈/2222/","图片3.png",FileUtil.getInputStream("F:\\pcb图\\图片3.png"));
//            SmbUtil.getInstance("10.1.20.200","gs","gs").upload("test","哈哈哈/2222/","图片4.png",FileUtil.getInputStream("F:\\pcb图\\图片4.png"));
//            SmbUtil.getInstance("10.1.20.200","gs","gs").upload("test","哈哈哈/2222/","图片5.png",FileUtil.getInputStream("F:\\pcb图\\图片5.png"));
//            SmbUtil.getInstance("10.1.20.200","gs","gs").upload("test","哈哈哈/2222/","图片6.png",FileUtil.getInputStream("F:\\pcb图\\图片6.png"));
//            SmbUtil.getInstance("10.1.20.200","gs","gs").upload("test","哈哈哈/2222/","图片7.png",FileUtil.getInputStream("F:\\pcb图\\图片7.png"));
//        }
        SmbInstance instance = SmbUtil.getInstance("10.1.20.200", "gs", "gs");
//        instance.list("test","/");
        List<SmbFile> test = instance.list("test", "/test");
//        List<SmbFile> test = instance.list("test", "");
        for (SmbFile smbFile : test) {
            if (smbFile.isDir()){
                continue;
            }
            instance.downloadInputStream(smbFile.getShareName(), smbFile.getPath(), smbFile.getFileName(),inputStream -> {
                String filePath = StrUtil.format("F:/pcb图/2222/{}", smbFile.getFileName());
                FileUtil.writeFromStream(inputStream, filePath);
            });
        }
    }
}
