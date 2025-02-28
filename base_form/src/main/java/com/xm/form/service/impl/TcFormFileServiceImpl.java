package com.xm.form.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.file.domain.dto.UploadFileWithStream;
import com.xm.file.domain.entity.TcFile;
import com.xm.form.domain.entity.TcFormFile;
import com.xm.form.mapper.TcFormFileMapper;
import com.xm.form.service.TcFormFileService;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.id.SnowIdUtil;
import com.xm.util.file.FileOptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TcFormFileServiceImpl implements TcFormFileService {

    private final TcFormFileMapper formFileMapper;


    //type
    //1.临时文件上传
    //2.关联已上传文件
    private void saveOrUpdateByType(Integer uploadType,String moduleName, String refId,String refField
            , Map<String, UploadFileWithStream> uploadFileWithStreamMap,Map<String, String> tempIdAndFileIdMap, List<TcFormFile> fileList){
        if (moduleName==null){
            throw new CommonException("[文件]模块为空");
        }
        if (StrUtil.isBlank(refId)){
            throw new CommonException("[文件]业务id为空");
        }
        if (StrUtil.isBlank(refField)){
            throw new CommonException("[文件]文件字段为空");
        }
        LambdaQueryWrapper<TcFormFile> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TcFormFile::getRefId,refId)
                .eq(TcFormFile::getRefType,moduleName)
                .eq(TcFormFile::getRefField,refField);
        List<TcFormFile> databaseList = formFileMapper.selectList(lambdaQueryWrapper);
        if (CollectionUtil.isEmpty(fileList)){
            //文件为空则直接全部删除
            delete(databaseList);
        }else {
            Map<String, TcFormFile> databaseMap = databaseList.stream().collect(Collectors.toMap(TcFormFile::getId, Function.identity()));

            List<String> newIdList=new ArrayList<>();
            Date now=new Date();
            TcUser currentLoginUserBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
            if (currentLoginUserBySession==null){
                throw new CommonException("未登录，无法操作文件");
            }
            //上传文件
            for (TcFormFile formFile:fileList){
                //如果是新文件则上传
                TcFile tcFile=null;
                if (uploadType == 1 ){
                    UploadFileWithStream uploadFileWithStream = uploadFileWithStreamMap.get(formFile.getFileId());
                    if(uploadFileWithStream!=null){
                        tcFile = FileOptionUtil.uploadByUploadFileWithStream(uploadFileWithStream);
                    }
                }
                if (uploadType == 2){
                    String fileId = tempIdAndFileIdMap.get(formFile.getFileId());
                    tcFile=FileOptionUtil.getFileInfoById(fileId);
                }
                if (tcFile!=null){
                    formFile.setId(SnowIdUtil.getSnowId());
                    formFile.setFileId(tcFile.getId());
                    formFile.setFileType(tcFile.getExtName());
                    formFile.setFileSize(tcFile.getFileSize());
                    formFile.setFileName(tcFile.getOriginalFileName());
                    formFile.setRefField(refField);
                    formFile.setRefId(refId);
                    formFile.setRefType(moduleName);
                    formFile.setCreateDate(now);
                    formFile.setCreateUser(currentLoginUserBySession.getNickName());
                    formFile.setCreateUserId(currentLoginUserBySession.getId());
                    formFileMapper.insert(formFile);
                }

                newIdList.add(formFile.getId());
            }
            //数据库取差集获取删除文件
            List<TcFormFile> deletePatentFileList = databaseMap.entrySet().stream()
                    .filter(item -> !newIdList.contains(item.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
            delete(deletePatentFileList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(String moduleName, String refId,String refField
            , Map<String, UploadFileWithStream> uploadFileWithStreamMap, List<TcFormFile> fileList) {
        saveOrUpdateByType(1,moduleName,refId,refField,uploadFileWithStreamMap,null,fileList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateWithExistsFileMap(String moduleName, String refId, String refField, Map<String, String> tempIdAndFileIdMap, List<TcFormFile> fileList) {
        saveOrUpdateByType(2,moduleName,refId,refField,null,tempIdAndFileIdMap,fileList);
    }

    @Override
    public void delete(String moduleName, String refId,String refField) {
        LambdaQueryWrapper<TcFormFile> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TcFormFile::getRefId,refId)
                .eq(TcFormFile::getRefType,moduleName)
                .eq(TcFormFile::getRefField,refField);
        List<TcFormFile> databaseList = formFileMapper.selectList(lambdaQueryWrapper);
        delete(databaseList);
    }

    @Override
    public List<TcFormFile> getFormFileList(String moduleName, String refId, String refField) {
        LambdaQueryWrapper<TcFormFile> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TcFormFile::getRefId,refId)
                .eq(TcFormFile::getRefType,moduleName)
                .eq(TcFormFile::getRefField,refField);
        return formFileMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    public List<TcFormFile> getFormFileList(LambdaQueryWrapper<TcFormFile> lambdaQueryWrapper) {
        return formFileMapper.selectList(lambdaQueryWrapper);
    }


    private void delete(List<TcFormFile> deletePatentFileList){
        if (CollectionUtil.isNotEmpty(deletePatentFileList)){
            List<String> deletePatentFileIdList = deletePatentFileList.stream().map(TcFormFile::getId).collect(Collectors.toList());
            LambdaQueryWrapper<TcFormFile> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.in(TcFormFile::getId,deletePatentFileIdList);
            formFileMapper.delete(lambdaQueryWrapper);

            List<String> deleteFileIdList = deletePatentFileList.stream().map(TcFormFile::getFileId).collect(Collectors.toList());
            FileOptionUtil.deleteFileBatch(deleteFileIdList);
        }
    }
}
