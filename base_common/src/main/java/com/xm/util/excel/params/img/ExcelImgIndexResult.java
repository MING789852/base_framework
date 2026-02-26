package com.xm.util.excel.params.img;

import lombok.Data;

@Data
public class ExcelImgIndexResult {
    //左上角dx dy偏移
    private Integer dx1;
    private Integer dy1;
    private Integer col1;
    private Integer row1;
    //右下角dx dy偏移
    private Integer dx2;
    private Integer dy2;
    private Integer col2;
    private Integer row2;

    //图片数据
    private byte[] imgData;
    //由Workbook中的常量决定，例如Workbook.PICTURE_TYPE_JPEG
    private int imgType;
}
