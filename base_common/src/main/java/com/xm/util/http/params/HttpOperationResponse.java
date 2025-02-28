package com.xm.util.http.params;

import lombok.Data;

import java.util.Map;
@Data
public class HttpOperationResponse {
    private byte[] byteContent;
    private String strContent;
    private Map<String,String> headers;
    private int httpStatus;
}
