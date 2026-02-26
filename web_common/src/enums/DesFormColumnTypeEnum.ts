enum DesFormColumnTypeEnum {
    //展示日期选择框
    DATE = "date",
    //展示文本输入框
    INPUT = "input",
    //展示多行文本输入框
    AREA_INPUT = "area_input",
    //展示选择框
    OPTION = "option",
    MULTIPLE_OPTION = "multiple_option",
    //展示选择框(带创建)
    OPTION_WITH_CREATE="option_with_create",
    //展示图片列表
    IMG_LIST='img_list',
    //普通展示（无法编辑）
    COMMON = "common",
    //如果数据为空，则展示按钮，非空则不展示
    BUTTON_SELECT = "button_select",
    //多选
    BUTTON_MULTI_SELECT = "button_multi_select",
    //按钮
    BUTTON = "button",
    //单选
    RADIO = "radio",
    //多选
    CHECK_BOX_LIST = "check_box_list",
    //渲染字典
    DICT_COMMON = 'dict_common',
    //文件上传
    FILE = "file"
}

export default DesFormColumnTypeEnum
