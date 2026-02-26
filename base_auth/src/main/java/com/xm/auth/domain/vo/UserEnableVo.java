package com.xm.auth.domain.vo;

import com.xm.auth.domain.entity.TcUser;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserEnableVo implements Serializable {
    private List<TcUser> tcUserList;
    private Integer judgeEnable;
}
