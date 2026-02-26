create table tc_business_approving
(
    id            varchar(32)                     not null comment '主键'
        primary key,
    business_id   varchar(32)                     not null comment '业务id关联tc_business',
    version       varchar(50) default ''          not null comment '版本',
    business_type varchar(50) default ''          not null comment '业务类型',
    business_no   varchar(50) default ''          not null comment '业务编码',
    user_id       varchar(32)                     not null comment '关联tc_user',
    user_name     varchar(32)                     not null comment '关联tc_user',
    judge_enable  int         default 1           not null comment '是否可用',
    create_user   varchar(50)                     null comment '创建人员',
    create_date   date        default (curdate()) null comment '创建日期',
    update_user   varchar(50)                     null comment '修改人员',
    update_date   date                            null comment '修改日期'
)
    comment '正在审批业务';

create index idx_business_id
    on tc_business_approving (business_id);

create index idx_business_user
    on tc_business_approving (business_type, version, user_id);

create index idx_judge_enable
    on tc_business_approving (judge_enable);


create table tc_config
(
    id           varchar(32)                      not null comment '主键'
        primary key,
    group_code   varchar(100) default ''          not null comment '组编码',
    config_code  varchar(100) default ''          not null comment '编码',
    config_value text                             null comment '配置值',
    create_user  varchar(50)                      null comment '创建人员',
    create_date  date         default (curdate()) null comment '创建日期',
    update_user  varchar(50)                      null comment '修改人员',
    update_date  date                             null comment '修改日期',
    constraint uidx_code
        unique (group_code, config_code)
)
    comment '配置表';

create table tc_dept
(
    id            varchar(100) default '066' not null comment '部门id'
        primary key,
    factory       varchar(100)               null comment '所属工厂',
    name          varchar(50)                null comment '部门名称',
    now_num       int                        null comment '当前部门包含子部门人数',
    now_num_base  int                        null comment '当前部门人数',
    expect_num    int          default 0     null comment '预计人数',
    dimission_num int          default 0     null comment '待离职人数',
    parent_id     varchar(100)               null comment '上级部门id',
    level         int                        null comment '层级',
    orders        bigint                     null,
    update_time   datetime                   null comment '修改时间',
    judge_enable  int          default 1     not null comment '是否可用'
)
    comment '部门表';

create index dept_pid
    on tc_dept (parent_id);

create table tc_dict
(
    id           varchar(32)              not null comment '主键'
        primary key,
    group_key    varchar(32)              not null comment '分组',
    dict_code    varchar(100)             not null comment '编码',
    dict_label   varchar(255)             not null comment '编码描述',
    judge_enable int  default 1           not null comment '是否可用',
    create_user  varchar(50)              null comment '创建人员',
    create_date  date default (curdate()) null comment '创建日期',
    update_user  varchar(50)              null comment '修改人员',
    update_date  date                     null comment '修改日期',
    level        int  default 0           not null comment '层级',
    parent_id    varchar(50)              null comment '父id'
)
    comment '字典组子项';

create index idx_dict_code
    on tc_dict (dict_code);

create index idx_group_key
    on tc_dict (group_key);

create index idx_level
    on tc_dict (level);

create index idx_parent_id
    on tc_dict (parent_id);

create table tc_dict_group
(
    id           varchar(32)              not null comment '主键'
        primary key,
    group_key    varchar(32)              not null comment '分组',
    group_name   varchar(32)              not null comment '分组名称',
    judge_enable int  default 1           not null comment '是否可用',
    create_user  varchar(50)              null comment '创建人员',
    create_date  date default (curdate()) null comment '创建日期',
    update_user  varchar(50)              null comment '修改人员',
    update_date  date                     null comment '修改日期'
)
    comment '字典组';

create index idx_group_key
    on tc_dict_group (group_key);

