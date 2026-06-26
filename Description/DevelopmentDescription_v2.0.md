# 个人消费统计网站 — 开发设计说明文档

> 版本 v2.0 　|　 更新日期：2026-06-18 技术栈：Vue 3 + Spring Boot 3 + MySQL 8 + DeepSeek API

------

## 一、项目概述

### 1.1 项目目标

开发一款轻量级个人消费统计网站，帮助用户实现快速记账、直观查看消费结构、有效控制预算。核心价值在于：

- **记录快** —— 快捷表单 + 常用标签，秒级完成记账
- **看盘清** —— 多维图表（饼图/柱状图/折线图/环形图）直观呈现消费结构
- **预警灵** —— 日/月预算进度条实时预警，超支即时提醒
- **AI 顾问（VIP）** —— 基于 DeepSeek API 的个性化消费建议

### 1.2 技术栈

| 层级     | 技术选型                    | 说明                                          |
| -------- | --------------------------- | --------------------------------------------- |
| 前端框架 | Vue 3 + Vite + Element Plus | 组合式 API，按需加载，响应式布局              |
| 图表库   | ECharts 5                   | 饼图、柱状图、折线图、环形图，按需引入        |
| 状态管理 | Pinia                       | 存储用户信息、主题偏好、当前活跃账本          |
| 网络请求 | Axios                       | 统一拦截器处理 JWT Token 注入与错误码路由     |
| 后端框架 | Spring Boot 3.x             | 配合 Spring Security + JWT 独立鉴权           |
| ORM      | MyBatis-Plus                | LambdaQueryWrapper 简化查询，内置逻辑删除插件 |
| 数据库   | MySQL 8.0                   | 支持 JSON 字段备用扩展，软删除全表统一        |
| 缓存     | Redis 7                     | 统计接口缓存、AI 调用次数计数（兜底）         |
| AI 接口  | DeepSeek API                | VIP 专属消费顾问，SSE 流式响应                |
| 部署     | Docker + Nginx              | 前后端容器化，Nginx 反向代理 + gzip 压缩      |

------

## 二、功能需求清单

### 2.1 当日统计页（`/dashboard`）

| 功能模块     | 详细说明                                                     | 备注                                     |
| ------------ | ------------------------------------------------------------ | ---------------------------------------- |
| 快捷记账表单 | 分类下拉：正餐/零食/外卖/购物/娱乐/交通/人情世故/借出借入/其他。选择【购物】时联动展示子类（网购/实体店）。收支类型 Tab（支出/收入）置于表单顶部，选【借出借入】时自动联动：借出=支出，借入=收入，禁止手动修改 type。常用标签预设于用途输入框旁，点击自动填入。金额框自动聚焦，回车键快速提交。 | 提交成功后清空表单并刷新饼图和预算进度条 |
| 预算进度条   | 置于页面顶部，展示当日/当月预算进度。根据百分比渲染：<80% 绿色😊，80%~100% 橙色😐，>100% 红色😱。日/月限额在【我的】页面配置，普通用户与 VIP 均可使用。 | 按 bookId 隔离，不同账本独立进度条       |
| 当日饼图     | 展示当日各分类支出金额占比，悬浮 Tooltip 显示具体数值及百分比。无数据时展示空态插图，引导用户记账。 | 数据来源：`/api/records/day`             |

### 2.2 月度/年度统计页（`/statistics`）

| 功能模块       | 详细说明                                                     |
| -------------- | ------------------------------------------------------------ |
| 月度明细表     | 选中月份所有流水记录，支持按分类筛选，分页参数：`page`/`size`，默认 `size=20`，支持编辑和软删除。 |
| 月度柱状图     | 横轴当月日期（1~31），每日支出/收入双柱对比，颜色区分，顶部展示总计。 |
| 年度折线图     | 横轴 1~12 月，支出/收入两条趋势线，支持切换年份（下拉选择），历史年份数据走缓存。 |
| 收支构成环形图 | 当月总支出 vs 总收入占比，及支出分类占比，支持点击分类下钻查看明细。 |
| 支出榜前十     | 当月金额最高 10 条记录，展示用途、日期、分类、金额，支持点击跳转明细。 |

### 2.3 我的页面（`/profile`）

