#创建xxl-job用户
create database xxl_job;
create user xxl_job identified by 'BbsDcDBNDD#g';
grant all privileges on xxl_job.* to xxl_job;
flush privileges;

#初始化系统用户密码：admin123456
INSERT INTO tc_user (id, username, password, nick_name, judge_enable, create_user, create_date, update_user, update_date, job_number, user_type, email, manager_userid) VALUES ('1774599009114132480', 'admin', 'd7242066c728817caca6f15f65174c2a', '超级管理员', 1, 'system', '2024-04-01', null, '2024-05-06', '', 0, '', null);

#初始化系统路由
INSERT INTO tc_router (id, path, name, title, redirect, icon, component, show_link, rank_int, keep_Alive, level, has_button, parent_id, judge_enable, create_user, create_date, update_user, update_date, hidden_Tag, judge_public, fullscreen) VALUES ('1780033250710917120', '/system', 'system', '系统管理', null, 'ri:information-line', null, 1, 99, 1, 0, null, null, 1, null, '2024-04-16', null, '2024-10-29', 0, 0, 0);
INSERT INTO tc_router (id, path, name, title, redirect, icon, component, show_link, rank_int, keep_Alive, level, has_button, parent_id, judge_enable, create_user, create_date, update_user, update_date, hidden_Tag, judge_public, fullscreen) VALUES ('1780033406940352512', '/system/dept', 'system-dept', '部门管理', null, null, '/views/system/dept.vue', 1, null, 1, 1, null, '1780033250710917120', 1, null, '2024-04-16', null, null, 0, 0, 0);
INSERT INTO tc_router (id, path, name, title, redirect, icon, component, show_link, rank_int, keep_Alive, level, has_button, parent_id, judge_enable, create_user, create_date, update_user, update_date, hidden_Tag, judge_public, fullscreen) VALUES ('1780033596116045824', '/system/menu', 'system-menu', '菜单管理', null, null, '/views/system/menu.vue', 1, null, 1, 1, null, '1780033250710917120', 1, null, '2024-04-16', null, null, 0, 0, 0);
INSERT INTO tc_router (id, path, name, title, redirect, icon, component, show_link, rank_int, keep_Alive, level, has_button, parent_id, judge_enable, create_user, create_date, update_user, update_date, hidden_Tag, judge_public, fullscreen) VALUES ('1780033757915516928', '/system/user', 'system-user', '用户管理', null, null, '/views/system/user.vue', 1, null, 1, 1, null, '1780033250710917120', 1, null, '2024-04-16', null, null, 0, 0, 0);
INSERT INTO tc_router (id, path, name, title, redirect, icon, component, show_link, rank_int, keep_Alive, level, has_button, parent_id, judge_enable, create_user, create_date, update_user, update_date, hidden_Tag, judge_public, fullscreen) VALUES ('1780034165706723328', '/system/role', 'system-role', '角色管理', null, null, '/views/system/role.vue', 1, null, 1, 1, null, '1780033250710917120', 1, null, '2024-04-16', null, null, 0, 0, 0);
INSERT INTO tc_router (id, path, name, title, redirect, icon, component, show_link, rank_int, keep_Alive, level, has_button, parent_id, judge_enable, create_user, create_date, update_user, update_date, hidden_Tag, judge_public, fullscreen) VALUES ('1785122809052270592', '/system/dictGroup', 'system-dictGroup', '字典管理', null, null, '/views/system/dict/dictGroup.vue', 1, null, 1, 1, null, '1780033250710917120', 1, null, '2024-04-30', null, null, 0, 0, 0);
INSERT INTO tc_router (id, path, name, title, redirect, icon, component, show_link, rank_int, keep_Alive, level, has_button, parent_id, judge_enable, create_user, create_date, update_user, update_date, hidden_Tag, judge_public, fullscreen) VALUES ('1837416264184762368', '/flowable/model', 'flowable-model', '流程管理', null, null, '/views/system/flowable/flowableModel.vue', 1, null, 1, 1, null, '1780033250710917120', 1, null, '2024-09-21', null, null, 0, 0, 0);
INSERT INTO tc_router (id, path, name, title, redirect, icon, component, show_link, rank_int, keep_Alive, level, has_button, parent_id, judge_enable, create_user, create_date, update_user, update_date, hidden_Tag, judge_public, fullscreen) VALUES ('1902208271465451520', '/msg/mainModel', 'msg-mainModel', '消息管理', null, null, '/views/system/msg/msgMainModel.vue', 1, null, 1, 1, null, '1780033250710917120', 1, null, '2025-03-19', null, null, 0, 0, 0);
INSERT INTO tc_router (id, path, name, title, redirect, icon, component, show_link, rank_int, keep_Alive, level, has_button, parent_id, judge_enable, create_user, create_date, update_user, update_date, hidden_Tag, judge_public, fullscreen) VALUES ('2005568542216425472', '/flowable/show', 'flowable-show', '待办流程', null, null, '/views/system/flowable/flowableShow.vue', 1, null, 1, 1, null, '1780033250710917120', 1, null, '2025-12-29', null, null, 0, 0, 0);





