package com.xm.module.order.domain.params;

import com.xm.core.params.ColumnProps;
import com.xm.module.core.params.QueryData;
import com.xm.module.order.domain.entity.DemoOrder;
import lombok.Data;

import java.util.List;

@Data
public class OrderExportParams {
    private QueryData<DemoOrder> queryData;
    private List<ColumnProps> mainColumns;
}
