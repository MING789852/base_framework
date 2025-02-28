enum QueryConditionEnum {
  //精确查询，转化为sql就是x=?
  EQ = "eq",
  //模糊查询，转化为sql就是x like ?
  LIKE = "like",
  //批量精确查询，转化为sql就是x in (?,?,?)
  IN = "in",
  //批量模糊查询，转化为sql就是x like ? or x like ?
  LIKE_IN = "likeIn",
  //范围查询条件，转化为sql就是0<x and x<2
  RANGE="range",
  //日期范围查询条件，后端对开始日期和结束日期做了特殊处理，转化为sql就是BETWEEN ? AND ?
  BETWEEN_DATE = "betweenDate"
}

export default QueryConditionEnum