#############################################如下为业务测试表和数据生成#############################################
create database demo;
use demo;

#新增demo用户并授权
create user  demo_user identified by 'cF6{bG2@mZ';
grant all privileges on demo.* to demo_user;
flush privileges;

#插入路由配置
INSERT INTO tc_router (id, path, name, title, redirect, icon, component, show_link, rank_int, keep_Alive, level, has_button, parent_id, judge_enable, create_user, create_date, update_user, update_date, hidden_Tag, judge_public, fullscreen) VALUES ('1895380433647046656', '/business', 'business', '业务', null, 'ri:information-line', null, 1, 1, 1, 0, null, null, 1, null, '2025-02-28', null, null, 0, 0, 0);
INSERT INTO tc_router (id, path, name, title, redirect, icon, component, show_link, rank_int, keep_Alive, level, has_button, parent_id, judge_enable, create_user, create_date, update_user, update_date, hidden_Tag, judge_public, fullscreen) VALUES ('1895380535165980672', '/business/order', 'business-order', '订单', null, null, '/views/order/order.vue', 1, null, 1, 1, null, '1895380433647046656', 1, null, '2025-02-28', null, null, 0, 0, 0);

#添加订单业务测试表（建议业务表和系统表的前缀分开）
drop table if exists demo_order;
create table demo_order
(
    id          varchar(50) primary key,
    order_no    varchar(50),
    order_name  varchar(50),
    order_price decimal(10, 2),
    create_date datetime,
    update_date datetime,
    create_user varchar(50),
    update_user varchar(50)
);
#循环添加订单业务测试表数据1000条
DROP PROCEDURE IF EXISTS GenerateDemoOrders;
DELIMITER //

CREATE  PROCEDURE GenerateDemoOrders()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE current_id VARCHAR(50);
    DECLARE current_order_no VARCHAR(50);
    DECLARE current_order_name VARCHAR(50);
    DECLARE current_order_price DECIMAL(10, 2);
    DECLARE current_create_date DATETIME;
    DECLARE current_update_date DATETIME;
    DECLARE current_create_user VARCHAR(50);
    DECLARE current_update_user VARCHAR(50);

    -- 循环生成1000条数据
    WHILE i < 1000 DO
            -- 生成唯一的ID
            SET current_id = UUID();

            -- 生成订单号，确保唯一性
            SET current_order_no = CONCAT('ORDER_', LPAD(i, 6, '0'));

            -- 生成订单名称
            SET current_order_name = CONCAT('Order Name ', i);

            -- 生成随机的订单价格，范围在 10.00 到 1000.00 之间
            SET current_order_price = ROUND(10 + (RAND() * 990), 2);

            -- 生成创建和更新时间
            SET current_create_date = NOW() - INTERVAL FLOOR(RAND() * 365) DAY;
            SET current_update_date = current_create_date + INTERVAL FLOOR(RAND() * 30) DAY;

            -- 生成创建和更新用户
            SET current_create_user = CONCAT('User', FLOOR(RAND() * 100));
            SET current_update_user = CONCAT('User', FLOOR(RAND() * 100));

            -- 插入数据
INSERT INTO demo_order (id, order_no, order_name, order_price, create_date, update_date, create_user, update_user)
VALUES (current_id, current_order_no, current_order_name, current_order_price, current_create_date, current_update_date, current_create_user, current_update_user);

-- 增加计数器
SET i = i + 1;
END WHILE;
END //
DELIMITER ;

CALL GenerateDemoOrders();

select * from demo_order;