| 功能模块     | 详细说明                                                     |
| ------------ | ------------------------------------------------------------ |
| 个人信息     | 展示用户名、邮箱、会员状态（普通/VIP）及 VIP 到期时间。      |
| 修改密码     | 旧密码验证 + 新密码两次确认，后端 BCrypt 加密存储，接口：`PUT /api/user/password`。 |
| VIP 展厅模式 | AI 顾问/多账本/永久存档/数据导出四大功能锁定展示（灰色+锁图标）。点击任意功能弹出【3 天免费体验】弹窗，后端校验 `trial_used` 字段，未用过则开通并记录，已用过引导付费购买。 |
| 主题切换     | 亮/暖/暗三套主题，CSS Variables 实现，切换时修改 `document.documentElement.dataset.theme` 并调用 `/api/user/theme` 持久化。 |
| 预算设置     | 日限额和月限额分开配置，保存至 `budget_setting` 表，**按 `book_id` 隔离**。 |
| 账本管理     | 下拉切换活跃账本，可新建/重命名/删除（默认账本不可删），所有记账和统计接口均携带 `bookId` 参数实现数据隔离。 |

### 2.4 AI 消费顾问（VIP 专属）

用户输入消费相关问题，后端从数据库拉取当月统计摘要（总支出、分类占比、预算执行情况），拼接结构化 Prompt 后调用 DeepSeek API，采用 **SSE 流式响应**逐字输出建议。

- 每日每 VIP 用户调用上限：**20 次**，计数存于 `ai_usage_log` 表（按日期 upsert），服务重启不丢计数
- 初期不搭建知识库，采用 Prompt 工程方式实现
- 前端聊天气泡式 UI，支持用户历史对话记录展示（当次会话内）

------

## 三、图表类型决策说明

| 图表         | 类型   | 理由                                                 |
| ------------ | ------ | ---------------------------------------------------- |
| 当日消费构成 | 饼图   | 直观展示当日各类别金额占比                           |
| 月度日对比   | 柱状图 | 粒度细（按天），柱体高度差异醒目，快速定位哪天花超了 |
| 年度趋势     | 折线图 | 粒度粗（按月），折线清晰展现全年升降节奏             |
| 收支占比     | 环形图 | 比饼图更美观，适合展示支出与收入的整体比例关系       |

------

## 四、数据库设计（完整表结构）

> 💡 所有表均包含软删除字段 `is_deleted`（tinyint，0=正常，1=已删除）和 `deleted_at`（datetime），查询时统一过滤 `is_deleted=0`，MyBatis-Plus 逻辑删除插件自动处理。

### 4.1 用户表（`user`）

| 字段名          | 类型         | 可空 | 默认值         | 说明                            |
| --------------- | ------------ | ---- | -------------- | ------------------------------- |
| id              | bigint       | 否   | AUTO_INCREMENT | 主键，自增                      |
| username        | varchar(50)  | 否   | —              | 唯一，用于登录                  |
| password        | varchar(100) | 否   | —              | BCrypt 加密存储                 |
| email           | varchar(100) | 否   | —              | 唯一，注册时同 IP 每小时限 5 次 |
| role            | tinyint      | 否   | 0              | 0=普通用户，1=VIP               |
| vip_expire_time | datetime     | 是   | NULL           | NULL 表示无 VIP 或已过期        |
| trial_used      | tinyint      | 否   | 0              | 0=未用过 3 天体验，1=已用过     |
| theme           | varchar(10)  | 否   | 'light'        | light / warm / dark             |
| is_deleted      | tinyint      | 否   | 0              | 逻辑删除标记                    |
| deleted_at      | datetime     | 是   | NULL           | 删除时间                        |
| created_at      | datetime     | 否   | NOW()          | 创建时间                        |
| updated_at      | datetime     | 否   | NOW()          | 更新时间，ON UPDATE NOW()       |

### 4.2 消费记录表（`expense_record`）