create table tc_file
(
    id                 varchar(32)                      not null comment 'id'
        primary key,
    status             varchar(10)  default '1'         not null comment '0、不可用 1、可用',
    original_file_name varchar(255)                     not null comment '文件名称（带后缀）',
    file_name          varchar(255)                     not null comment '文件名称(不带后缀)',
    ext_name           varchar(50)  default 'other'     not null comment '文件后缀',
    file_size          bigint                           null comment '文件大小(字节)',
    path               text                             not null comment '文件路径',
    create_user        varchar(50)                      null comment '创建人员',
    create_date        date         default (curdate()) null comment '创建日期',
    update_user        varchar(50)                      null comment '修改人员',
    update_date        date                             null comment '修改日期',
    md5                varchar(255) default ''          null comment 'md5码'
)
    comment '文件';

create index idx_md5
    on tc_file (md5);

create table tc_file_apply
(
    id              varchar(32)                      not null comment '主键',
    ref_id          varchar(50)  default ''          null comment '关联ID',
    ref_type        varchar(50)  default ''          null comment '关联类型',
    user_id         varchar(32)                      not null comment '申请人',
    user_name       varchar(50)                      not null comment '申请人名称',
    apply_remark    varchar(255) default ''          null comment '申请备注',
    apply_time      datetime     default (curdate()) null comment '申请时间',
    effective_time  datetime     default (curdate()) null comment '申请有效日期',
    status          int          default 0           null comment '0、审批中 1、同意 2、拒绝',
    operate_user_id varchar(32)                      null comment '授权人id',
    operate_user    varchar(32)                      null comment '授权人名称',
    operate_time    datetime                         null comment '授权人操作时间'
) comment '文件权限申请主表';

create index idx_desc_apply_time
    on tc_file_apply (apply_time desc);

create index idx_status
    on tc_file_apply (status);

create index idx_user_id
    on tc_file_apply (user_id);

create table tc_file_apply_detail
(
    id        varchar(32)            not null comment '主键',
    apply_id  varchar(50) default '' null comment '关联tc_file_apply表',
    file_id   varchar(32)            not null comment '文件ID',
    file_name varchar(50)            not null comment '文件名称'
) comment '文件权限申请明细表';

create index idx_apply_id
    on tc_file_apply_detail (apply_id);

create index idx_file_id
    on tc_file_apply_detail (file_id);

create table tc_file_auth_certificate
(
    id              varchar(32)                  not null comment '主键'
        primary key,
    file_id         varchar(32)                  not null comment '文件ID',
    file_name       varchar(50)                  not null comment '文件名称',
    user_id         varchar(32)                  not null comment '申请人',
    user_name       varchar(50)                  not null comment '申请人名称',
    apply_time      datetime default (curdate()) null comment '申请时间',
    effective_time  datetime default (curdate()) null comment '授权有效日期',
    operate_user_id varchar(32)                  null comment '授权人id',
    operate_user    varchar(32)                  null comment '授权人名称',
    operate_time    datetime                     null comment '授权人操作时间',
    constraint uidx_uf_id
        unique (user_id, file_id)
) comment '文件授权用户表';

create table tc_file_chunk
(
    id           varchar(32)                      not null comment '主键'
        primary key,
    md5          varchar(255) default ''          null comment '文件MD5',
    upload_id    varchar(255) default ''          null comment '上传唯一ID',
    file_name    varchar(255) default ''          null comment '文件名',
    file_path    varchar(255) default ''          null comment '文件位置',
    chunk_number int          default 0           null comment '分块定位',
    chunk_size   int          default 0           null comment '分块大小',
    other1       varchar(255)                     null comment '分块其他信息1',
    other2       varchar(255)                     null comment '分块其他信息2,minio用于存储etag',
    other3       varchar(255)                     null comment '分块其他信息3',
    create_date  date         default (curdate()) null comment '创建日期',
    constraint uidx_md5_chunk_number
        unique (md5, chunk_number)
) comment '文件分块上传记录表(用于断点续传)';

