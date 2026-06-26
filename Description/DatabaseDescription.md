# 数据库设计

## 1. 用户表（user）

| 字段名      | 类型         | 可空 | 默认值            | 说明                     |
| ----------- | ------------ | ---- | ----------------- | ------------------------ |
| id          | bigint       | 否   | AUTO_INCREMENT    | 主键，自增               |
| username    | varchar(50)  | 否   | —                 | 唯一，用于登录           |
| email       | varchar(100) | 否   | —                 | 唯一邮箱                 |
| password    | varchar(255) | 否   | —                 | BCrypt加密密码           |
| avatar      | varchar(255) | 是   | NULL              | 用户头像                 |
| status      | tinyint      | 否   | 1                 | 账号状态（1正常，0禁用） |
| create_time | datetime     | 否   | CURRENT_TIMESTAMP | 创建时间                 |
| update_time | datetime     | 否   | CURRENT_TIMESTAMP | 更新时间                 |

---

## 2. 分类表（category）

| 字段名      | 类型         | 可空 | 默认值            | 说明         |
| ----------- | ------------ | ---- | ----------------- | ------------ |
| id          | bigint       | 否   | AUTO_INCREMENT    | 主键         |
| name        | varchar(50)  | 否   | —                 | 分类名称     |
| type        | tinyint      | 否   | 0                 | 0支出，1收入 |
| icon        | varchar(100) | 是   | NULL              | 分类图标     |
| sort_num    | int          | 否   | 0                 | 排序值       |
| create_time | datetime     | 否   | CURRENT_TIMESTAMP | 创建时间     |

### 初始化分类数据

#### 支出分类

- 餐饮
- 购物
- 交通
- 娱乐
- 学习
- 医疗
- 住房
- 其他

#### 收入分类

- 工资
- 兼职
- 奖学金
- 投资
- 红包
- 其他

---

## 3. 收支记录表（expense_record）

| 字段名      | 类型          | 可空 | 默认值            | 说明         |
| ----------- | ------------- | ---- | ----------------- | ------------ |
| id          | bigint        | 否   | AUTO_INCREMENT    | 主键         |
| user_id     | bigint        | 否   | —                 | 用户ID       |
| category_id | bigint        | 否   | —                 | 分类ID       |
| type        | tinyint       | 否   | 0                 | 0支出，1收入 |
| amount      | decimal(10,2) | 否   | 0.00              | 金额         |
| remark      | varchar(255)  | 是   | NULL              | 消费备注     |
| record_date | date          | 否   | —                 | 消费日期     |
| create_time | datetime      | 否   | CURRENT_TIMESTAMP | 创建时间     |
| update_time | datetime      | 否   | CURRENT_TIMESTAMP | 更新时间     |

### 索引建议

| 索引名称          | 字段                   |
| ----------------- | ---------------------- |
| idx_user_date     | (user_id, record_date) |
| idx_user_category | (user_id, category_id) |
| idx_user_type     | (user_id, type)        |

---

## 4. 预算表（budget）

| 字段名       | 类型          | 可空 | 默认值            | 说明                  |
| ------------ | ------------- | ---- | ----------------- | --------------------- |
| id           | bigint        | 否   | AUTO_INCREMENT    | 主键                  |
| user_id      | bigint        | 否   | —                 | 用户ID                |
| budget_month | varchar(7)    | 否   | —                 | 预算月份（如2026-06） |
| amount       | decimal(10,2) | 否   | 0.00              | 月预算金额            |
| create_time  | datetime      | 否   | CURRENT_TIMESTAMP | 创建时间              |
| update_time  | datetime      | 否   | CURRENT_TIMESTAMP | 更新时间              |

### 示例数据

| user_id | budget_month | amount  |
| ------- | ------------ | ------- |
| 1       | 2026-06      | 5000.00 |
| 1       | 2026-07      | 6000.00 |
| 1       | 2026-08      | 6500.00 |

---

## 5. AI消费报告表（ai_report）

| 字段名         | 类型          | 可空 | 默认值            | 说明           |
| -------------- | ------------- | ---- | ----------------- | -------------- |
| id             | bigint        | 否   | AUTO_INCREMENT    | 主键           |
| user_id        | bigint        | 否   | —                 | 用户ID         |
| report_month   | varchar(7)    | 否   | —                 | 报告月份       |
| total_income   | decimal(10,2) | 否   | 0.00              | 总收入         |
| total_expense  | decimal(10,2) | 否   | 0.00              | 总支出         |
| budget_amount  | decimal(10,2) | 否   | 0.00              | 月预算金额     |
| report_content | text          | 否   | —                 | AI分析报告内容 |
| create_time    | datetime      | 否   | CURRENT_TIMESTAMP | 创建时间       |

## 6. 用户反馈表（feedback）

| 字段名      | 类型          | 可空 | 默认值            | 说明                         |
| ----------- | ------------- | ---- | ----------------- | ---------------------------- |
| id          | bigint        | 否   | AUTO_INCREMENT    | 主键                         |
| user_id     | bigint        | 否   | —                 | 用户ID                       |
| content     | varchar(1000) | 否   | —                 | 反馈内容                     |
| status      | tinyint       | 否   | 0                 | 处理状态（0未处理，1已处理） |
| create_time | datetime      | 否   | CURRENT_TIMESTAMP | 提交时间                     |

## 7. 系统通知表（notification）

| 字段名      | 类型          | 可空 | 默认值            | 说明     |
| ----------- | ------------- | ---- | ----------------- | -------- |
| id          | bigint        | 否   | AUTO_INCREMENT    | 主键     |
| user_id     | bigint        | 否   | —                 | 用户ID   |
| title       | varchar(100)  | 否   | —                 | 通知标题 |
| content     | varchar(1000) | 否   | —                 | 通知内容 |
| is_read     | tinyint       | 否   | 0                 | 是否已读 |
| create_time | datetime      | 否   | CURRENT_TIMESTAMP | 创建时间 |

## 8. 记账打卡表（check_in_record）

| 字段名      | 类型     | 可空 | 默认值            | 说明     |
| ----------- | -------- | ---- | ----------------- | -------- |
| id          | bigint   | 否   | AUTO_INCREMENT    | 主键     |
| user_id     | bigint   | 否   | —                 | 用户ID   |
| check_date  | date     | 否   | —                 | 打卡日期 |
| create_time | datetime | 否   | CURRENT_TIMESTAMP | 创建时间 |

---

# 数据库关系图

```text
user
 │
 ├── expense_record
 │        │
 │        └── category
 │
 ├── budget
 │
 ├── ai_report
 │
 ├── feedback
 │
 ├── notification
 │
 └── check_in_record
```

# 最终数据库表

```text
consumption_plan
│
├── user
├── category
├── expense_record
├── budget
├── ai_report
├── feedback
├── notification
└── check_in_record
```