import type ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import type QueryTypeEnum from "@/enums/QueryTypeEnum";
import type QueryConditionEnum from "@/enums/QueryConditionEnum";

declare global{
  type ColumnDefine = {
    // 字段属性名
    prop?: string,
    // 标签
    label?: string,
    // 是否展示
    show?: boolean,
    // 固定列
    fixed?: boolean,
    // tooltip
    tooltip?: boolean,
    // 宽度
    width?: number|string,
    // 最小
    minWidth?: number|string,
    // 表头对齐方式
    headerAlign?: string,
    // 表数据对齐方式
    align?:'left'|'right'|'center',
    // 表格类型
    type?: ColumnTypeEnum|string|any,
    // 是否可查询
    query?: Boolean,
    // 查询条件（可选）
    queryCondition?: QueryConditionEnum,
    // 查询类型
    queryType?:QueryTypeEnum,
    // 多级表头子表头
    children?: ColumnDefine[],
    // 是否可排序
    sortable?: boolean
  }
}
