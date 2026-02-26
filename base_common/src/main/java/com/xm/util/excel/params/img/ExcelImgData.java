package com.xm.util.excel.params.img;

import lombok.Data;

@Data
public class ExcelImgData {
    private String extName;
    private byte[] data;

    public ExcelImgData(String extName, byte[] data) {
        this.extName = extName;
        this.data = data;
    }
}
