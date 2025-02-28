# base_framework
管理系统快速开发框架

# 特别说明
## 环境配置
MYSQL8数据库（必选）
REDIS（可选）
RABBITMQ （可选）
MINIO（可选）

## 前置准备
使用项目下base.sql构建数据表，init.sql构建系统数据还有测试数据

## 对于前端
基于pure-admin精简版[https://github.com/pure-admin/pure-admin-thin](https://github.com/pure-admin/pure-admin-thin)改造

web_common是前端公共模块
web_demo是前端业务演示模块
两个都需要使用pnpm install

## 对于后端
base_business_demo为业务演示模块，其它均为系统模块，业务模块引用系统模块
base_business_demo模块中的配置文件application-prod.yaml有详细备注，默认使用application-local.yaml

## 效果展示

### 界面效果

![](https://github.com/MING789852/-/blob/main/%E7%95%8C%E9%9D%A2%E5%B1%95%E7%A4%BA.png)

### EXCEL导出效果展示

![](https://github.com/MING789852/-/blob/main/excel%E5%AF%BC%E5%87%BA%E6%95%88%E6%9E%9C.png)





