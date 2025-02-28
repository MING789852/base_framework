package com.xm.util.pdf;

import lombok.Data;

import java.io.InputStream;

@Data
public class HtmlToPdfResult {
    private String pdfFilePath;
    private InputStream inputStream;
}
