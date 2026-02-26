import type {QueryTypeEnum} from "@/enums/QueryTypeEnum";

declare global{
  type DetailColumnDefine = {
    // 字段属性名
    prop?: string,
    // 标签
    label?: string,
    // 输入提示
    placeholder?: string,
    // 是否禁止输入
    disabled?: boolean,
    // 表格类型
    type: QueryTypeEnum
  }
}
