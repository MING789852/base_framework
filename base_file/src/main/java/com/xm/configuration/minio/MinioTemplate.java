package com.xm.configuration.minio;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.xm.util.bean.SpringBeanUtil;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import io.minio.messages.Part;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MinioTemplate {

    private static final MinioClient client;

    private static final MultipartMinioClient multipartClient;

    private static final String region="cn-beijing";


    static {
        client = SpringBeanUtil.getBeanByName("minioClient", MinioClient.class);
        multipartClient = SpringBeanUtil.getBeanByName("multipartMinioClient",MultipartMinioClient.class);
    }


    /**
     * 获取全部bucket
     */
    @SneakyThrows
    public List<Bucket> getAllBuckets() {
        return client.listBuckets();
    }


    /**
     * 获取文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @return 二进制流
     */
    @SneakyThrows
    public static InputStream getObjectInputStream(String bucketName, String objectName) {
        return client.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    /**
     * 创建桶
     *
     * @param bucketName 桶名称
     */
    @SneakyThrows
    public static void createBucket(String bucketName) {
        boolean found =
                client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            client.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .region(region)
                            .build());
        }
    }

    /**
     * 删除桶
     *
     * @param bucketName 桶名称
     */
    @SneakyThrows
    public static void removeBucket(String bucketName) {
        client.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }

    /**
     * 根据文件前置查询文件
     *
     * @param bucketName bucket名称
     * @param prefix     前缀
     * @param recursive  是否递归查询
     * @return MinioItem 列表
     */
    @SneakyThrows
    public List<Item> getAllObjectsByPrefix(String bucketName, String prefix, boolean recursive) {
        List<Item> list = new ArrayList<>();
        ListObjectsArgs listObjectsArgs=
                ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).recursive(recursive).build();
        Iterable<Result<Item>> objectsIterator = client.listObjects(listObjectsArgs);
        if (objectsIterator != null) {
            for (Result<Item> result : objectsIterator) {
                Item item = result.get();
                list.add(item);
            }
        }
        return list;
    }


    /**
     * 获取文件信息
     *
     * @param bucketName 桶名称
     * @param objectName 文件名称
     */
    @SneakyThrows
    public static StatObjectResponse getObjectInfo(String bucketName, String objectName) {
        return client.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }


    private static String getContentTypeByFileName(String fileName){
        String contentType="application/octet-stream";
        if (StrUtil.isBlank(fileName)){
            return contentType;
        }
        String suffix= FileUtil.getSuffix(fileName);
        if(suffix.equals("jpg") || suffix.equals("JPG")){
            contentType="image/jpg";
        }
        else if(suffix.equals("png") || suffix.equals("PNG")){
            contentType="image/png";
        }
        else if(suffix.equals("jpeg") || suffix.equals("JPEG")){
            contentType="image/jpeg";
        }
        else if(suffix.equals("svg") || suffix.equals("SVG")){
            contentType="image/svg";
        }
        else if(suffix.equals("gif") || suffix.equals("GIF")){
            contentType="image/gif";
        }
        return contentType;
    }


    /**
     * 上传文件
     *
     * @param bucketName 桶名称
     * @param objectName 文件名
     * @param stream     流
     */
    public static void putObject(String bucketName, String objectName, InputStream stream) throws Exception {
        String contentType=getContentTypeByFileName(objectName);
        client.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
                                stream,stream.available(),ObjectWriteArgs.MAX_PART_SIZE)
                        .contentType(contentType)
                        .build());
    }


    public static void putObject(String bucketName, String objectName, byte[] data) throws Exception {
        String contentType=getContentTypeByFileName(objectName);
        ByteArrayInputStream stream=new ByteArrayInputStream(data);
        client.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
                                stream,data.length,ObjectWriteArgs.MAX_PART_SIZE)
                        .contentType(contentType)
                        .build());
    }


    /**
     * 删除文件
     * @param bucketName 桶名称
     * @param objectName 文件名
     */
    public static void removeObject(String bucketName, String objectName) throws Exception  {
        client.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
    }


    /**
     * 判断文件夹是否存在
     *
     * @param bucketName 桶名称
     * @param prefix     文件夹名字
     */
    @SneakyThrows
    public static Boolean folderExists(String bucketName, String prefix) {
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder().bucket(bucketName)
                .prefix(prefix).recursive(false).build());
        for (Result<Item> result:results){
            Item item = result.get();
            if (item.isDir()){
                return true;
            }
        }
        return false;
    }

    /**
     * 创建文件夹
     * @param bucketName 桶名称
     * @param path 路径
     */
    @SneakyThrows
    public static void createFolder(String bucketName,String path) {
        client.putObject(PutObjectArgs.builder().bucket(bucketName).object(path)
                .stream(new ByteArrayInputStream(new byte[]{}),0,-1).build());
    }

    /**
     * 获取文件在minio在服务器上的外链
     * @param bucketName 桶名称
     * @param objectName 文件名
     */
    @SneakyThrows
    public static String getObjectUrl(String bucketName,String objectName){
        return client.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
    }

    /**
     * 获取文件外链
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param expires    过期时间 <=7
     * @return url
     */
    @SneakyThrows
    public String getObjectURLWithExpiry(String bucketName, String objectName, Integer expires) {
        GetPresignedObjectUrlArgs getPresignedObjectUrlArgs=
                GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(bucketName).object(objectName).expiry(expires).build();
        return client.getPresignedObjectUrl(getPresignedObjectUrlArgs);
    }


    @SneakyThrows
    public static String createMultipartUpload(String bucketName, String objectName){
        Multimap<String, String> headers = HashMultimap.create();
        String contentType = getContentTypeByFileName(objectName);
        headers.put("Content-Type", contentType);

        // 创建分段上传请求
        CompletableFuture<CreateMultipartUploadResponse> multipartUploadAsync = multipartClient.createMultipartUploadAsync(bucketName, null, objectName, headers, null);
        return multipartUploadAsync.get().result().uploadId();
    }

    @SneakyThrows
    public static UploadPartResponse uploadPart(String uploadId,String bucketName, String objectName,Object data,int partNumber,long length){
        Multimap<String, String> headers = HashMultimap.create();
        String contentType = getContentTypeByFileName(objectName);
        headers.put("Content-Type", contentType);
        CompletableFuture<UploadPartResponse> uploadPartAsync = multipartClient.uploadPartAsync(bucketName, null, objectName, data, length, uploadId, partNumber, headers, null);
        return uploadPartAsync.get();
    }

    @SneakyThrows
    public static ObjectWriteResponse  completeMultipartUpload(String uploadId,String bucketName, String objectName, Part[] parts){
        Multimap<String, String> headers = HashMultimap.create();
        String contentType = getContentTypeByFileName(objectName);
        headers.put("Content-Type", contentType);
        CompletableFuture<ObjectWriteResponse> completeMultipartUploadAsync = multipartClient.completeMultipartUploadAsync(bucketName,null,objectName,uploadId,parts,headers,null);
        return completeMultipartUploadAsync.get();
    }

}