create index idx_md5
    on tc_file_chunk (md5);

create index idx_upload_id
    on tc_file_chunk (upload_id);

create table tc_flowable_elements
(
    process_definition_key  varchar(100)   not null comment '流程定义key',
    process_definition_name varchar(100)   not null comment '流程定义名称',
    element_id              varchar(100)   not null comment '节点id',
    element_type            varchar(100)   not null comment '节点类型',
    element_x               decimal(10, 2) null comment '节点x坐标',
    element_y               decimal(10, 2) null comment '节点y坐标',
    text_x                  decimal(10, 2) null comment '节点文本x坐标',
    text_y                  decimal(10, 2) null comment '节点文本y坐标',
    assignee                varchar(100)   null comment '节点人员key',
    assignee_name           varchar(100)   null comment '节点人员名称',
    source_node_Id          varchar(100)   null comment '连线源节点id',
    target_node_Id          varchar(100)   null comment '连线目标节点id',
    content1                text           null comment '预留字段1',
    content2                text           null comment '预留字段2',
    content3                varchar(100)   null comment '预留字段3',
    content4                varchar(100)   null comment '预留字段4',
    element_name            varchar(100)   null comment '节点名称',
    content5                text           null comment '预留字段5',
    content6                text           null comment '预留字段6',
    primary key (process_definition_key, element_id)
) comment '流程图节点';


create table tc_flowable_model
(
    process_definition_key  varchar(100)             not null comment '流程定义key'
        primary key,
    process_definition_name varchar(100)             not null comment '流程定义名称',
    create_user             varchar(100)             null comment '创建人员',
    create_date             datetime default (now()) null comment '创建日期'
)
    comment '流程模型';

create table tc_form_detail_model
(
    id                 varchar(32)                      not null comment '主键'
        primary key,
    form_main_model_id varchar(30)                      not null comment '关联tc_form_main_model',
    field_code         varchar(100) default ''          not null comment '字段编码',
    field_name         varchar(100) default ''          not null comment '字段名称',
    field_type         varchar(100) default ''          not null comment '字段类型',
    other1             varchar(100) default ''          null comment '备用字段1',
    other2             varchar(100) default ''          null comment '备用字段2',
    other3             varchar(100) default ''          null comment '备用字段3',
    sequence           int                              not null comment '序号',
    placeholder        varchar(100) default ''          not null comment '占位符',
    required           tinyint(1)   default 0           not null comment '是否必填',
    judge_show         tinyint(1)   default 1           not null comment '是否展示',
    create_date        date         default (curdate()) null comment '创建日期',
    create_user        varchar(50)                      null comment '创建人员',
    update_date        date                             null comment '更新日期',
    update_user        varchar(50)                      null comment '更新人员',
    constraint uidx_detail_model
        unique (form_main_model_id, field_code)
)
    comment '表单详情模型';

create table tc_form_dynamic_field
(
    id             varchar(32)            not null comment '主键'
        primary key,
    ref_id         varchar(32) default '' not null comment '关联id',
    ref_type       varchar(50) default '' not null comment '关联类型',
    form_ins_id    varchar(32) default '' not null comment '表单id',
    form_detail_id varchar(32) default '' not null comment '表单id',
    field_text     varchar(255)           null comment '文本字段',
    field_big_text text                   null comment '大文本字段',
    field_date     date                   null comment '日期字段',
    constraint uidx_one
        unique (form_ins_id, form_detail_id)
)
    comment '表单实例动态字段';

create table tc_form_file
(
    id             varchar(32)                  not null comment '主键'
        primary key,
    ref_id         varchar(32)                  not null comment '关联id',
    ref_type       varchar(50)                  not null comment '关联类型',
    ref_field      varchar(50)                  not null comment '关联字段',
    sequence       int                          null comment '序号',
    file_id        varchar(50)                  not null comment '文件id',
    file_name      varchar(255)                 not null comment '文件名称',
    file_type      varchar(50)                  not null comment '文件类型',
    file_size      bigint                       null comment '文件大小',
    create_date    datetime default (curdate()) null comment '创建日期',
    create_user    varchar(50)                  null comment '创建人员',
    create_user_id varchar(50)                  null comment '创建人员id'
)
    comment '单据附件';

