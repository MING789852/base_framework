package com.xm.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xm.module.order.domain.entity.DemoOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DemoOrderMapper extends BaseMapper<DemoOrder> {
}