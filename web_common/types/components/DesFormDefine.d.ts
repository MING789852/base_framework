import type DesFormColumnTypeEnum from "@/enums/DesFormColumnTypeEnum";

declare global{
  interface DesFormContentDefine{
    // 字段属性名
    prop?: string,
    // 标签
    label?: string,
    // 类型
    type?: DesFormColumnTypeEnum,
  }
  interface DesFormDefine{
    title?:string,
    showTitle: boolean,
    content: DesFormContentDefine[]
  }
}
