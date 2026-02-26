package com.xm.auth.domain.vo;

import com.xm.auth.domain.entity.TcRouter;
import lombok.Data;

import java.io.Serializable;

@Data
public class AddRouterVo implements Serializable {
    private TcRouter parentRouter;
    private TcRouter addRouter;
}
