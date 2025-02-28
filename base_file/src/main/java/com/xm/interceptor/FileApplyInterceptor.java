package com.xm.interceptor;

import com.xm.fileAuth.domain.dto.ApplyFileDto;
import com.xm.fileAuth.domain.entity.TcFileApply;
import com.xm.fileAuth.domain.entity.TcFileApplyDetail;

import java.util.List;

//文件申请拦截器
public interface FileApplyInterceptor {
    void before(ApplyFileDto applyFileDto);
    void after(TcFileApply fileApply, List<TcFileApplyDetail> fileApplyDetailList);
}