| 字段名       | 类型          | 可空 | 默认值         | 说明                                                 |
| ------------ | ------------- | ---- | -------------- | ---------------------------------------------------- |
| id           | bigint        | 否   | AUTO_INCREMENT | 主键                                                 |
| user_id      | bigint        | 否   | —              | 外键关联 user.id                                     |
| book_id      | bigint        | 否   | 0              | 账本 ID，0=默认账本                                  |
| category     | varchar(20)   | 否   | —              | 正餐/零食/外卖/购物/娱乐/交通/人情世故/借出借入/其他 |
| sub_category | varchar(20)   | 是   | NULL           | 网购/实体店（仅购物分类使用）                        |
| amount       | decimal(10,2) | 否   | —              | 金额，单位元                                         |
| type         | tinyint       | 否   | 0              | 0=支出，1=收入；借出=0，借入=1，前端联动固定         |
| purpose      | varchar(100)  | 是   | NULL           | 用途描述，用户手动填写或点击标签填入                 |
| record_date  | date          | 否   | —              | 消费日期，默认今日                                   |
| is_deleted   | tinyint       | 否   | 0              | 软删除                                               |
| deleted_at   | datetime      | 是   | NULL           | 删除时间                                             |
| created_at   | datetime      | 否   | NOW()          | —                                                    |
| updated_at   | datetime      | 否   | NOW()          | ON UPDATE NOW()                                      |

> 💡 索引建议：`(user_id, book_id, record_date)` 联合索引、`(user_id, book_id, category)` 联合索引，覆盖主要查询场景。

### 4.3 预算设置表（`budget_setting`）

| 字段名       | 类型          | 可空 | 默认值         | 说明                                      |
| ------------ | ------------- | ---- | -------------- | ----------------------------------------- |
| id           | bigint        | 否   | AUTO_INCREMENT | 主键                                      |
| user_id      | bigint        | 否   | —              | 外键关联 user.id                          |
| book_id      | bigint        | 否   | 0              | **【v2.0 新增】** 账本 ID，预算按账本隔离 |
| type         | tinyint       | 否   | —              | 0=日预算，1=月预算                        |
| limit_amount | decimal(10,2) | 否   | —              | 限额，0 表示不限制                        |
| updated_at   | datetime      | 否   | NOW()          | ON UPDATE NOW()                           |

> ⚠️ v1.0 的 `budget_setting` 表无 `book_id`，v2.0 新增此字段，数据迁移时为存量数据补填 `book_id=0`。

### 4.4 账本表（`book`）

| 字段名     | 类型        | 可空 | 默认值         | 说明                                   |
| ---------- | ----------- | ---- | -------------- | -------------------------------------- |
| id         | bigint      | 否   | AUTO_INCREMENT | 主键                                   |
| user_id    | bigint      | 否   | —              | 外键关联 user.id                       |
| name       | varchar(50) | 否   | —              | 账本名称，如「家庭账本」               |
| is_default | tinyint     | 否   | 0              | 0=否，1=是，每用户有且仅有一个默认账本 |
| is_deleted | tinyint     | 否   | 0              | 软删除，默认账本不可删除               |
| created_at | datetime    | 否   | NOW()          | —                                      |

> 💡 `book_id=0` 为系统默认账本，不在 `book` 表中存记录，后端自动识别。普通用户仅可使用默认账本，VIP 可创建多个自定义账本。

### 4.5 AI 调用日志表（`ai_usage_log`）【v2.0 新增】

| 字段名     | 类型     | 可空 | 默认值         | 说明             |
| ---------- | -------- | ---- | -------------- | ---------------- |
| id         | bigint   | 否   | AUTO_INCREMENT | 主键             |
| user_id    | bigint   | 否   | —              | 外键关联 user.id |
| use_date   | date     | 否   | —              | 使用日期         |
| count      | int      | 否   | 0              | 当日已调用次数   |
| updated_at | datetime | 否   | NOW()          | ON UPDATE NOW()  |

> 💡 每日调用时执行 `INSERT ... ON DUPLICATE KEY UPDATE count=count+1`，唯一键为 `(user_id, use_date)`，服务重启不丢计数。

------

## 五、API 接口设计

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 错误码定义

| 错误码 | 触发场景                          | 前端处理                       |
| ------ | --------------------------------- | ------------------------------ |
| 400    | 参数校验失败                      | 表单内联提示错误信息           |
| 401    | Token 无效或已过期                | 自动跳转登录页，清除本地 Token |
| 403    | 权限不足（普通用户调用 VIP 接口） | 弹出 VIP 引导弹窗              |
| 404    | 资源不存在                        | Toast 提示                     |
| 429    | 请求频率超限（注册/AI 调用）      | Toast 提示并禁用按钮           |
| 500    | 服务器内部错误                    | Toast 提示，可上报 Sentry      |

