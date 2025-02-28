package com.xm.util.zip;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;

@RequiredArgsConstructor
@Data
public class CompressZipInputStreamEntry {
    private final String name;
    private final InputStream inputStream;
}
