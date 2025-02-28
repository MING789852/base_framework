package com.xm.auth.domain.vo;

import com.xm.auth.domain.entity.TcUser;
import lombok.Data;

import java.util.List;

@Data
public class UserEnableVo {
    private List<TcUser> tcUserList;
    private Integer judgeEnable;
}