### 5.1 认证模块（`/api/auth`）

| 方法 | 端点                 | 入参                      | 说明                                    |
| ---- | -------------------- | ------------------------- | --------------------------------------- |
| POST | `/api/auth/register` | username, password, email | 注册，同 IP 每小时限 5 次，邮箱唯一校验 |
| POST | `/api/auth/login`    | username, password        | 登录，返回 JWT Token（7 天有效）        |
| PUT  | `/api/user/password` | oldPassword, newPassword  | 修改密码，需旧密码验证，BCrypt 对比     |

### 5.2 记录模块（`/api/records`，需认证）

| 方法   | 端点                 | 参数                                                   | 说明                                            |
| ------ | -------------------- | ------------------------------------------------------ | ----------------------------------------------- |
| POST   | `/api/records`       | body: 完整记录字段                                     | 新增记录，bookId 默认 0，type 前端已联动固定    |
| GET    | `/api/records/day`   | `?date=2026-06-18&bookId=0`                            | 查询某日记录，用于当日饼图数据                  |
| GET    | `/api/records/month` | `?yearMonth=2026-06&bookId=0&category=&page=1&size=20` | 月度明细，支持分类过滤和分页                    |
| PUT    | `/api/records/{id}`  | body: 修改字段                                         | 修改记录，仅允许修改本人数据                    |
| DELETE | `/api/records/{id}`  | —                                                      | 软删除，设置 `is_deleted=1`，`deleted_at=NOW()` |

### 5.3 统计模块（`/api/statistics`，需认证）

| 方法 | 端点                          | 参数                          | 缓存策略                   | 说明                                    |
| ---- | ----------------------------- | ----------------------------- | -------------------------- | --------------------------------------- |
| GET  | `/api/statistics/monthly`     | `?yearMonth=2026-06&bookId=0` | 当月不缓存，历史月缓存 24h | 月度柱状图：每日支出/收入               |
| GET  | `/api/statistics/yearly`      | `?year=2026&bookId=0`         | 当年不缓存，历史年缓存 7d  | 年度折线图：每月支出/收入，支持切换年份 |
| GET  | `/api/statistics/composition` | `?yearMonth=2026-06&bookId=0` | 同 monthly                 | 月度收支构成环形图数据                  |
| GET  | `/api/statistics/top`         | `?yearMonth=2026-06&bookId=0` | 同 monthly                 | 当月支出榜前十                          |

> 💡 Redis Key 格式示例：`stats:monthly:{userId}:{bookId}:{yearMonth}`，TTL 86400s。当月数据写入新记录时主动删除对应 Key。

### 5.4 预算模块（`/api/budget`，需认证）

| 方法 | 端点                  | 参数                                   | 说明                                                    |
| ---- | --------------------- | -------------------------------------- | ------------------------------------------------------- |
| GET  | `/api/budget/status`  | `?bookId=0`                            | 返回当日/当月已花金额、限额、百分比，前端据此渲染进度条 |
| POST | `/api/budget/setting` | body: `type(0/1), limitAmount, bookId` | 设置日/月限额，bookId 默认 0                            |

### 5.5 VIP 模块（`/api/vip`，需认证）

| 方法 | 端点              | 说明                                                         |
| ---- | ----------------- | ------------------------------------------------------------ |
| POST | `/api/vip/trial`  | 开启 3 天免费体验。检查 `trial_used`：0 则开通并置为 1；1 则返回提示引导付费。 |
| GET  | `/api/vip/status` | 返回 VIP 状态、到期时间、剩余天数。                          |

### 5.6 AI 模块（`/api/ai`，需 VIP 权限）

| 方法 | 端点              | 说明                                                         |
| ---- | ----------------- | ------------------------------------------------------------ |
| POST | `/api/ai/advisor` | 传入用户问题（`question`），后端拉取近 30 天统计摘要拼入 Prompt，调用 DeepSeek API，SSE 流式返回。校验 `ai_usage_log`，当日 `count≥20` 则拒绝，返回 429。 |

> 💡 `Content-Type` 响应头为 `text/event-stream`，前端使用 `EventSource` 或 `fetch ReadableStream` 接收。

### 5.7 账本模块（`/api/books`，需认证）