create index idx_file_id
    on tc_form_file (file_id);

create index idx_ref
    on tc_form_file (ref_id, ref_type);

create table tc_form_label
(
    id          varchar(32)              not null comment '主键'
        primary key,
    label_name  varchar(100)             not null comment '标签名称',
    create_date date default (curdate()) null comment '创建日期',
    create_user varchar(50)              null comment '创建人员',
    update_date date                     null comment '更新日期',
    update_user varchar(50)              null comment '更新人员',
    constraint uidx_label_name
        unique (label_name)
)
    comment '标签';

create table tc_form_label_ref
(
    ref_business_id varchar(32)              not null comment '关联业务id',
    ref_type        varchar(50)              not null comment '关联类型',
    ref_label_id    varchar(32)              not null comment '关联标签id',
    create_date     date default (curdate()) null comment '创建日期',
    create_user     varchar(50)              null comment '创建人员',
    update_date     date                     null comment '更新日期',
    update_user     varchar(50)              null comment '更新人员',
    primary key (ref_business_id, ref_type, ref_label_id)
)
    comment '标签关联';

create table tc_form_main_instance
(
    id                 varchar(32)                     not null comment '主键'
        primary key,
    form_main_model_id varchar(32)                     not null comment '关联tc_form_main_model',
    ref_id             varchar(32) default ''          not null comment '关联id',
    ref_type           varchar(50) default ''          not null comment '关联类型',
    ref_filed1         varchar(50) default ''          not null comment '关联备用字段',
    ref_filed2         varchar(50) default ''          not null comment '关联备用字段',
    name               varchar(50) default ''          not null comment '实例名称',
    code               varchar(50) default ''          not null comment '实例编码',
    finish             tinyint(1)  default 0           not null comment '实例是否结束',
    create_date        date        default (curdate()) null comment '创建日期',
    create_user        varchar(50)                     null comment '创建人员',
    update_date        date                            null comment '更新日期',
    update_user        varchar(50)                     null comment '更新人员',
    constraint uidx_main_instance
        unique (form_main_model_id, ref_id, ref_type)
)
    comment '表单主表实例';

create table tc_form_main_model
(
    id          varchar(32)              not null comment '主键'
        primary key,
    name        varchar(100)             not null comment '名称',
    code        varchar(100)             not null comment '编码',
    create_date date default (curdate()) null comment '创建日期',
    create_user varchar(50)              null comment '创建人员',
    update_date date                     null comment '更新日期',
    update_user varchar(50)              null comment '更新人员',
    constraint code
        unique (code)
)
    comment '表单主表模型';


create table tc_msg
(
    id            varchar(50)                      not null comment '主键'
        primary key,
    business_type varchar(100) default ''          not null comment '业务类型',
    business_key  varchar(100) default ''          not null comment '业务编码',
    type          varchar(50)  default '0'         not null comment '消息类型',
    info          text                             null comment '额外信息',
    des           varchar(255) default ''          null comment '描述',
    judge_finish  tinyint(1)   default 0           not null,
    user_id       varchar(100)                     null comment '用户',
    create_date   datetime     default (curdate()) null comment '创建日期',
    update_date   datetime                         null comment '更新日期',
    title         text                             null comment '标题',
    content       text                             null comment '内容',
    group_id      varchar(100)                     null comment '群组id',
    group_at_all  varchar(3)                       null comment '群组@所有人',
    constraint uidx_msg
        unique (business_type, business_key, type, user_id)
)
    comment '消息表';

create index idx_create_time
    on tc_msg (create_date desc);

