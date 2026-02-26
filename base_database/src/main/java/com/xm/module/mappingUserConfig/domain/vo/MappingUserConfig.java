package com.xm.module.mappingUserConfig.domain.vo;

import com.xm.auth.domain.entity.TcUser;
import lombok.Data;

import java.util.List;

@Data
public class MappingUserConfig {
    private String name;
    private List<TcUser> userList;
}
