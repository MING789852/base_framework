package com.xm.fileAuth.domain.vo;

import com.xm.fileAuth.domain.entity.TcFileApply;
import com.xm.fileAuth.domain.entity.TcFileApplyDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TcFileApplyVo extends TcFileApply {
    private List<TcFileApplyDetail> detailList;
}
