package com.xm.util.sftp;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SftpUtil {
    private static final Map<String,SftpInstance> instanceMap=new ConcurrentHashMap<>();

    public static SftpInstance getInstance(String host, int port,String username,String password){
        String key=StrUtil.format("{}_{}",host,username);
        SftpInstance sftpInstance = instanceMap.get(key);
        if (sftpInstance==null){
            sftpInstance=new SftpInstance(host, port, username, password);
            instanceMap.put(key,sftpInstance);
        }
        return sftpInstance;
    }
}
