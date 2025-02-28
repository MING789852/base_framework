package com.xm.util.file.save;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.file.domain.dto.UploadFileWithStream;
import com.xm.file.domain.entity.TcFile;
import com.xm.util.file.FileOptionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileListJsonUtil {

    public static void executeDeleteAllFile(String fileListJson){
        if (StrUtil.isNotBlank(fileListJson)) {
            List<TcFile> fileList = JSONUtil.toList(fileListJson, TcFile.class);
            List<String> deleteFileIdList = fileList.stream().map(TcFile::getId).collect(Collectors.toList());
            FileOptionUtil.deleteFileBatch(deleteFileIdList);
        }
    }

    private static List<String> calDeleteFileList(String newFileListJson,String oldFileListJson){
        List<TcFile> newFileList;
        List<TcFile> oldFileList;
        if (StrUtil.isBlank(newFileListJson)){
            newFileList=new ArrayList<>();
        }else {
            newFileList=JSONUtil.toList(newFileListJson, TcFile.class);
        }
        if (StrUtil.isBlank(oldFileListJson)){
            oldFileList=new ArrayList<>();
        }else {
            oldFileList=JSONUtil.toList(oldFileListJson,TcFile.class);
        }
        Map<String, TcFile> newMap = newFileList.stream().collect(Collectors.toMap(TcFile::getId, Function.identity()));
        //旧对新取差集就是删除
        return oldFileList.stream().
                map(TcFile::getId)
                .filter(id -> !newMap.containsKey(id))
                .collect(Collectors.toList());
    }

    public static String executeUploadFileWithOldFileListJson(String newFileListJson,String oldFileListJson,
                                                              Map<String, UploadFileWithStream> uploadFileWithStreamMap){
        return executeUploadFileWithDeleteFileIdList(newFileListJson,calDeleteFileList(newFileListJson, oldFileListJson),uploadFileWithStreamMap);
    }

    public static String executeMappingFileWithOldFileListJson(String newFileListJson,String oldFileListJson,
                                                              Map<String, String> tempIdAndFileIdMap){
        return executeMappingFileWithDeleteFileIdList(newFileListJson,calDeleteFileList(newFileListJson, oldFileListJson),tempIdAndFileIdMap);
    }

    public static String executeMappingFileWithDeleteFileIdList(String fileListJson,
                                                                List<String> deleteFileIdList,
                                                                Map<String, String> tempIdAndFileIdMap){
        //删除文件
        if (CollectionUtil.isNotEmpty(deleteFileIdList)) {
            FileOptionUtil.deleteFileBatch(deleteFileIdList);
        }
        //处理文件映射
        if (StrUtil.isBlank(fileListJson)) {
            return "[]";
        }
        List<TcFile> fileList = JSONUtil.toList(fileListJson, TcFile.class);
        List<JSONObject> resultList = new ArrayList<>();
        for (TcFile item : fileList) {
            JSONObject result = new JSONObject();
            TcFile tcFile;
            if (item.getId().startsWith("temp_")) {
                String fileId = tempIdAndFileIdMap.get(item.getId());
                if (fileId == null) {
                    continue;
                }
                tcFile = FileOptionUtil.getFileInfoById(fileId);
                if (tcFile==null){
                    throw new CommonException(StrUtil.format("文件ID->{}不存在",fileId));
                }
            } else {
                tcFile = item;
            }
            result.set("id",tcFile.getId());
            //名称不为空，则使用已修改的名称
            if (StrUtil.isNotBlank(item.getOriginalFileName())){
                result.set("originalFileName",item.getOriginalFileName());
            }else {
                result.set("originalFileName",tcFile.getOriginalFileName());
            }
            resultList.add(result);
        }

        return JSONUtil.toJsonStr(resultList);
    }

    public static String executeUploadFileWithDeleteFileIdList(String fileListJson,
                                                         List<String> deleteFileIdList,
                                                               Map<String, UploadFileWithStream> uploadFileWithStreamMap) {
        //删除文件
        if (CollectionUtil.isNotEmpty(deleteFileIdList)) {
            FileOptionUtil.deleteFileBatch(deleteFileIdList);
        }
        //上传文件
        if (StrUtil.isBlank(fileListJson)) {
            return "[]";
        }
        if (CollectionUtil.isEmpty(uploadFileWithStreamMap)) {
            return fileListJson;
        }
        List<TcFile> fileList = JSONUtil.toList(fileListJson, TcFile.class);
        List<JSONObject> resultList = new ArrayList<>();
        for (TcFile item : fileList) {
            JSONObject result = new JSONObject();
            TcFile tcFile;
            if (item.getId().startsWith("temp_")) {
                UploadFileWithStream uploadFileWithStream = uploadFileWithStreamMap.get(item.getId());
                if (uploadFileWithStream == null) {
                    continue;
                }
                //名称不为空，则使用已修改的名称
                if (StrUtil.isNotBlank(item.getOriginalFileName())){
                    uploadFileWithStream.setFileName(item.getOriginalFileName());
                }
                tcFile = FileOptionUtil.uploadByUploadFileWithStream(uploadFileWithStream);
            } else {
                tcFile = item;
            }
            result.set("id",tcFile.getId());
            result.set("originalFileName",tcFile.getOriginalFileName());
            resultList.add(result);
        }

        return JSONUtil.toJsonStr(resultList);
    }
}
