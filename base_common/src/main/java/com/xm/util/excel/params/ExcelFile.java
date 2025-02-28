package com.xm.util.excel.params;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;

@Data
@RequiredArgsConstructor
public class ExcelFile {
    private final String name;
    private final byte[] readBytes;
}
