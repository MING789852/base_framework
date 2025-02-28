package com.xm.interceptor;

import com.xm.fileAuth.domain.dto.AuthApplyFileDto;

//文件授权拦截器
public interface FileAuthInterceptor {
    void  before(AuthApplyFileDto authApplyFileDto);
    void  after(AuthApplyFileDto authApplyFileDto);
}
