# ConsumptionPlan

## 项目简介

### 项目名称

ConsumptionPlan

### 项目版本

v3.0

### 项目类型

前后端分离 Web 项目

### 开发周期

...

### 项目定位

个人消费分析系统是一款面向个人用户的消费记录与分析平台。

用户可以记录日常收入与支出信息，系统自动完成消费统计、预算分析以及 AI 消费报告生成，帮助用户了解消费结构、优化消费习惯并提升财务管理能力。

系统强调：

- 消费记录
- 数据统计
- 可视化分析
- 预算管理
- AI智能建议

而不仅仅是简单记账工具。

------

# 技术架构

## 前端技术栈

- Vue3
- Vite
- Element Plus
- Vue Router
- Pinia
- Axios
- ECharts

## 后端技术栈

- Spring Boot 3.x
- Spring Security
- JWT
- MyBatis Plus
- MySQL 8
- Lombok

## AI服务

- DeepSeek API

------

# 功能模块设计

系统共分为五大模块：

1. 用户模块
2. 消费记录模块
3. 统计分析模块
4. 预算管理模块
5. AI消费分析模块

------

# 页面设计

## 登录页（Login）

路径：

```text
/login
```

功能：

- 用户登录
- Token获取
- 登录状态保存

表单字段：

| 字段     | 说明   |
| -------- | ------ |
| username | 用户名 |
| password | 密码   |

------

## 注册页（Register）

路径：

```text
/register
```

功能：

- 用户注册
- 用户信息校验

字段：

| 字段            | 说明     |
| --------------- | -------- |
| username        | 用户名   |
| email           | 邮箱     |
| password        | 密码     |
| confirmPassword | 确认密码 |

校验规则：

- 用户名唯一
- 邮箱唯一
- 密码长度不少于6位

------

## 首页（Dashboard）

路径：

```text
/dashboard
```

### 数据概览

顶部展示四张统计卡片：

- 今日支出
- 今日收入
- 本月支出
- 本月结余

计算公式：

```java
结余 = 本月收入 - 本月支出
```

------

### 快速记账

字段：

| 字段     | 类型   |
| -------- | ------ |
| 收支类型 | Select |
| 分类     | Select |
| 金额     | Number |
| 日期     | Date   |
| 备注     | Input  |

收支类型：

- 支出
- 收入

消费分类：

- 餐饮
- 购物
- 交通
- 娱乐
- 学习
- 医疗
- 住房
- 其他

------

### 最近消费记录

展示最近10条消费记录。

字段：

- 分类
- 金额
- 日期
- 备注

------

## 统计分析页（Statistics）

路径：

```text
/statistics
```

------

### 分类占比分析

图表类型：

- 饼图

统计内容：

- 餐饮
- 购物
- 交通
- 娱乐
- 学习
- 医疗
- 住房
- 其他

展示：

- 消费金额
- 占比情况

------

### 月度消费趋势分析

图表类型：

- 折线图

横轴：

```text
日期
```

纵轴：

```text
消费金额
```

展示每日消费变化趋势。

------

### 收支对比分析

图表类型：

- 柱状图

展示：

- 收入
- 支出

用于分析资金流动情况。

------

### TOP消费分类

按照消费金额降序排列。

示例：

```text
TOP1 餐饮
TOP2 购物
TOP3 娱乐
```

------

### 大额消费榜

统计金额最高的10笔消费记录。

展示：

- 备注
- 金额
- 日期

------

## 预算管理页（Budget）

路径：

```text
/budget
```

### 月预算设置

用户可设置：

```text
月预算金额
```

例如：

```text
5000元
```

------

### 预算执行情况

计算公式：

```java
预算使用率 = 本月支出 ÷ 月预算 × 100%
```

展示：

```text
3500 / 5000

70%
```

------

### 预算预警

规则：

| 使用率   | 状态 |
| -------- | ---- |
| ≤80%     | 正常 |
| 80%~100% | 预警 |
| >100%    | 超支 |

颜色：

- 绿色
- 橙色
- 红色

------

## AI消费报告页（Report）

路径：

```text
/report
```

