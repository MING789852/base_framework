package com.xm.file.service;

import com.xm.file.domain.dto.TcFileWithByteArray;
import com.xm.file.domain.dto.TcFileWithInputStream;
import com.xm.file.domain.dto.UploadFileWithStream;
import com.xm.file.domain.entity.TcFile;
import com.xm.file.domain.params.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface FileOption {
    List<TcFile> uploadBatchByUploadFileWithStream(List<UploadFileWithStream> fileWithStreamList);

    TcFile uploadByUploadFileWithStream(UploadFileWithStream uploadFileWithStream);

    List<TcFileWithInputStream> readFileInputStreamByIdList(List<String> idList);

    List<TcFileWithByteArray> readFileByteArrayByIdList(List<String> idList);

    TcFileWithByteArray readFileByteArrayById(String id);

    TcFile deleteFile(String id);


    TcFile realDeleteFile(String id);


    void viewFile(String id, HttpServletResponse response);

    void viewFileWithCacheControl(String id, HttpServletResponse response);

    void downloadFile(String id, HttpServletResponse response);


    Map<String, TcFile> getIdAndFileMapping(List<String> idList);


    List<TcFile> getFileInfoByIdList(List<String> idList);

    TcFile getFileInfoById(String id);

    CreateChunkUploadResult createChunkUpload(CreateChunkUploadParams params);

    UploadChunkResult uploadChunk(UploadChunkParams params,byte[] bytes);

    TcFile mergeChunk(MergeChunkParams params);
}
