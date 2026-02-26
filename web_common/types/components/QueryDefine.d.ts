declare type QueryDefine = {
  // 当前页数
  current?: number,
  // 每页数量
  size?: number,
  // 总数
  total?: number,
  // 查询条件
  queryParams?: any,
  // 排序条件
  orderByParams?: any,
}
