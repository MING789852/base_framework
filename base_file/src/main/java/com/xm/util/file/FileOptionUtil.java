package com.xm.util.file;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.configuration.file.FileProperty;
import com.xm.file.domain.dto.TcFileWithByteArray;
import com.xm.file.domain.dto.TcFileWithInputStream;
import com.xm.file.domain.dto.UploadFileWithStream;
import com.xm.file.domain.entity.TcFile;
import com.xm.file.domain.params.*;
import com.xm.file.service.FileOption;
import com.xm.util.bean.SpringBeanUtil;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileOptionUtil {

    private static FileOption fileOption;

    static {
        init();
    }

    private static void init() {
        FileProperty fileProperty = SpringBeanUtil.getBeanByClass(FileProperty.class);
        fileOption = SpringBeanUtil.getBeanByName(fileProperty.getType() + "-file-option", FileOption.class);
    }

    public static TcFile uploadByMultipartFile(MultipartFile file) {
        if (file == null) {
            return null;
        }
        UploadFileWithStream uploadFileWithStream = convertUploadFileWithStreamByMultipartFile(file);

        return fileOption.uploadByUploadFileWithStream(uploadFileWithStream);
    }

    public static UploadFileWithStream convertUploadFileWithStreamByMultipartFile(MultipartFile multipartFile) {
        try {
            UploadFileWithStream uploadFileWithStream = new UploadFileWithStream();
            InputStream inputStream = multipartFile.getInputStream();
            uploadFileWithStream.setInputStream(inputStream);
            uploadFileWithStream.setFileName(multipartFile.getOriginalFilename());
            uploadFileWithStream.setFileSize(multipartFile.getSize());
            return uploadFileWithStream;
        } catch (IOException e) {
            String msg = StrUtil.format("MultipartFile转换UploadFileWithStream失败->{}", ExceptionUtil.stacktraceToString(e));
            throw new CommonException(msg);
        }
    }

    public static Map<String, UploadFileWithStream> convertUploadFileMap(Map<String, MultipartFile> fileMap) {
        if (fileMap == null) {
            fileMap = new HashMap<>();
        }
        return fileMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        item -> FileOptionUtil.convertUploadFileWithStreamByMultipartFile(item.getValue())));
    }

    public static TcFile uploadByUploadFileWithStream(UploadFileWithStream uploadFileWithStream) {
        return fileOption.uploadByUploadFileWithStream(uploadFileWithStream);
    }

    public static List<TcFile> uploadBatchByUploadFileWithStream(List<UploadFileWithStream> fileWithStreamList) {
        return fileOption.uploadBatchByUploadFileWithStream(fileWithStreamList);
    }

    public static TcFileWithByteArray readFileByteArrayById(String id) {
        return fileOption.readFileByteArrayById(id);
    }

    public static List<TcFileWithInputStream> readFileInputStreamByIdList(List<String> idList) {
        return fileOption.readFileInputStreamByIdList(idList);
    }

    public static void viewFile(String id, HttpServletResponse response) {
        try {
            fileOption.viewFile(id, response);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    public static void viewFileWithCacheControl(String id, HttpServletResponse response) {
        try {
            fileOption.viewFileWithCacheControl(id, response);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    public static void downloadFile(String id, HttpServletResponse response) {
        fileOption.downloadFile(id, response);
    }

    public static Map<String, TcFile> getIdAndFileMapping(List<String> idList) {
        return fileOption.getIdAndFileMapping(idList);
    }

    public static List<TcFile> getFileInfoByIdList(List<String> idList) {
        return fileOption.getFileInfoByIdList(idList);
    }

    public static TcFile getFileInfoById(String id) {
        return fileOption.getFileInfoById(id);
    }


    public static TcFile deleteFile(String id) {
        return fileOption.deleteFile(id);
    }

    public static void deleteFileBatch(List<String> idList) {
        if (CollectionUtil.isNotEmpty(idList)) {
            for (String id : idList) {
                deleteFile(id);
            }
        }
    }

    public static CreateChunkUploadResult createChunkUpload(CreateChunkUploadParams params) {
        return fileOption.createChunkUpload(params);
    }

    public static UploadChunkResult uploadChunk(UploadChunkParams params, byte[] bytes) {
        return fileOption.uploadChunk(params, bytes);
    }

    public static TcFile mergeChunk(MergeChunkParams params) {
        return fileOption.mergeChunk(params);
    }

    public static TcFile realDeleteFile(String id) {
        return fileOption.realDeleteFile(id);
    }
}
