package com.xm.file.domain.vo;

import com.xm.file.domain.entity.TcFile;
import lombok.Data;

@Data
public class TcFileVo extends TcFile {
    private String uploadMapKey;
}
