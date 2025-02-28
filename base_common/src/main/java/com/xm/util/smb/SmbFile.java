package com.xm.util.smb;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SmbFile {
    private boolean isDir;
    private long fileSize;
    private String fileName;
    private String shareName;
    private String path;
}
