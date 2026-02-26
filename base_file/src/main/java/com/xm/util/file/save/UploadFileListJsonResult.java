package com.xm.util.file.save;

import lombok.Data;

import java.util.Map;

@Data
public class UploadFileListJsonResult {
    private String fileListJson;
    private Map<String, String> tempIdAndFileIdMap;
}