create table tc_msg_user_mapping
(
    user_id varchar(50) not null comment 'tc_user表主键',
    mapping varchar(50) not null comment '映射关系',
    type    varchar(50) not null comment '类型',
    constraint user_id
        unique (user_id, mapping, type)
)
    comment '消息用户映射表';

create table tc_role
(
    id           varchar(32)              not null comment '主键'
        primary key,
    role_code    varchar(50)              not null comment '角色编码',
    role_name    varchar(100)             not null comment '角色名称',
    judge_enable int  default 1           not null comment '是否可用',
    create_user  varchar(50)              null comment '创建人员',
    create_date  date default (curdate()) null comment '创建日期',
    update_user  varchar(50)              null comment '修改人员',
    update_date  date                     null comment '修改日期',
    constraint uidx_role_code
        unique (role_code)
)
    comment '角色';

create table tc_role_button_rel
(
    id           varchar(32)              not null comment '主键'
        primary key,
    button_id    varchar(32)              not null comment '按钮主键',
    role_id      varchar(32)              not null comment '角色主键',
    judge_enable int  default 1           not null comment '是否可用',
    create_user  varchar(50)              null comment '创建人员',
    create_date  date default (curdate()) null comment '创建日期',
    update_user  varchar(50)              null comment '修改人员',
    update_date  date                     null comment '修改日期'
)
    comment '角色和按钮关联表';

create table tc_role_router_action_rel
(
    id               varchar(32)              not null comment '主键'
        primary key,
    router_action_id varchar(32)              not null comment '路由操作主键',
    role_id          varchar(32)              not null comment '角色主键',
    judge_enable     int  default 1           not null comment '是否可用',
    create_user      varchar(50)              null comment '创建人员',
    create_date      date default (curdate()) null comment '创建日期',
    update_user      varchar(50)              null comment '修改人员',
    update_date      date                     null comment '修改日期',
    router_id        varchar(32)              not null comment '路由主键'
)
    comment '角色和路由操作关联表';

create index idx_role_id
    on tc_role_router_action_rel (role_id);

create index idx_router_action_id
    on tc_role_router_action_rel (router_action_id);

create index idx_router_id
    on tc_role_router_action_rel (router_id);

create table tc_role_router_rel
(
    id           varchar(32)              not null comment '主键'
        primary key,
    router_id    varchar(32)              not null comment '路由主键',
    role_id      varchar(32)              not null comment '角色主键',
    judge_enable int  default 1           not null comment '是否可用',
    create_user  varchar(50)              null comment '创建人员',
    create_date  date default (curdate()) null comment '创建日期',
    update_user  varchar(50)              null comment '修改人员',
    update_date  date                     null comment '修改日期'
)
    comment '角色和路由关联表';

create index idx_role_id
    on tc_role_router_rel (role_id);

create index idx_router_id
    on tc_role_router_rel (router_id);

create table tc_router
(
    id           varchar(32)                    not null comment '主键'
        primary key,
    path         varchar(100)                   not null comment '路由地址',
    name         varchar(100)                   not null comment '路由名称',
    title        varchar(100)                   null comment '标题',
    redirect     varchar(100)                   null comment '重定向',
    icon         varchar(100)                   null comment '菜单图标',
    component    varchar(100)                   null comment '组件路径',
    show_link    tinyint(1) default 1           null comment '是否在菜单中显示',
    rank_int     int                            null comment '菜单升序排序,值越高越后（只对顶级路由有效）',
    keep_Alive   tinyint(1) default 1           null comment '路由组件缓存',
    level        int                            null comment '0代表顶级菜单，1代表页面',
    has_button   tinyint(1)                     null comment '是否存在按钮',
    parent_id    varchar(32)                    null comment '父主键',
    judge_enable int        default 1           not null comment '是否可用',
    create_user  varchar(50)                    null comment '创建人员',
    create_date  date       default (curdate()) null comment '创建日期',
    update_user  varchar(50)                    null comment '修改人员',
    update_date  date                           null comment '修改日期',
    hidden_Tag   tinyint(1) default 0           null comment '不显示标签页',
    judge_public tinyint(1) default 0           not null comment '是否公开',
    fullscreen   tinyint(1) default 0           null comment '是否全屏展示',
    constraint name
        unique (name)
)
    comment '路由';