| 方法   | 端点                      | 说明                                                      |
| ------ | ------------------------- | --------------------------------------------------------- |
| GET    | `/api/books`              | 获取当前用户所有账本列表（含默认账本 `book_id=0`）        |
| POST   | `/api/books`              | 新建账本，仅 VIP 可调用（普通用户返回 403 并弹 VIP 引导） |
| PUT    | `/api/books/{id}`         | 重命名账本                                                |
| DELETE | `/api/books/{id}`         | 软删除账本（`is_default=1` 的账本返回 400 拒绝删除）      |
| PUT    | `/api/books/{id}/default` | 切换默认账本，更新旧默认账本 `is_default=0`，新账本置 1   |

### 5.8 用户模块（`/api/user`，需认证）

| 方法 | 端点                | 说明                                                       |
| ---- | ------------------- | ---------------------------------------------------------- |
| GET  | `/api/user/profile` | 返回用户名、邮箱、role、vip_expire_time、trial_used、theme |
| PUT  | `/api/user/theme`   | 更新主题偏好（light/warm/dark），写入 `user.theme` 字段    |

------

## 六、前端开发规范

### 6.1 目录结构

```
src/
├── api/                # 接口定义（按模块拆分：auth.js / record.js / statistics.js ...）
├── assets/             # 静态资源（图标、空态插图）
├── components/         # 公共组件（BudgetBar / PieChart / TagInput ...）
├── composables/        # 组合式函数（useTheme / useBudget / useBook / useAI）
├── layouts/            # 布局组件（AppLayout：侧边栏+顶栏，含移动端底部导航）
├── pages/
│   ├── Dashboard/      # 当日统计（快捷记账 + 进度条 + 饼图）
│   ├── Statistics/     # 月度/年度统计
│   ├── Profile/        # 我的（个人信息 + VIP展厅 + 设置）
│   └── AI/             # AI 消费顾问（VIP 专属聊天页）
├── router/             # 路由配置（含权限守卫）
├── stores/             # Pinia 状态（user / theme / book）
├── styles/
│   ├── variables.css   # 三套主题 CSS Variables
│   └── global.css      # 全局重置和基础样式
└── utils/              # 工具函数（request.js / format.js / chart.js）
```

### 6.2 主题切换实现

采用 CSS Variables + `data-theme` 属性实现三套主题，切换时同步持久化至后端。

```css
/* styles/variables.css */
:root[data-theme="light"] {
  --color-primary: #1A6B8A;
  --color-bg:      #F5F7FA;
  --color-surface: #FFFFFF;
  --color-text:    #1A1A1A;
}
:root[data-theme="warm"] {
  --color-primary: #C0612B;
  --color-bg:      #FDF6EE;
  --color-surface: #FFFAF4;
  --color-text:    #2C1A0E;
}
:root[data-theme="dark"] {
  --color-primary: #4ECDC4;
  --color-bg:      #1A1A2E;
  --color-surface: #16213E;
  --color-text:    #E0E0E0;
}
// composables/useTheme.js
export function useTheme() {
  const userStore = useUserStore()
  function setTheme(theme) {
    document.documentElement.dataset.theme = theme
    userStore.theme = theme
    api.put('/api/user/theme', { theme })
  }
  return { setTheme }
}
```

### 6.3 响应式布局策略

| 断点                   | 布局方案          | 导航形式                          |
| ---------------------- | ----------------- | --------------------------------- |
| ≥ 1024px（桌面）       | 侧边栏 + 主内容区 | 左侧固定侧边栏，宽 220px          |
| 768px ~ 1023px（平板） | 侧边栏可折叠      | 汉堡按钮展开/收起，悬浮层覆盖     |
| < 768px（手机）        | 全屏内容区        | 底部固定 TabBar（首页/统计/我的） |

### 6.4 记账表单交互规范

- 收支 Tab 默认选中「支出」；选中「借出借入」分类时，`type` 字段自动联动（借出=支出 / 借入=收入）并**禁用** Tab 手动切换
- 金额输入框页面加载后自动 `focus`，`Enter` 键触发提交
- 常用标签按分类动态切换（如选「交通」显示 [加油]、[打车]、[地铁]）
- **提交成功**：Toast 提示 → 清空表单 → 刷新饼图和预算进度条 → 金额框重新 focus
- **提交失败**：行内展示字段错误，不清空已填内容

