package com.xm.form.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.file.domain.dto.UploadFileWithStream;
import com.xm.form.domain.entity.TcFormFile;

import java.util.List;
import java.util.Map;

public interface TcFormFileService {
    void saveOrUpdate(String moduleName, String refId, String refField
            , Map<String, UploadFileWithStream> uploadFileWithStreamMap, List<TcFormFile> fileList);
    void saveOrUpdateWithExistsFileMap(String moduleName, String refId, String refField
            , Map<String, String> tempIdAndFileIdMap, List<TcFormFile> fileList);
    void delete(String moduleName, String refId,String refField);

    List<TcFormFile> getFormFileList(String moduleName, String refId, String refField);

    List<TcFormFile> getFormFileList(LambdaQueryWrapper<TcFormFile> lambdaQueryWrapper);
}