create index router_parent_id
    on tc_router (parent_id);

create table tc_router_action
(
    id           varchar(32)                      not null comment '主键'
        primary key,
    router_id    varchar(32)                      not null comment '路由id',
    action_code  varchar(100)                     not null comment '操作编码',
    action_name  varchar(100) default ''          null comment '操作名称',
    action_type  varchar(100) default '0'         null comment '操作类型0、按钮 1、函数 2、其它',
    judge_enable int          default 1           not null comment '是否可用',
    create_user  varchar(50)                      null comment '创建人员',
    create_date  date         default (curdate()) null comment '创建日期',
    update_user  varchar(50)                      null comment '修改人员',
    update_date  date                             null comment '修改日期',
    constraint uidx_rid_code
        unique (router_id, action_code)
)
    comment '路由操作权限';

create table tc_sequence
(
    id          bigint                     not null comment 'id'
        primary key,
    code        varchar(50)                not null comment '编码',
    value       bigint default 1           null comment '序号',
    create_date date   default (curdate()) null comment '创建时间',
    update_date date                       null comment '修改时间',
    constraint code
        unique (code)
)
    comment '序号生成表';

create table tc_status_detail_instance
(
    id                      varchar(32)                     not null comment '主键'
        primary key,
    ref_id                  varchar(32) default ''          not null comment '关联id',
    ref_type                varchar(50) default ''          not null comment '关联类型',
    ref_filed1              varchar(50) default ''          null comment '关联备用字段',
    ref_filed2              varchar(50) default ''          null comment '关联备用字段',
    sequence                int         default 0           null comment '序号',
    status_main_model_id    varchar(32)                     not null comment '关联tc_status_main_model',
    status_main_instance_id varchar(32)                     not null comment '关联tc_status_main_instance',
    status_detail_model_id  varchar(32)                     not null comment '关联tc_status_detail_model',
    form_main_model_id      varchar(30)                     null comment '关联tc_form_main_model',
    form_main_instance_id   varchar(32)                     null comment '关联tc_form_main_instance',
    active                  tinyint(1)  default 0           not null comment '是否激活中',
    create_date             datetime    default (curdate()) null comment '创建日期',
    create_user             varchar(50)                     null comment '创建人员',
    update_date             datetime                        null comment '更新日期',
    update_user             varchar(50)                     null comment '更新人员',
    constraint uidx_detail_instance
        unique (status_main_instance_id, status_detail_model_id)
)
    comment '状态明细实例';

create table tc_status_detail_model
(
    id                   varchar(32)                      not null comment '主键'
        primary key,
    status_main_model_id varchar(32)                      not null comment '关联tc_status_main_model',
    form_main_model_id   varchar(30)                      null comment '关联tc_form_main_model',
    sequence             int          default 0           null comment '序号',
    code                 varchar(100)                     not null comment '编码',
    name                 varchar(100)                     not null comment '名称',
    other1               varchar(100) default ''          null comment '备用字段1',
    other2               varchar(100) default ''          null comment '备用字段2',
    other3               varchar(100) default ''          null comment '备用字段3',
    create_date          datetime     default (curdate()) null comment '创建日期',
    create_user          varchar(50)                      null comment '创建人员',
    update_date          datetime                         null comment '更新日期',
    update_user          varchar(50)                      null comment '更新人员',
    constraint uidx_detail_model
        unique (status_main_model_id, code)
)
    comment '状态明细模型';

