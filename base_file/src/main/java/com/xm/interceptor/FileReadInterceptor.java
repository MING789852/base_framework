package com.xm.interceptor;

import java.util.List;

//文件读取拦截
public interface FileReadInterceptor {
    //禁用检查文件授权状态
    boolean disableCheckAuthCertificate(List<String> fileIdList);
    //读取文件(核心限制)
    boolean allowRead(List<String> fileIdList);
}
