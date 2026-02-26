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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileListJsonUtil {

    public static List<TcFile> getFileListByJson(String fileListJson){
        if (StrUtil.isBlank(fileListJson)){
            return new ArrayList<>();
        }
        return JSONUtil.toList(fileListJson,TcFile.class);
    }


    //删除所有文件
    public static void executeDeleteAllFile(String fileListJson){
        if (StrUtil.isNotBlank(fileListJson)) {
            List<TcFile> fileList = getFileListByJson(fileListJson);
            List<String> deleteFileIdList = fileList.stream().map(TcFile::getId).collect(Collectors.toList());
            FileOptionUtil.deleteFileBatch(deleteFileIdList);
        }
    }

    //对比新旧文件列表，返回需要删除的文件ID
    private static List<String> calDeleteFileList(String newFileListJson,String oldFileListJson){
        List<TcFile> newFileList;
        List<TcFile> oldFileList;
        if (StrUtil.isBlank(newFileListJson)){
            newFileList=new ArrayList<>();
        }else {
            newFileList= getFileListByJson(newFileListJson);
        }
        if (StrUtil.isBlank(oldFileListJson)){
            oldFileList=new ArrayList<>();
        }else {
            oldFileList= getFileListByJson(oldFileListJson);
        }
        Map<String, TcFile> newMap = newFileList.stream().collect(Collectors.toMap(TcFile::getId, Function.identity()));
        //旧对新取差集就是删除
        return oldFileList.stream().
                map(TcFile::getId)
                .filter(id -> !newMap.containsKey(id))
                .collect(Collectors.toList());
    }

    //对比新旧文件列表，返回需要删除的文件ID，文件未上传，上传文件之后执行映射操作
    public static String executeUploadFileWithOldFileListJson(String newFileListJson,String oldFileListJson,
                                                              Map<String, UploadFileWithStream> uploadFileWithStreamMap){
        return executeUploadFileWithDeleteFileIdList(newFileListJson,calDeleteFileList(newFileListJson, oldFileListJson),uploadFileWithStreamMap);
    }

    //对比新旧文件列表，返回需要删除的文件ID，文件已上传，执行映射操作
    public static String executeMappingFileWithOldFileListJson(String newFileListJson,String oldFileListJson,
                                                              Map<String, String> tempIdAndFileIdMap){
        return executeMappingFileWithDeleteFileIdList(newFileListJson,calDeleteFileList(newFileListJson, oldFileListJson),tempIdAndFileIdMap);
    }

    //带删除id列表，文件已上传，执行映射操作
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
        List<TcFile> fileList = getFileListByJson(fileListJson);
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

    //带删除id列表，文件未上传，上传文件之后执行映射操作（不带映射表）
    public static String executeUploadFileWithDeleteFileIdList(String fileListJson,
                                                         List<String> deleteFileIdList,
                                                               Map<String, UploadFileWithStream> uploadFileWithStreamMap) {
        UploadFileListJsonResult uploadFileListJsonResult = executeUploadFileWithDeleteFileIdListResult(fileListJson, deleteFileIdList, uploadFileWithStreamMap);
        return uploadFileListJsonResult.getFileListJson();
    }

    //带删除id列表，文件未上传，上传文件之后执行映射操作（带映射表）
    public static UploadFileListJsonResult executeUploadFileWithDeleteFileIdListResult(String fileListJson,
                                                                                 List<String> deleteFileIdList,
                                                                                 Map<String, UploadFileWithStream> uploadFileWithStreamMap) {
        UploadFileListJsonResult fileListJsonResult = new UploadFileListJsonResult();
        Map<String, String> tempIdAndFileIdMap=new HashMap<>();
        //删除文件
        if (CollectionUtil.isNotEmpty(deleteFileIdList)) {
            FileOptionUtil.deleteFileBatch(deleteFileIdList);
        }
        //上传文件
        if (StrUtil.isBlank(fileListJson)) {
            fileListJsonResult.setFileListJson("[]");
            fileListJsonResult.setTempIdAndFileIdMap(tempIdAndFileIdMap);
            return fileListJsonResult;
        }
        if (CollectionUtil.isEmpty(uploadFileWithStreamMap)) {
            fileListJsonResult.setFileListJson(fileListJson);
            fileListJsonResult.setTempIdAndFileIdMap(tempIdAndFileIdMap);
            return fileListJsonResult;
        }
        List<TcFile> fileList = getFileListByJson(fileListJson);
        List<JSONObject> resultList = new ArrayList<>();
        for (TcFile item : fileList) {
            JSONObject result = new JSONObject();
            TcFile tcFile;
            if (item.getId().startsWith("temp_")) {
                String tempId = item.getId();
                UploadFileWithStream uploadFileWithStream = uploadFileWithStreamMap.get(tempId);
                if (uploadFileWithStream == null) {
                    continue;
                }
                //名称不为空，则使用已修改的名称
                if (StrUtil.isNotBlank(item.getOriginalFileName())){
                    uploadFileWithStream.setFileName(item.getOriginalFileName());
                }
                tcFile = FileOptionUtil.uploadByUploadFileWithStream(uploadFileWithStream);
                tempIdAndFileIdMap.put(tempId, tcFile.getId());
            } else {
                tcFile = item;
            }
            result.set("id",tcFile.getId());
            result.set("originalFileName",tcFile.getOriginalFileName());
            resultList.add(result);
        }

        fileListJsonResult.setFileListJson(JSONUtil.toJsonStr(resultList));
        fileListJsonResult.setTempIdAndFileIdMap(tempIdAndFileIdMap);
        return fileListJsonResult;
    }
}
