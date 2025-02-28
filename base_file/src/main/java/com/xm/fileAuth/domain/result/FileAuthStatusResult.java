package com.xm.fileAuth.domain.result;

import lombok.Data;

import java.util.List;

@Data
public class FileAuthStatusResult {
    private int status;
    //需要新申请
    private List<String> newApplyFileIdList;
    //已申请
    private List<String> authFileIdList;
    //申请中
    private List<String> applyingFileIdList;
}