### 功能说明

用户点击按钮：

```text
生成本月消费报告
```

系统自动统计：

- 本月收入
- 本月支出
- 分类占比
- 预算执行率

并调用 DeepSeek API 生成消费分析报告。

------

### Prompt模板

```text
你是一名专业消费分析师。

用户本月消费情况如下：

总收入：{income}

总支出：{expense}

预算：{budget}

预算使用率：{rate}

分类占比：

餐饮：{food}%

购物：{shopping}%

交通：{traffic}%

娱乐：{entertainment}%

请分析用户消费结构并给出建议，
控制在200字以内。
```

------

## 个人中心（Profile）

路径：

```text
/profile
```

展示：

- 用户名
- 邮箱
- 注册时间

功能：

- 修改密码
- 退出登录

------

# 数据库设计

## 用户表（user）

```sql
CREATE TABLE user(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE,
    email VARCHAR(100) UNIQUE,
    password VARCHAR(100),
    create_time DATETIME
);
```

------

## 消费记录表（expense_record）

```sql
CREATE TABLE expense_record(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    user_id BIGINT,

    type TINYINT COMMENT '0支出 1收入',

    category VARCHAR(30),

    amount DECIMAL(10,2),

    remark VARCHAR(255),

    record_date DATE,

    create_time DATETIME
);
```

------

## 预算表（budget）

```sql
CREATE TABLE budget(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    user_id BIGINT UNIQUE,

    monthly_budget DECIMAL(10,2),

    update_time DATETIME
);
```

------

# API接口设计

## 用户模块

### 注册

```http
POST /api/auth/register
```

### 登录

```http
POST /api/auth/login
```

### 获取用户信息

```http
GET /api/user/info
```

------

## 消费记录模块

### 新增记录

```http
POST /api/record
```

### 查询记录

```http
GET /api/record/list
```

### 修改记录

```http
PUT /api/record/{id}
```

### 删除记录

```http
DELETE /api/record/{id}
```

------

## 统计模块

### 首页统计

```http
GET /api/statistics/dashboard
```

### 分类统计

```http
GET /api/statistics/category
```

### 趋势统计

```http
GET /api/statistics/trend
```

### TOP消费统计

```http
GET /api/statistics/top
```

------

## 预算模块

### 设置预算

```http
POST /api/budget
```

### 获取预算

```http
GET /api/budget
```

------

## AI分析模块

### 生成消费报告

```http
POST /api/report/generate
```

------

# 项目目录结构

## 前端目录

```text
src
├── api
├── assets
├── components
│   ├── charts
│   └── layout
├── pages
│   ├── Login
│   ├── Register
│   ├── Dashboard
│   ├── Statistics
│   ├── Budget
│   ├── Report
│   └── Profile
├── router
├── stores
├── styles
├── utils
└── App.vue
```

------

## 后端目录

```text
com.expense

├── controller
├── service
├── service.impl
├── mapper
├── entity
├── dto
├── vo
├── config
├── security
├── utils
└── common
```

------

# 开发计划

## Step1

- 数据库设计
- SpringBoot项目搭建
- 登录注册
- JWT认证

## Step2

- 消费记录CRUD
- Dashboard首页开发

## Step3

- 统计分析模块
- ECharts图表开发

## Step4

- 预算管理模块
- DeepSeek接入
- AI消费报告

## Step5

- UI优化
- Bug修复
- 项目测试
- 项目文档整理

------

# 项目亮点

## 数据可视化分析

通过：

- 饼图
- 折线图
- 柱状图

帮助用户快速了解消费结构。

------

## 预算预警机制

实时计算预算使用率。

预算接近上限时自动提醒用户。

------

## AI消费分析

结合真实消费数据生成分析报告。

为用户提供个性化消费建议。

------

## 前后端分离架构

采用：

```text
Vue3 + SpringBoot + JWT
```

实现现代化Web开发架构。

------

# 预期成果

完成后系统应具备：

- 用户管理
- 消费记录
- 数据统计
- 图表分析
- 预算管理
- AI消费报告

形成一个完整的个人消费分析平台。