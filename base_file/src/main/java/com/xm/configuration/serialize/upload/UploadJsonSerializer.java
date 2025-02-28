package com.xm.configuration.serialize.upload;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.xm.file.domain.entity.TcFile;
import com.xm.file.mapper.TcFileMapper;
import com.xm.util.bean.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UploadJsonSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String str, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
//        log.info("序列化上传文件列表");

        TcFileMapper tcFileMapper= SpringBeanUtil.getBeanByClass(TcFileMapper.class);

        if (StrUtil.isBlank(str)){
           str="[]";
        }
        List<String> fileIdList=JSONUtil.toList(str,String.class);
        List<TcFile> tcFileVoList=new ArrayList<>();

        for (String id:fileIdList){
            TcFile tcFile=tcFileMapper.selectById(id);
            if (tcFile!=null){
                tcFileVoList.add(tcFile);
            }
        }
        jsonGenerator.writeObject(tcFileVoList);
    }
}
