package com.xm.status.domain.dto;

import com.xm.status.domain.entity.TcStatusDetailInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class TcStatusDetailInstanceDto extends TcStatusDetailInstance {
    private Map<String,Object> formData;
    private String name;
    private String code;
}
