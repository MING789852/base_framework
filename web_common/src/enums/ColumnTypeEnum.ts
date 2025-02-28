enum ColumnTypeEnum {
  //双击编辑时才展示输入框
  EDIT_INPUT = "editInput",
  //双击编辑时才展示月份选择框
  EDIT_MONTH = "editMonth",
  //双击编辑时才展示日期选择框
  EDIT_DATE = "editDate",
  //双击编辑时才展示选择框
  EDIT_OPTION = "editOption",
  //展示日期选择框
  DATE = "date",
  //展示文本输入框
  INPUT = "input",
  //展示多行文本输入框
  AREA_INPUT = "area_input",
  //展示选择框
  OPTION = "option",
  //展示选择框(带创建)
  OPTION_WITH_CREATE="option_with_create",
  //日期格式化展示（无法编辑）
  DATE_COMMON = "date_common",
  //时间格式化展示（无法编辑）
  DATE_TIME_COMMON = "date_time_common",
  //普通展示（无法编辑）
  COMMON = "common",
  //普通连接展示（可点击但无法编辑）
  COMMON_LINK = "commonLink",
  //渲染html
  HTML = "html",
  //渲染字典
  DICT_COMMON = 'dict_common',
  //渲染多选字典
  MULTIPLE_DICT_COMMON = 'multiple_dict_common',
  //月格式化展示（无法编辑）
  MONTH_COMMON = 'month_common',
  //展示图片列表
  IMG_LIST='img_list',
  //以标签形式展示字典
  DICT_TAG = "dict_tag",
  //详情按钮
  DETAIL_BUTTON = 'Detail_button',
  //单选
  RADIO = "radio",
  //如果数据为空，则展示按钮，非空则不展示
  BUTTON_SELECT = "button_select",
  //按钮
  BUTTON = "button",
  //多级展示
  LEVEL = "LEVEL",
  //文件上传
  FILE = "file",
  //文件上传（只读）
  ONLY_READ_FILE = "only_read_file"
}

export default ColumnTypeEnum
