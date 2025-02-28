package com.xm.auth.domain.dto;

import com.xm.auth.domain.entity.TcRole;
import lombok.Data;

import java.util.List;

@Data
public class RouterActionSaveRefDto {
    private TcRole role;
    private List<String> routerActionIdList;
}
