package com.xm.util.zip;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class CompressZipBytesEntry {
    private final String name;
    private final byte[] bytes;
}