### 6.5 图表规范

| 图表         | 类型   | 空态处理             | 交互                                 |
| ------------ | ------ | -------------------- | ------------------------------------ |
| 当日消费构成 | 饼图   | 空态插图 + 引导文案  | hover Tooltip 显示金额和百分比       |
| 月度日对比   | 柱状图 | 提示「本月暂无数据」 | 点击柱体跳转当日明细                 |
| 年度趋势     | 折线图 | 提示「本年暂无数据」 | 支持切换年份，hover 显示月度汇总     |
| 收支占比     | 环形图 | 同上                 | 点击分类下钻至月度明细（过滤该分类） |

------

## 七、后端开发规范

### 7.1 JWT 配置

- 过期时间：7 天
- 请求头：`Authorization: Bearer <token>`
- Spring Security Filter 统一校验，Token 过期或无效返回 401
- 白名单：`/api/auth/register`、`/api/auth/login` 无需认证

### 7.2 权限层级

| 角色     | 可访问模块                   | 限制                                            |
| -------- | ---------------------------- | ----------------------------------------------- |
| 未登录   | `/api/auth/*`                | 仅注册和登录                                    |
| 普通用户 | 记录/统计/预算/用户/默认账本 | 不可调用 `/api/ai/*` 和多账本接口，触发返回 403 |
| VIP 用户 | 全部模块                     | AI 每日 20 次上限，多账本无数量限制             |

### 7.3 缓存策略（Redis）

| 数据类型         | Key 模式                         | TTL     | 失效时机                   |
| ---------------- | -------------------------------- | ------- | -------------------------- |
| 月度统计（历史） | `stats:monthly:{uid}:{bid}:{ym}` | 24 小时 | 该月有新记录写入时主动删除 |
| 年度统计（历史） | `stats:yearly:{uid}:{bid}:{y}`   | 7 天    | 该年有新记录写入时主动删除 |
| AI 调用次数      | `ai:count:{uid}:{date}`          | 25 小时 | 兜底冗余（主存 DB）        |

> 💡 当月/当年数据不走缓存，直接查库保证实时性。统计接口响应头加 `X-Cache: HIT/MISS` 便于排查。

### 7.4 注册防刷策略

- 同 IP 每小时注册次数限制：5 次，超出返回 429
- 实现方案：Redis Key = `reg:ip:{ip}:{hour}`，TTL = 3600s，原子 `INCR` 判断
- 同邮箱唯一校验（数据库唯一索引兜底）

### 7.5 AI 接口 SSE 实现

Spring Boot 使用 `SseEmitter` 实现流式输出，将 DeepSeek SSE 响应转发至前端：

```java
@PostMapping(value = "/api/ai/advisor", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter advisor(@RequestBody AdvisorRequest req, @AuthUser User user) {
    // 1. 校验 VIP 权限
    // 2. 查询 ai_usage_log，count >= 20 则抛 429
    // 3. 拉取统计摘要，拼接 Prompt
    // 4. 调用 DeepSeek API（流式），转发 SSE chunk
    // 5. 完成后 ai_usage_log count+1
    SseEmitter emitter = new SseEmitter(60_000L);
    executor.execute(() -> deepSeekService.streamAdvice(prompt, emitter));
    return emitter;
}
```

### 7.6 Prompt 模板

```
你是一位专业的个人财务顾问，语言简洁、建议具体可操作。
用户本月消费数据如下：
- 总支出：{totalExpense} 元，总收入：{totalIncome} 元
- 分类占比：餐饮 {食饮ratio}%，购物 {购物ratio}%，娱乐 {娱乐ratio}%，交通 {交通ratio}%，其他 {其他ratio}%
- 预算执行：日预算 {dayBudget} 元，日均支出 {dayAvg} 元（{dayPercent}%）
             月预算 {monthBudget} 元，已用 {monthUsed} 元（{monthPercent}%）
- 支出最多的3笔：{top3Records}

用户问题：{userQuestion}

请根据以上真实数据给出具体、可操作的建议，不超过 200 字，避免泛泛而谈。
```

------

## 八、安全与运营规范

### 8.1 密码与数据安全

