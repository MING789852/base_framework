package com.xm.fileAuth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.fileAuth.domain.dto.ApplyFileDto;
import com.xm.fileAuth.domain.dto.AuthApplyFileDto;
import com.xm.fileAuth.domain.entity.TcFileApply;
import com.xm.fileAuth.domain.result.FileAuthStatusResult;
import com.xm.fileAuth.domain.vo.TcFileApplyVo;
import com.xm.module.core.params.QueryData;

import java.util.List;

public interface TcFileApplyService {
    String fileApply(ApplyFileDto applyFileDto);

    String fileAuth(AuthApplyFileDto authApplyFileDto);

    FileAuthStatusResult checkFileAuthStatus(List<String> fileIdList);

    Page<TcFileApply> selectByPage(QueryData<TcFileApply> queryData);

    String saveOrUpdateData(List<TcFileApplyVo> list);

    TcFileApplyVo fillData(TcFileApply fileApply);

    String deleteData(List<TcFileApply> list);
}
