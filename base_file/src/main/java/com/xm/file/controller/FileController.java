package com.xm.file.controller;

import cn.hutool.json.JSONUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.core.params.Result;
import com.xm.file.domain.dto.UploadFileWithStream;
import com.xm.file.domain.entity.TcFile;
import com.xm.file.domain.params.*;
import com.xm.util.file.FileOptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("file")
@RequiredArgsConstructor
public class FileController {

    @GetMapping({"view","mini/view"})
    public void view(@RequestParam("id")String id, HttpServletResponse response){
        FileOptionUtil.viewFile(id, response);
    }

    @GetMapping("download")
    public void download(@RequestParam("id")String id, HttpServletResponse response){
        FileOptionUtil.downloadFile(id, response);
    }

//    @GetMapping("realDeleteFile")
//    public Result<TcFile> realDeleteFile(@RequestParam("id")String id){
//        return Result.successForData(FileOptionUtil.realDeleteFile(id));
//    }

    @PostMapping("getIdAndFileMapping")
    public Map<String, TcFile> getIdAndFileMapping(@RequestBody List<String> idList){
        return FileOptionUtil.getIdAndFileMapping(idList);
    }

    @PostMapping("getFileInfoByIdList")
    public Result<List<TcFile>> getFileInfoByIdList(@RequestBody List<String> idList){
        return Result.successForData(FileOptionUtil.getFileInfoByIdList(idList));
    }


    @PostMapping("uploadFile")
    public Result<TcFile> uploadFile(MultipartHttpServletRequest request){
        String data = request.getParameter("data");
        UploadFileParams params= JSONUtil.toBean(data,UploadFileParams.class);
        MultipartFile multipartFile=request.getFile("file");
        if (multipartFile==null){
            throw new CommonException("上传文件为空");
        }
        UploadFileWithStream uploadFileWithStream=new UploadFileWithStream();
        uploadFileWithStream.setFileName(params.getFileName());
        uploadFileWithStream.setFileSize(params.getFileSize());
        try {
            uploadFileWithStream.setInputStream(multipartFile.getInputStream());
            return Result.successForData(FileOptionUtil.uploadByUploadFileWithStream(uploadFileWithStream));
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }


    @PostMapping("createChunkUpload")
    public Result<CreateChunkUploadResult> createChunkUpload(@RequestBody CreateChunkUploadParams params){
        return Result.successForData(FileOptionUtil.createChunkUpload(params));
    }

    @PostMapping("uploadChunk")
    public Result<UploadChunkResult> uploadChunk(MultipartHttpServletRequest request){
        String data = request.getParameter("data");
        UploadChunkParams params= JSONUtil.toBean(data,UploadChunkParams.class);
        MultipartFile multipartFile=request.getFile("file");
        if (multipartFile==null){
            throw new CommonException("上传文件为空");
        }
        try {
            return Result.successForData(FileOptionUtil.uploadChunk(params,multipartFile.getBytes()));
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @PostMapping("mergeChunk")
    public Result<TcFile> mergeChunk(@RequestBody MergeChunkParams params){
        return Result.successForData(FileOptionUtil.mergeChunk(params));
    }
}