create table tc_status_main_instance
(
    id                   varchar(32)                     not null comment '主键'
        primary key,
    status_main_model_id varchar(32)                     not null comment '关联tc_status_main_model',
    ref_id               varchar(32) default ''          not null comment '关联id',
    ref_type             varchar(50) default ''          not null comment '关联类型',
    ref_filed1           varchar(50) default ''          null comment '关联备用字段',
    ref_filed2           varchar(50) default ''          null comment '关联备用字段',
    name                 varchar(50) default ''          not null comment '实例名称',
    code                 varchar(50) default ''          not null comment '实例编码',
    finish               tinyint(1)  default 0           null comment '实例是否结束',
    create_date          datetime    default (curdate()) null comment '创建日期',
    create_user          varchar(50)                     null comment '创建人员',
    update_date          datetime                        null comment '更新日期',
    update_user          varchar(50)                     null comment '更新人员',
    constraint uidx_main_instance
        unique (status_main_model_id, ref_id, ref_type)
)
    comment '状态主表实例';

create table tc_status_main_model
(
    id          varchar(32)                  not null comment '主键'
        primary key,
    name        varchar(100)                 not null comment '名称',
    code        varchar(100)                 not null comment '编码',
    create_date datetime default (curdate()) null comment '创建日期',
    create_user varchar(50)                  null comment '创建人员',
    update_date datetime                     null comment '更新日期',
    update_user varchar(50)                  null comment '更新人员'
)
    comment '状态主表模型';

create table tc_system_front_token
(
    access_token varchar(100) not null comment 'token'
        primary key,
    name         varchar(50)  not null comment '名称'
) comment 'accessToken接口白名单放行表';


create table tc_user
(
    id              varchar(32)                      not null comment '主键'
        primary key,
    username        varchar(100)                     not null comment '账号',
    password        varchar(100)                     not null comment '密码',
    nick_name       varchar(50)  default ''          not null comment '姓名',
    judge_enable    int          default 1           not null comment '是否可用',
    create_user     varchar(50)                      null comment '创建人员',
    create_date     date         default (curdate()) null comment '创建日期',
    update_user     varchar(50)                      null comment '修改人员',
    update_date     date                             null comment '修改日期',
    job_number      varchar(50)  default ''          null comment '工号',
    user_type       int          default 0           not null comment '用户类型 0、系统创建  1、微信小程序获取',
    email           varchar(100) default ''          not null comment '邮箱地址',
    phone_number    varchar(50)  default ''          not null comment '手机号',
    manager_userid  varchar(32)                      null comment '对应管理人（领导）',
    last_login_time datetime                         null comment '最后一次登录时间',
    constraint uidx_username_jobnumber
        unique (username, job_number)
)
    comment '用户';

create index idx_user_type
    on tc_user (user_type);

create table tc_user_dept_rel
(
    id           varchar(32)              not null comment '主键'
        primary key,
    user_id      varchar(32)              not null comment '用户主键',
    dept_id      varchar(32)              not null comment '部门主键',
    judge_enable int  default 1           not null comment '是否可用',
    create_user  varchar(50)              null comment '创建人员',
    create_date  date default (curdate()) null comment '创建日期',
    update_user  varchar(50)              null comment '修改人员',
    update_date  date                     null comment '修改日期'
)
    comment '用户和部门关联表';

create index idx_dept_id
    on tc_user_dept_rel (dept_id);

create index idx_user_id
    on tc_user_dept_rel (user_id);

create table tc_user_role_rel
(
    id           varchar(32)              not null comment '主键'
        primary key,
    user_id      varchar(32)              not null comment '用户主键',
    role_id      varchar(32)              not null comment '角色主键',
    judge_enable int  default 1           not null comment '是否可用',
    create_user  varchar(50)              null comment '创建人员',
    create_date  date default (curdate()) null comment '创建日期',
    update_user  varchar(50)              null comment '修改人员',
    update_date  date                     null comment '修改日期'
)
    comment '用户和角色关联表';

create index idx_role_id
    on tc_user_role_rel (role_id);

create index idx_user_id
    on tc_user_role_rel (user_id);


