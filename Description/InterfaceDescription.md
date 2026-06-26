## 认证（无需 Token）
POST   /api/auth/register          注册
POST   /api/auth/login             登录 → 返回 token
POST   /api/auth/logout            登出

## 用户（需 Token）
GET    /api/user/info              查当前用户信息
PUT    /api/user/info              修改用户名/头像

## 分类（需 Token）
GET    /api/categories             查全部分类
GET    /api/categories?type=0      按类型筛选（0支出 1收入）

## 消费记录（需 Token）
POST   /api/records                新增记录
PUT    /api/records/{id}           修改记录
DELETE /api/records/{id}           删除记录
GET    /api/records                分页查询（type/categoryId/startDate/endDate/page/size）

## 预算（需 Token）
POST   /api/budget                 设置/修改预算
GET    /api/budget?month=          查预算执行情况（默认当月）

## AI 报告（需 Token）
POST   /api/report/generate?month= 生成报告（默认当月）
GET    /api/report?month=          查单月报告
GET    /api/reports                历史报告列表

## 打卡（需 Token）
POST   /api/checkin                今日打卡
GET    /api/checkin/status         查打卡状态

## 通知（需 Token）
GET    /api/notifications          通知列表
PUT    /api/notifications/read-all 全部标为已读
GET    /api/notifications/unread-count  未读数量

## 反馈（需 Token）
POST   /api/feedback               提交反馈
GET    /api/feedback/my            我的反馈列表

## Dashboard（需 Token）
GET    /api/dashboard              首页统计数据