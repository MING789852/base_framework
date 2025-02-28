enum QueryTypeEnum  {
  //多行文本框
  AREA_INPUT= "AREA_INPUT",
  //对象输入文本框，支持对象赋值，比如prop属性可以为order.price
  OBJECT_INPUT = "OBJECT_INPUT",
  //单行文本框
  INPUT = "Input",
  //月份精确查询
  MONTH = "Month",
  //日期精确查询
  DATE = "Date",
  //输入范围
  INPUT_RANGE = "INPUT_RANGE",
  //日期范围输入
  DATE_RANGE = "DATE_RANGE",
  //年份选择
  YEAR = "year",
  //单选
  OPTION = "Option",
  //多选like查询
  OPTION_LIKE="OPTION_LIKE",
  //多选精确查询
  MULTIPLE_OPTION = "MULTIPLE_OPTION",
  //只展示，无输入
  COMMON="COMMON"
}
export default QueryTypeEnum
