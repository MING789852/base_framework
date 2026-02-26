package com.xm.file.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.configuration.file.FileProperty;
import com.xm.file.domain.dto.TcFileWithByteArray;
import com.xm.file.domain.dto.TcFileWithInputStream;
import com.xm.file.domain.dto.UploadFileWithStream;
import com.xm.file.domain.entity.TcFile;
import com.xm.file.domain.entity.TcFileChunk;
import com.xm.file.domain.params.*;
import com.xm.file.enums.FileStatusEnum;
import com.xm.file.mapper.TcFileChunkMapper;
import com.xm.file.mapper.TcFileMapper;
import com.xm.file.service.FileOption;
import com.xm.interceptor.FileReadInterceptor;
import com.xm.util.file.CommonFileUtil;
import com.xm.util.id.SnowIdUtil;
import com.xm.util.valid.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service("local-file-option")
@RequiredArgsConstructor
@Slf4j
public class LocalFileOption implements FileOption {

    private final TcFileMapper tcFileMapper;

    private final FileProperty fileProperty;

    private final TcFileChunkMapper fileChunkMapper;

    private final List<FileReadInterceptor> fileReadInterceptors;



    private String getFullFilePath(String path){
        if (StrUtil.isBlank(path)){
            return null;
        }else {
            return Paths.get(fileProperty.getPath(),path).toString();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TcFile> uploadBatchByUploadFileWithStream(List<UploadFileWithStream> fileWithStreamList) {
        if (CollectionUtil.isEmpty(fileWithStreamList)) {
            return null;
        }
        List<TcFile> tcFileList = new ArrayList<>();
        for (UploadFileWithStream fileWithStream : fileWithStreamList) {
            tcFileList.add(uploadByUploadFileWithStream(fileWithStream));
        }
        return tcFileList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TcFile uploadByUploadFileWithStream(UploadFileWithStream uploadFileWithStream) {
        try (InputStream inputStream = uploadFileWithStream.getInputStream()) {
            if (inputStream == null) {
                throw new CommonException("文件流为空");
            }
            if (StrUtil.isBlank(uploadFileWithStream.getFileName())) {
                throw new CommonException("文件名为空");
            }
            byte[] readBytes = IoUtil.readBytes(inputStream);
            String md5 = DigestUtil.md5Hex(readBytes);
            TcFile saveFileEntity = CommonFileUtil.getSaveFileEntity(uploadFileWithStream.getFileName(), uploadFileWithStream.getFileSize());
            TcFile md5File=CommonFileUtil.getFileByMd5(md5,saveFileEntity);
            TcFile tcFile;
            if (md5File==null){
                tcFile = saveFileEntity;
                tcFile.setMd5(md5);
                tcFileMapper.insert(tcFile);
                String fullFilePath = getFullFilePath(tcFile.getPath());
                FileUtil.writeBytes(readBytes,fullFilePath);
            }else {
                tcFile=md5File;
            }
            return tcFile;
        } catch (Exception e) {
            String msg = StrUtil.format("上传文件失败->{}", ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        }
    }

    @Override
    public List<TcFileWithInputStream> readFileInputStreamByIdList(List<String> idList) {
        if (CollectionUtil.isEmpty(idList)) {
            return null;
        }
        if (CollectionUtil.isNotEmpty(fileReadInterceptors)){
            for (FileReadInterceptor interceptor:fileReadInterceptors){
                if (interceptor.allowRead(idList)){
                    log.info("文件授权->{}通过拦截器->{}",idList,interceptor.getClass().getName());
                }else {
                    throw new CommonException("无权限读取文件");
                }
            }
        }
        List<TcFileWithInputStream> list=new ArrayList<>();
        for (String id:idList){
            TcFile tcFile = tcFileMapper.selectById(id);
            if (tcFile == null) {
                return null;
            } else {
                if (FileStatusEnum.NOT_AVAILABLE.getValue().equals(tcFile.getStatus())) {
                    return null;
                }
                String fullFilePath = getFullFilePath(tcFile.getPath());
                BufferedInputStream inputStream = FileUtil.getInputStream(fullFilePath);
                TcFileWithInputStream fileWithInputStream=new TcFileWithInputStream();
                BeanUtils.copyProperties(tcFile, fileWithInputStream);
                fileWithInputStream.setInputStream(inputStream);
                list.add(fileWithInputStream);
            }
        }
        return list;
    }

    @Override
    public List<TcFileWithByteArray> readFileByteArrayByIdList(List<String> idList) {
        List<TcFileWithByteArray> list=new ArrayList<>();
        List<TcFileWithInputStream> tcFileWithInputStreams = readFileInputStreamByIdList(idList);
        try{
            if (CollectionUtil.isEmpty(tcFileWithInputStreams)){
                return list;
            }
            for (TcFileWithInputStream fileWithInputStream:tcFileWithInputStreams){
                InputStream inputStream=fileWithInputStream.getInputStream();
                TcFileWithByteArray tcFileWithByteArray = new TcFileWithByteArray();
                BeanUtils.copyProperties(fileWithInputStream,tcFileWithByteArray);
                byte[] readBytes = IoUtil.readBytes(inputStream);
                tcFileWithByteArray.setReadBytes(readBytes);
                list.add(tcFileWithByteArray);
            }
            return list;
        }catch (Exception e){
            String msg=StrUtil.format("根据文件ID读取文件流失败", ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        }finally {
            if (CollectionUtil.isNotEmpty(tcFileWithInputStreams)){
                for (TcFileWithInputStream fileWithInputStream:tcFileWithInputStreams){
                    InputStream inputStream = fileWithInputStream.getInputStream();
                    try {
                        inputStream.close();
                    }catch (Exception e){
                        String msg=StrUtil.format("文件id->{},名称->{},流关闭失败",fileWithInputStream.getId(),fileWithInputStream.getFileName());
                        log.error(msg,e);
                    }
                }
            }
        }
    }


    @Override
    public TcFileWithByteArray readFileByteArrayById(String id) {
        if (StrUtil.isBlank(id)){
            return null;
        }
        List<TcFileWithByteArray> list = readFileByteArrayByIdList(Collections.singletonList(id));
        if (CollectionUtil.isEmpty(list)){
            return null;
        }
        return list.get(0);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public TcFile deleteFile(String id) {
        try {
            TcFile tcFile=tcFileMapper.selectById(id);
            //假删
            if (tcFile!=null){
                TcFile newTcFile=new TcFile();
                newTcFile.setId(tcFile.getId());
                newTcFile.setStatus(FileStatusEnum.NOT_AVAILABLE.getValue());
                tcFileMapper.updateById(newTcFile);
            }

            return tcFile;
        }catch (Exception e) {
            String msg=StrUtil.format("文件删除失败,ID->{}",id);
            log.error(msg,e);
            throw new CommonException(msg);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TcFile realDeleteFile(String id) {
        try {
            TcFile tcFile = tcFileMapper.selectById(id);
            //真删
            if (tcFile != null) {
                tcFileMapper.deleteById(id);
                FileUtil.del(getFullFilePath(tcFile.getPath()));
            }
            return tcFile;
        } catch (Exception e){
            String msg = StrUtil.format("文件实际删除失败,ID->{}", id);
            log.error(msg, e);
            throw new CommonException(msg);
        }
    }

    @Override
    public void viewFile(String id, HttpServletResponse response) {
        TcFileWithByteArray fileWithByteArray = readFileByteArrayById(id);
        if (fileWithByteArray == null) {
            throw new CommonException("文件ID不存在");
        }
        try {
            byte[] readBytes = fileWithByteArray.getReadBytes();
            // 清空response
            response.reset();
            // 设置response的Header
            response.setCharacterEncoding("UTF-8");
            String extName = fileWithByteArray.getExtName();
            if (StrUtil.isBlank(extName)) {
                extName = "";
            }
            extName = extName.toLowerCase();
            if ("pdf".equalsIgnoreCase(extName)) {
                response.setContentType("application/pdf");
            } else if (Arrays.asList("jpeg", "png", "gif", "bmp").contains(extName)) {
                response.setContentType(StrUtil.format("image/{}", extName));
            } else if (Arrays.asList("txt", "json", "css", "js", "ts").contains(extName)) {
                response.setContentType("text/plain");
            } else {
                response.setContentType("application/octet-stream");
            }
            // 设置文件长度
            response.setContentLengthLong(fileWithByteArray.getFileSize());

            // 在线打开方式 文件名应该编码成UTF-8
            response.setHeader("Content-Disposition", "inline; filename=" + URLEncoder.encode(fileWithByteArray.getOriginalFileName(), "UTF-8"));

            OutputStream outputStream = response.getOutputStream();
            outputStream.write(readBytes);
            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            log.error("文件查看失败", e);
            throw new CommonException("文件查看失败");
        }
    }

    @Override
    public void viewFileWithCacheControl(String id, HttpServletResponse response) {

    }

    @Override
    public void downloadFile(String id, HttpServletResponse response) {
        TcFileWithByteArray fileWithByteArray = readFileByteArrayById(id);
        if (fileWithByteArray == null) {
            throw new CommonException("文件ID不存在");
        }
        try {
            byte[] readBytes = fileWithByteArray.getReadBytes();
            // 清空response
            response.reset();
            // 设置response的Header
            response.setCharacterEncoding("UTF-8");
            // 设置文件长度
            response.setContentLengthLong(fileWithByteArray.getFileSize());
            // 字节流
            response.setContentType("application/octet-stream");
            // 在线下载方式 文件名应该编码成UTF-8
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileWithByteArray.getOriginalFileName(), "UTF-8"));

            OutputStream outputStream = response.getOutputStream();
            outputStream.write(readBytes);
            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            log.error("文件下载失败", e);
            throw new CommonException("文件下载失败");
        }
    }

    @Override
    public Map<String, TcFile> getIdAndFileMapping(List<String> idList) {
        Map<String, TcFile> mapping = new HashMap<>();
        if (CollectionUtil.isEmpty(idList)) {
            return mapping;
        }
        QueryWrapper<TcFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", idList);
        List<TcFile> tcFileList = tcFileMapper.selectList(queryWrapper);
        if (CollectionUtil.isEmpty(tcFileList)) {
            return mapping;
        } else {
            for (TcFile tcFile : tcFileList) {
                if (FileStatusEnum.NOT_AVAILABLE.getValue().equals(tcFile.getStatus())) {
                    continue;
                }
                mapping.put(tcFile.getId(), tcFile);
            }
            return mapping;
        }
    }

    @Override
    public List<TcFile> getFileInfoByIdList(List<String> idList) {
        List<TcFile> emptyFileList = new ArrayList<>();
        if (CollectionUtil.isEmpty(idList)) {
            return emptyFileList;
        }
        QueryWrapper<TcFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", idList);
        List<TcFile> tcFileList = tcFileMapper.selectList(queryWrapper);
        if (CollectionUtil.isEmpty(tcFileList)) {
            return emptyFileList;
        } else {
            return tcFileList.stream().filter(item -> FileStatusEnum.AVAILABLE.getValue().equals(item.getStatus())).collect(Collectors.toList());
        }
    }

    @Override
    public TcFile getFileInfoById(String id) {
        return tcFileMapper.selectById(id);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateChunkUploadResult createChunkUpload(CreateChunkUploadParams params) {
        String md5 = params.getMd5();
        if (md5 == null) {
            throw new CommonException("文件MD5不存在，无法判断是否继续分块上传");
        }
        TcFile saveFileEntity = CommonFileUtil.getSaveFileEntity(params.getFileName(), params.getFileSize());
        TcFile md5File= CommonFileUtil.getFileByMd5(md5,saveFileEntity);
        CreateChunkUploadResult result = new CreateChunkUploadResult();
        if (md5File==null) {
            LambdaQueryWrapper<TcFileChunk> chunkLambdaQueryWrapper = new LambdaQueryWrapper<>();
            chunkLambdaQueryWrapper.eq(TcFileChunk::getMd5, md5).orderByDesc(TcFileChunk::getChunkNumber);
            List<TcFileChunk> tcFileChunks = fileChunkMapper.selectList(chunkLambdaQueryWrapper);
            BeanUtils.copyProperties(params, result);
            result.setFileExists(false);
            if (CollectionUtil.isEmpty(tcFileChunks)) {
                String path = saveFileEntity.getPath();
                String uploadId = IdUtil.fastUUID();
                result.setFilePath(path);
                result.setUploadId(uploadId);
                result.setAlreadyChunkNumberList(new ArrayList<>());
            } else {
                TcFileChunk tcFileChunk = tcFileChunks.get(0);
                result.setFilePath(tcFileChunk.getFilePath());
                result.setUploadId(tcFileChunk.getUploadId());
                List<Integer> alreadyChunkNumberList = tcFileChunks.stream().map(TcFileChunk::getChunkNumber).collect(Collectors.toList());
                result.setAlreadyChunkNumberList(alreadyChunkNumberList);
            }
        } else {
            result.setFileExists(true);
            result.setFileId(md5File.getId());
        }
        return result;
    }

    private String getChunkUploadPath(TcFileChunk tcFileChunk){
        return getFullFilePath(StrUtil.format("chunkUpload/{}_{}",tcFileChunk.getUploadId(),tcFileChunk.getChunkNumber()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadChunkResult uploadChunk(UploadChunkParams params,byte[] bytes) {
        if (bytes==null){
            throw new CommonException("上传分块数据不能为空");
        }
        ValidationUtils.validateEntity(params);
        Integer chunkNumber = params.getChunkNumber();
        Integer chunkSize = params.getChunkSize();
        String uploadId = params.getUploadId();
        String md5 = params.getMd5();
        String fileName = params.getFileName();
        String filePath = params.getFilePath();
        LambdaQueryWrapper<TcFileChunk> chunkLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chunkLambdaQueryWrapper.eq(TcFileChunk::getMd5, md5).eq(TcFileChunk::getChunkNumber, chunkNumber);
        TcFileChunk tcFileChunk = fileChunkMapper.selectOne(chunkLambdaQueryWrapper);
        UploadChunkResult result = new UploadChunkResult();
        BeanUtils.copyProperties(params, result);
        try {
            if (tcFileChunk == null) {
                tcFileChunk = new TcFileChunk();
                tcFileChunk.setId(SnowIdUtil.getSnowId());
                tcFileChunk.setChunkSize(chunkSize);
                tcFileChunk.setChunkNumber(chunkNumber);
                tcFileChunk.setFilePath(filePath);
                tcFileChunk.setMd5(md5);
                tcFileChunk.setFileName(fileName);
                tcFileChunk.setUploadId(uploadId);
                tcFileChunk.setCreateDate(new Date());
                tcFileChunk.setOther1("上传服务器本地");
                fileChunkMapper.insert(tcFileChunk);
                FileUtil.writeBytes(bytes,getChunkUploadPath(tcFileChunk));
            }
            result.setChunkId(tcFileChunk.getId());
            result.setEtag(tcFileChunk.getOther1());
            return result;
        } catch (Exception e) {
            String msg = StrUtil.format("文件->{},第{}块上传失败", fileName, chunkNumber);
            log.error(msg, e);
            throw new CommonException(msg);
        }
    }

    @Override
    public TcFile mergeChunk(MergeChunkParams params) {
        String uploadId = params.getUploadId();
        String fileName = params.getFileName();
        String md5 = params.getMd5();
        String filePath = params.getFilePath();
        LambdaQueryWrapper<TcFileChunk> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcFileChunk::getUploadId,uploadId);
        lambdaQueryWrapper.orderByAsc(TcFileChunk::getChunkNumber);
        List<TcFileChunk> tcFileChunks = fileChunkMapper.selectList(lambdaQueryWrapper);
        if (CollectionUtil.isEmpty(tcFileChunks)){
            throw new CommonException("uploadId对应数据不存在，无法合并大文件");
        }
        String finalFilePath = getFullFilePath(tcFileChunks.get(0).getFilePath());
        FileUtil.del(finalFilePath);
        File mergeFile = FileUtil.touch(finalFilePath);
        Long fileSize=0L;
        for (int i=0;i<tcFileChunks.size();i++){
            TcFileChunk chunk = tcFileChunks.get(i);
            byte[] chunkBytes = FileUtil.readBytes(getChunkUploadPath(chunk));
            FileUtil.writeBytes(chunkBytes,mergeFile,0,chunk.getChunkSize(),true);
            fileSize = fileSize+chunk.getChunkSize();
        }
        String prefix= FileUtil.getPrefix(fileName);
        String suffix=FileUtil.getSuffix(fileName);
        TcFile tcFile=new TcFile();
        tcFile.setId(SnowIdUtil.getSnowId());
        tcFile.setFileSize(fileSize);
        tcFile.setStatus(FileStatusEnum.AVAILABLE.getValue());
        tcFile.setOriginalFileName(fileName);
        tcFile.setFileName(prefix);
        tcFile.setExtName(suffix);
        tcFile.setMd5(md5);
        tcFile.setPath(filePath);
        tcFile.setCreateDate(new Date());
        tcFile.setCreateUser("system");
        tcFileMapper.insert(tcFile);
        fileChunkMapper.delete(lambdaQueryWrapper);
        //删除所有临时文件
        for (int i=0;i<tcFileChunks.size();i++){
            FileUtil.del(getChunkUploadPath(tcFileChunks.get(i)));
        }
        return tcFile;
    }
}
