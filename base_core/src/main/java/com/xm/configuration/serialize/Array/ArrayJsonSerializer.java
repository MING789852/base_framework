package com.xm.configuration.serialize.Array;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ArrayJsonSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String str, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (StrUtil.isBlank(str)){
           str="[]";
        }
        jsonGenerator.writeObject(JSONUtil.parseArray(str));
    }
}
