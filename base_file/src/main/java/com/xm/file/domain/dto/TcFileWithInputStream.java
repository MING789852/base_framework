package com.xm.file.domain.dto;

import com.xm.file.domain.entity.TcFile;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.InputStream;

@EqualsAndHashCode(callSuper = true)
@Data
public class TcFileWithInputStream extends TcFile {
    private InputStream inputStream;
}
