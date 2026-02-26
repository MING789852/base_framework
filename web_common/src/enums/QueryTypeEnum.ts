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
  //日期时间
  DATE_TIME = "DATE_TIME",
  //输入范围
  INPUT_RANGE = "INPUT_RANGE",
  //日期范围输入
  DATE_RANGE = "DATE_RANGE",
  MONTH_RANGE = "MONTH_RANGE",
  //年份选择
  YEAR = "year",
  //单选
  OPTION = "Option",
  //多选like查询
  OPTION_LIKE="OPTION_LIKE",
  //多选精确查询
  MULTIPLE_OPTION = "MULTIPLE_OPTION",
  //只展示，无输入
  COMMON="COMMON",
  //普通展示可换行
  COMMON_OVER = "common_over",
  DICT_COMMON="DICT_COMMON",
  //如果数据为空，则展示按钮，非空则不展示
  BUTTON_SELECT = "button_select",
  //多选
  BUTTON_MULTI_SELECT = "button_multi_select",
  //展示图片列表
  IMG_LIST='img_list'
}
export default QueryTypeEnum