- 密码存储：BCrypt（`strength=12`），禁止明文
- JWT Secret：环境变量注入，不得硬编码
- DeepSeek API Key：环境变量注入，不得提交至代码仓库（`.gitignore`）
- 所有 SQL 查询通过 MyBatis-Plus 参数化，禁止字符串拼接防 SQL 注入
- 前端敏感操作（删除账本、修改密码）加二次确认弹窗

### 8.2 数据隔离

- 所有查询接口强制带 `user_id` 条件（从 JWT 中解析，**不信任客户端传参**）
- `book_id` 合法性校验：确认该账本属于当前用户，防止越权访问
- 软删除数据在查询层统一过滤（MyBatis-Plus 逻辑删除插件自动处理）

### 8.3 前端安全

- 路由守卫：未登录跳转 `/login`，VIP 路由（`/ai`）检查 `role=1` 否则弹引导
- Token 存储于 `localStorage`，Axios 拦截器自动注入请求头
- 401 响应自动清除 Token 并跳转登录页，防止 Token 过期后继续操作

### 8.4 版本迭代说明（v1.0 → v2.0 变更汇总）

| 变更点       | v1.0               | v2.0                           | 迁移说明                        |
| ------------ | ------------------ | ------------------------------ | ------------------------------- |
| 预算隔离     | 无 book_id         | `budget_setting` 加 `book_id`  | 存量数据补填 `book_id=0`        |
| 软删除       | 硬删除             | 全表软删除字段                 | 新表直接建，旧表加字段+默认值 0 |
| 借出借入规则 | 无明确约定         | 借出=支出/借入=收入，前端联动  | 前端表单联动，无需数据迁移      |
| AI 计数存储  | 未说明             | `ai_usage_log` 表（DB 持久化） | 新建表                          |
| AI 响应方式  | 同步等待           | SSE 流式输出                   | 后端接口改造，前端接入 SSE      |
| 移动端导航   | 汉堡菜单（侧边栏） | < 768px 改为底部 Tab 导航      | 纯前端布局调整                  |
| 错误码 429   | 无                 | 注册防刷 + AI 超限             | 新增 Redis 频控逻辑             |

------

## 九、部署配置参考

### 9.1 环境变量清单

| 变量名             | 说明                      | 示例值                            |
| ------------------ | ------------------------- | --------------------------------- |
| `JWT_SECRET`       | JWT 签名密钥（≥256 位）   | 随机生成，不得提交                |
| `DEEPSEEK_API_KEY` | DeepSeek API 密钥         | `sk-xxxxxxxxxxxxxxxx`             |
| `MYSQL_URL`        | 数据库连接串              | `jdbc:mysql://db:3306/expense_db` |
| `MYSQL_USERNAME`   | 数据库用户名              | `expense_user`                    |
| `MYSQL_PASSWORD`   | 数据库密码                | 强密码                            |
| `REDIS_HOST`       | Redis 主机                | `redis`                           |
| `REDIS_PORT`       | Redis 端口                | `6379`                            |
| `REG_IP_LIMIT`     | 注册 IP 每小时限制次数    | `5`                               |
| `AI_DAILY_LIMIT`   | AI 每日每用户最大调用次数 | `20`                              |

### 9.2 Docker Compose 服务结构

```yaml
services:
  db:        # MySQL 8.0，挂载 init SQL，持久化 volume
  redis:     # Redis 7，持久化 AOF
  backend:   # Spring Boot JAR，依赖 db/redis，注入环境变量
  frontend:  # Vite build 产物，Nginx 静态托管
  nginx:     # 反向代理，/api/* → backend:8080，其余 → frontend:80
```

### 9.3 Nginx 关键配置

```nginx
# API 反向代理
location /api/ {
    proxy_pass http://backend:8080;
    proxy_set_header Authorization $http_authorization;
    # SSE 流式响应必须关闭 buffering
    proxy_buffering  off;
    proxy_cache      off;
    proxy_read_timeout 120s;
}

# 前端 SPA History 模式
location / {
    try_files $uri $uri/ /index.html;
    gzip on;
    gzip_types text/javascript application/javascript text/css;
}
```

> ⚠️ SSE 接口（`/api/ai/advisor`）必须设置 `proxy_buffering off`，否则 Nginx 会缓冲整个响应，流式效果完全失效。

------

*个人消费统计网站开发设计说明文档 v2.0　|　2026-06-18*