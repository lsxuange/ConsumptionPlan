# ConsumptionPlan 前端项目结构说明

## 项目概述

ConsumptionPlan 是一个消费计划管理 Web 应用前端，提供消费记录管理、统计分析、预算跟踪、AI 报表生成等功能。采用清新白绿风视觉主题，白色侧边栏搭配绿色强调色，界面干净专业。

---

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3（Composition API + `<script setup>`） | ^3.5.34 |
| 构建工具 | Vite | ^8.0.12 |
| 路由 | Vue Router 5 | ^5.1.0 |
| 状态管理 | Pinia 3 | ^3.0.4 |
| HTTP 客户端 | Axios | ^1.18.0 |
| UI 组件库 | Element Plus | ^2.14.2 |
| 图标 | @element-plus/icons-vue | 随 Element Plus 安装 |
| 图表库 | ECharts | ^6.1.0 |
| Markdown 渲染 | markdown-it | ^14.2.0 |
| 日期处理 | dayjs | Element Plus 传递依赖 |

---

## 目录结构

```
ConsumptionPlan-web/
├── public/                          # 静态资源
│   ├── favicon.svg                  # 网站图标
│   └── icons.svg                    # SVG 图标集
├── src/
│   ├── api/                         # API 接口模块（每个文件对应一个业务域）
│   │   ├── auth.js                  # 认证（登录/注册/退出）          16 行
│   │   ├── budget.js                # 预算管理（保存/查询/历史）        15 行
│   │   ├── category.js              # 分类（获取分类列表）               6 行
│   │   ├── checkin.js               # 签到（打卡/状态/月打卡日期）      17 行
│   │   ├── dashboard.js             # 仪表盘数据                        7 行
│   │   ├── feedback.js              # 反馈（提交/查询历史）             11 行
│   │   ├── notification.js          # 通知（列表/全部已读/未读数）      16 行
│   │   ├── record.js                # 消费记录（CRUD + 分页查询）       21 行
│   │   ├── report.js                # AI 报表（生成/查询/历史列表）     16 行
│   │   └── user.js                  # 用户信息（获取/更新）             11 行
│   ├── assets/                      # 项目内资源文件
│   │   └── hero.png                 # 首页主图
│   ├── components/                  # 公共组件（当前为空）
│   ├── router/
│   │   └── index.js                 # 路由配置 + 导航守卫               72 行
│   ├── stores/
│   │   └── userStore.js             # Pinia 用户状态管理                25 行
│   ├── utils/
│   │   └── request.js               # Axios 封装（拦截器）              50 行
│   ├── views/
│   │   ├── BudgetView.vue           # 预算管理页                       367 行
│   │   ├── DashboardView.vue        # 首页仪表盘                       492 行
│   │   ├── LoginView.vue            # 登录页                           160 行
│   │   ├── ProfileView.vue          # 个人中心                         814 行
│   │   ├── RecordView.vue           # 消费记录页                       414 行
│   │   ├── RegisterView.vue         # 注册页                           180 行
│   │   ├── ReportView.vue           # AI 报告页                        335 行
│   │   └── StatisticsView.vue       # 统计分析页                       341 行
│   ├── App.vue                      # 根组件（布局 + 侧边导航 + 顶部栏） 302 行
│   ├── main.js                      # 应用入口（插件注册）               27 行
│   └── style.css                    # 全局样式（CSS 变量 + 重置 + EP 覆盖） 77 行
├── index.html                       # HTML 入口
├── package.json                     # 依赖配置
├── vite.config.js                   # Vite 构建配置
└── ProjectStructure_fronted.md      # 本文件
```

---

## 路由配置

所有路由均使用 **Vue Router 5**，采用 `createWebHistory` 模式，路由懒加载（`() => import(...)`），导航守卫统一使用 `return '/login'` 语法。

| 路径 | 名称 | 页面 | 需认证（meta.requiresAuth） |
|------|------|------|--------|
| `/login` | Login | 登录 | ❌ |
| `/register` | Register | 注册 | ❌ |
| `/` | — | 重定向 `/dashboard` | — |
| `/dashboard` | Dashboard | 首页仪表盘 | ✅ |
| `/records` | Records | 消费记录 | ✅ |
| `/statistics` | Statistics | 统计分析 | ✅ |
| `/budget` | Budget | 预算管理 | ✅ |
| `/report` | Report | AI 报告 | ✅ |
| `/profile` | Profile | 个人中心 | ✅ |

**导航守卫逻辑**：`router.beforeEach` 中检查 `localStorage.token`，若目标路由 `meta.requiresAuth === true` 且 token 不存在，则 `return '/login'` 重定向。

---

## API 模块说明

所有 API 文件通过 `utils/request.js` 发出请求，**baseURL 为 `/`**，由 Vite 开发代理转发至 `http://localhost:8080`。

### 通用约定

- 所有接口路径以 `/api` 开头
- 统一响应格式：`{ code: 200, message: "success", data: {...} }`
- `request.js` 响应拦截器自动解包 `data` 字段
- 认证 token 存储于 `localStorage`，key 为 `"token"`
- 请求头携带：`Authorization: Bearer <token>`

### type 字段映射规则（重要）

| 接口 | 字段含义 |
|------|---------|
| `/api/categories` | `type` 参数为 Integer：**0 = 支出，1 = 收入** |
| `/api/records`（提交 body） | `type` 字段为 Integer：**0 = 支出，1 = 收入** |
| `/api/records`（查询参数） | `type` 参数为 Integer：0/1，不传则查全部 |

> 分类接口和记录接口现已统一使用 **0=支出，1=收入**，前端直接传整数，不做任何字符串转换。

### 各模块接口一览

| 文件 | 方法 | 接口 | 说明 |
|------|------|------|------|
| `auth.js` | `register(data)` | POST `/api/auth/register` | 注册 |
| `auth.js` | `login(data)` | POST `/api/auth/login` | 登录 |
| `auth.js` | `logout()` | POST `/api/auth/logout` | 退出登录 |
| `user.js` | `getUserInfo()` | GET `/api/user/info` | 获取用户信息 |
| `user.js` | `updateUserInfo(data)` | PUT `/api/user/info` | 更新用户名/头像 |
| `record.js` | `addRecord(data)` | POST `/api/records` | 添加记录 |
| `record.js` | `updateRecord(id, data)` | PUT `/api/records/{id}` | 更新记录 |
| `record.js` | `deleteRecord(id)` | DELETE `/api/records/{id}` | 删除记录 |
| `record.js` | `getRecords(params)` | GET `/api/records` | 分页查询记录 |
| `category.js` | `getCategories(type)` | GET `/api/categories` | 获取分类列表 |
| `budget.js` | `saveBudget(data)` | POST `/api/budget` | 设置/更新预算 |
| `budget.js` | `getBudget(month)` | GET `/api/budget` | 查询某月预算 |
| `budget.js` | `getBudgetHistory()` | GET `/api/budget/history` | 获取预算历史 |
| `report.js` | `generateReport(month)` | POST `/api/report/generate` | 生成 AI 报告 |
| `report.js` | `getReport(month)` | GET `/api/report` | 查询某月报告 |
| `report.js` | `listReports()` | GET `/api/reports` | 报告历史列表 |
| `checkin.js` | `checkIn()` | POST `/api/checkin` | 签到 |
| `checkin.js` | `getCheckInStatus()` | GET `/api/checkin/status` | 查询签到状态 |
| `checkin.js` | `getCheckedDates(month)` | GET `/api/checkin/month` | 获取某月打卡日期列表 |
| `notification.js` | `getNotifications()` | GET `/api/notifications` | 通知列表 |
| `notification.js` | `markAllRead()` | PUT `/api/notifications/read-all` | 全部标为已读 |
| `notification.js` | `getUnreadCount()` | GET `/api/notifications/unread-count` | 未读数量 |
| `feedback.js` | `submitFeedback(data)` | POST `/api/feedback` | 提交反馈 |
| `feedback.js` | `getMyFeedback()` | GET `/api/feedback/my` | 查询我的反馈 |
| `dashboard.js` | `getDashboard()` | GET `/api/dashboard` | 仪表盘数据 |

---

## 状态管理（Pinia）

### userStore.js

使用 `defineStore('user', () => {...})` 组合式写法。

| 属性/方法 | 说明 |
|----------|------|
| `token` | JWT token，初始化从 `localStorage.getItem('token')` 读取 |
| `username` | 当前用户名 |
| `avatar` | 头像 URL |
| `setUser(token, username, avatar)` | 登录成功后存储信息，同时写入 `localStorage` |
| `clearUser()` | 退出登录时清除所有信息，同时删除 `localStorage` 中的 token |

---

## 入口文件（main.js）

`main.js` 按顺序完成以下注册：

1. **createPinia()** — 启用 Pinia 状态管理
2. **router** — 挂载 Vue Router
3. **ElementPlus** — 全量引入 Element Plus 组件库 + CSS
4. **Element Plus Icons** — 遍历 `@element-plus/icons-vue` 全部注册为全局组件
5. **$echarts** — 将 ECharts 挂载到 `app.config.globalProperties`
6. **style.css** — 引入全局样式

---

## 根组件布局（App.vue）

App.vue 根据当前路由动态切换两种布局：

| 场景 | 布局方式 |
|------|----------|
| `/login` 或 `/register` | 纯净全屏页面，无侧边栏和顶部栏 |
| 其他认证页面 | 自定义 flex 布局：固定侧边栏（220px）+ 主内容区（sticky 顶部栏 + 页面内容） |

### 侧边栏结构

侧边栏采用**清新白绿风**主题，白色背景 + 绿色边框，不使用 Element Plus 的 `el-menu` 组件，而是纯 CSS + `router-link` 实现导航。

```
+------------------+
|  Logo 区          |  emoji 图标 + 应用名
+------------------+
|  导航菜单          |  router-link + exact-active-class
|                   |  激活项：浅绿背景 + 绿色文字 + 左侧绿色指示条
+------------------+
|  底部用户信息区    |  头像 + 用户名
+------------------+
```

**导航菜单项**（`menuItems` 数组）：

| 路径 | 图标 | 标签 |
|------|------|------|
| `/dashboard` | 🏠 | 首页 |
| `/records` | 📝 | 消费记录 |
| `/statistics` | 📊 | 统计分析 |
| `/budget` | 💳 | 预算管理 |
| `/report` | 🤖 | AI报告 |
| `/profile` | 👤 | 我的 |

**导航高亮**：同时设置 `active-class` 和 `exact-active-class` 为 `nav-item--active`。

### 顶部栏结构

- 页面标题通过 `pageTitleMap` 映射当前 `route.path` 动态显示
- 通知按钮使用 `el-badge` 显示未读消息数（`getUnreadCount` API）
- 用户名显示带 fallback：`userStore.username || '用户'`

### 主题配色（CSS 变量）

```css
:root {
  --color-primary: #00b96b;           /* 主色：清新绿 */
  --color-primary-dark: #00a35c;      /* 深绿（hover） */
  --color-primary-light: #e8f9f2;     /* 浅绿（激活背景） */
  --color-sidebar-bg: #ffffff;        /* 侧边栏背景：白色 */
  --color-sidebar-text: #606266;      /* 导航文字：灰色 */
  --color-sidebar-active-bg: #e8f9f2; /* 激活项背景 */
  --color-sidebar-active-text: #00b96b;/* 激活项文字 */
  --color-bg: #f5f7fa;                /* 页面背景：浅灰 */
  --color-border: #e4e7ed;            /* 边框色 */
}
```

---

## Axios 封装（request.js）

### 配置

```js
baseURL: '/'          // 走 Vite 代理，不直接写后端地址
timeout: 10000        // 10 秒超时
```

### 请求拦截器
- 自动从 `localStorage.getItem('token')` 读取 token
- 设置到 `Authorization: Bearer <token>` 请求头

### 响应拦截器

| 状态码 | 处理 |
|--------|------|
| `code === 200` | 直接返回 `data` 字段 |
| `code === 401` | 清除 token，`ElMessage.error('登录已过期')`，跳转 `/login` |
| 其他 code | `ElMessage.error(message)` 显示错误 |
| HTTP 错误 | 显示后端返回的 message 或 `'网络错误'` |

---

## 公共组件（components/）

当前 `src/components/` 目录为空，所有页面逻辑均在 `views/` 中的单文件组件内实现。后续可提取公共组件（如分页器、统计卡片、图表容器等）到此目录。

---

## 页面功能概览

| 页面 | 文件 | 行数 | 核心功能 |
|------|------|------|---------|
| 登录 | `LoginView.vue` | 160 | 用户名/密码表单，登录后存 token 跳转 /dashboard |
| 注册 | `RegisterView.vue` | 180 | 用户名/邮箱/密码表单（含邮箱格式校验），注册成功跳转 /login |
| 首页 | `DashboardView.vue` | 492 | 4 统计卡片 + 5 个 ECharts 图表（详见下方） |
| 消费记录 | `RecordView.vue` | 414 | CRUD 列表 + 筛选（类型/分类/日期范围）+ 分页 + 弹窗编辑 |
| 统计分析 | `StatisticsView.vue` | 341 | 月份选择 + 支出分类饼图 + 收入来源饼图 + 消费日历热力图 |
| 预算管理 | `BudgetView.vue` | 367 | 月选择 + 四统计卡片 + 预警横幅 + 使用率环形图 + 历史趋势 + 历史表 |
| AI 报告 | `ReportView.vue` | 335 | markdown-it 渲染报告 + 月份选择 + 生成/查询 + 历史时间线 |
| 个人中心 | `ProfileView.vue` | 814 | 左右两列 6 个功能区块（详见下方） |

### DashboardView 图表布局

首页仪表盘包含 **5 个 ECharts 图表**，四行排列：

```
+----------------------------------------------------------+
| Row 1: [本月收入] [本月支出] [本月结余] [预算使用率]          |
+----------------------------------------------------------+
| Row 2: [        本月每日收支柱状图（全宽）        ]          |
+---------------------------+------------------------------+
| Row 3: [收入来源饼图]      | [预算执行仪表盘]               |
+---------------------------+------------------------------+
| Row 4: [支出分类饼图]      | [收支趋势折线图]               |
+---------------------------+------------------------------+
```

| 图表 | DOM 引用 | ECharts 类型 | 数据源 |
|------|---------|-------------|--------|
| 每日收支柱状图 | `dailyChartRef` | bar（双柱） | `getRecords()` 按日期+类型分组 |
| 收入来源饼图 | `incomeChartRef` | pie（环形） | `data.incomeSourceList` |
| 预算执行仪表盘 | `gaugeChartRef` | gauge（三色） | `data.budgetUsageRate` |
| 支出分类饼图 | `expenseChartRef` | pie（环形） | `data.expenseCategoryList` |
| 收支趋势折线图 | `trendChartRef` | line（双线） | `data.trendList` |

**ECharts 生命周期管理**：
- `onMounted` 中 `setTimeout(100ms)` 后初始化所有图表
- `window.resize` 事件监听 - 所有图表实例调用 `resize()`
- `onBeforeUnmount` 中移除监听 + 所有图表实例调用 `dispose()`

### StatisticsView 图表布局

统计分析页包含 **2 个 ECharts 图表 + 1 个纯 CSS 热力图**：

```
+----------------------------------------------------------+
| 顶部：[选择月份 el-date-picker]                            |
+---------------------------+------------------------------+
| Row 1: [支出分类饼图]      | [收入来源饼图]                 |
+---------------------------+------------------------------+
| Row 2: [        消费日历热力图（全宽，纯 CSS）         ]    |
+----------------------------------------------------------+
```

| 图表 | DOM 引用 | 类型 | 数据源 |
|------|---------|------|--------|
| 支出分类饼图 | `expenseChartRef` | ECharts pie（环形） | `getRecords()` type=0 按 category 分组 |
| 收入来源饼图 | `incomeChartRef` | ECharts pie（环形） | `getRecords()` type=1 按 category 分组 |
| 消费日历热力图 | 无 DOM ref（纯 CSS grid） | 纯 Vue+CSS | `getRecords()` type=0 按 recordDate 聚合 |

**热力图实现要点**：
- 数据通过 `loadHeatmapData()` 独立请求 type=0 的支出记录，按 `recordDate` 聚合求和
- `fillerCount`（computed）：当月第一天是周几，前面填充占位格（周一起算）
- `calendarDays`（computed）：生成当月每日数据对象，含 `date`、`day`、`amount`、`level`（0~4）
- 5 级消费着色：`level-0`（无消费 #ebedf0）到 `level-4`（高消费 #196127），类似 GitHub Contribution Graph
- `.heatmap-wrapper { max-height: 420px }` 限高，一屏内可见
- 月份切换时通过 `watch` 重新加载数据和热力图

### BudgetView 页面结构

预算管理页包含 **6 个区块 + 1 个弹窗**：

```
+----------------------------------------------------------+
| 1 操作栏：[月份选择] [查询] [修改预算]                      |
+----------------------------------------------------------+
| 2 四卡片行：[预算金额] [本月支出] [剩余预算] [使用率]        |
+----------------------------------------------------------+
| 3 预警横幅（自定义 div，非 el-alert）                      |
+---------------------------+------------------------------+
| 4 使用率环形图（ECharts）| 趋势折线图（ECharts）            |
+---------------------------+------------------------------+
| 5 历史记录表：el-table + el-progress 使用率进度条          |
+----------------------------------------------------------+
| 6 修改预算弹窗：el-dialog + el-date-picker + el-input-number |
+----------------------------------------------------------+
```

**预警横幅**（`warningInfo` computed，3 级预警）：

| 条件 | 级别 | 类型 | 颜色 |
|------|------|------|------|
| usageRate >= 100% | 预算已超支 | `error` | 红色（#f56c6c） |
| usageRate >= 80% | 预算预警 | `warning` | 橙色（#e6a23c） |
| usageRate >= 60% | 预算提醒 | `info` | 蓝色（#409eff） |

**使用率环形图**：ECharts `pie` 类型，`radius: ['55%', '75%']`，中心 label 显示百分比，颜色随使用率变化（红/橙/绿）。

**历史趋势折线图**：ECharts `line` 类型，预算线虚线绿色 + 支出线实线红色，X 轴为历史月份列表，数据来自 `getBudgetHistory()`。

**历史记录表**：`el-table` + `el-progress` 展示使用率进度条，status 根据使用率切换（exception/warning/空）。

**依赖**：BudgetView 使用 `dayjs` 处理月份格式（`dayjs().format('YYYY-MM')`）。

### ReportView 页面结构

AI 报告页包含 **3 个区块**：

| 区块 | 内容 |
|------|------|
| 顶部操作栏 | 月份选择 el-date-picker + 生成AI报告按钮 |
| 报告展示区 | 有报告时：三统计卡片 + v-html 渲染 Markdown 正文；无报告时：el-empty 占位 |
| 历史报告列表 | el-timeline + el-timeline-item，点击可切换查看 |

**Markdown 渲染**：使用 `markdown-it` 库将后端返回的 Markdown 字符串转换为 HTML，通过 `v-html` 渲染到 `.report-content` 容器内。

### ProfileView 页面结构

个人中心页面采用**左右两列 flex 布局**，共 **6 个功能区块**：

```
+---------------------------+-------------------------------+
| 左列 (flex: 0 0 52%)      | 右列 (flex: 1)                |
|                           |                               |
| 1 个人信息                 | 3 通知中心                     |
|   头像/用户名/邮箱/         |   通知列表（已读/未读样式）     |
|   注册时间 + 编辑按钮       |   + 未读徽标 + 全部已读按钮    |
|                           |                               |
| 2 记账打卡                 | 4 意见反馈                     |
|   连续/本月打卡天数         |   文本域提交反馈（1000字限制）  |
|   + 月历网格（标记打卡日）  |   + 历史反馈列表                |
|   + 今日打卡按钮            |                               |
|                           | 5 退出登录                     |
| 6 消费健康度               |   确认弹窗 -> logout -> 跳转    |
|   四维度评分环形分数圈      |                               |
|   + 等级标签 + 指标列表     |                               |
+---------------------------+-------------------------------+
```

**布局 CSS**：

```css
.profile-layout { display: flex; gap: 20px; align-items: flex-start; }
.profile-left   { flex: 0 0 52%; display: flex; flex-direction: column; gap: 16px; }
.profile-right  { flex: 1; display: flex; flex-direction: column; gap: 16px; }
```

**消费健康度**（`healthScore` computed，四维度评分，总分 100）：

| 维度 | 满分 | 评分规则 |
|------|------|---------|
| 预算执行 | 35 分 | usageRate <= 80% -> 35 分；<= 100% -> 20 分；> 100% -> 0 分；无数据 -> 20 分 |
| 收支盈余 | 30 分 | balance > 0 -> 30 分；= 0 -> 15 分；< 0 -> 0 分 |
| 连续记账 | 20 分 | >= 7 天 -> 20 分；>= 3 天 -> 12 分；>= 1 天 -> 6 分；0 天 -> 0 分 |
| 分类均衡 | 15 分 | 最高类占比 <= 50% -> 15 分；<= 70% -> 8 分；> 70% -> 0 分；无数据 -> 15 分 |

| 等级 | 分数范围 | 颜色 |
|------|---------|------|
| S | >= 90 | #00b96b（绿） |
| A | >= 75 | #409eff（蓝） |
| B | >= 60 | #e6a23c（橙） |
| C | < 60 | #f56c6c（红） |

**消费健康度数据依赖**（不新增 API，复用已有数据）：
- `budgetData`（usageRate）<- `getBudget(month)` API
- `dashboardData`（monthIncome / monthExpense / expenseCategoryList）<- `getDashboard()` API
- `checkInData`（continuousDays）<- `getCheckInStatus()` API

**打卡日历**：使用 `dayjs` 处理日期计算（日历网格生成、月份切换），`checkedDates` 从 `getCheckedDates(month)` 获取，日历网格中标记已打卡日期。

---

## 全局样式（style.css）

`style.css` 在 `main.js` 中引入，承担以下职责：

| 模块 | 说明 |
|------|------|
| CSS 变量 | 定义全局主题色（与 App.vue 中的 `:root` 同步） |
| 全局重置 | `*`、`*::before`、`*::after` 选择器重置 margin/padding/box-sizing |
| 字体设置 | `-apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif` |
| 字体渲染 | `-webkit-font-smoothing: antialiased` + `-moz-osx-font-smoothing: grayscale` |
| body 样式 | margin: 0，background: #f5f7fa，color: #1d1d1f |
| 滚动条美化 | 6px 宽、圆角、灰色 thumb（#c0c4cc）、hover 变深（#909399） |
| Element Plus 覆盖 | `el-card` 圆角 10px + 边框色；`el-button--primary` 使用绿色主题变量（hover 变深） |

App.vue 内的 `<style>` 标签（非 scoped）负责布局样式（侧边栏、顶部栏、导航项等）。

---

## 开发命令

```bash
# 安装依赖
npm install

# 启动开发服务器（默认 http://localhost:5173）
npm run dev

# 构建生产包（输出到 dist/）
npm run build

# 预览生产构建
npm run preview
```

---

## 环境要求

- **Node.js**: >= 18
- **后端服务**: 运行在 `http://localhost:8080`，接口前缀 `/api`
- **浏览器**: 支持 ES Module 的现代浏览器

---

## Vite 配置说明

```js
// vite.config.js 关键配置
{
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))  // @ -> src/
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // 开发代理目标
        changeOrigin: true
      }
    }
  }
}
```

---

## 注意事项与常见问题

1. **SFC 单文件规范**：每个 `.vue` 文件只能包含一组 `<script setup>` + `<template>` + `<style scoped>` 块，重复的 SFC 块会导致渲染冲突和样式覆盖问题
2. **ECharts 内存管理**：使用 ECharts 的页面需在 `onBeforeUnmount` 中调用 `chartInstance.dispose()` 释放资源，并处理 `window.resize` 事件
3. **日期格式**：所有日期字段统一使用 `YYYY-MM-DD` 格式字符串，月份使用 `YYYY-MM`
4. **分页参数**：`getRecords(params)` 支持 `page`、`size` 分页参数，后端返回 `{ records, total, current, size }` 或类似结构
5. **生产部署**：`npm run build` 输出到 `dist/`，需配置服务器将所有路由回退到 `index.html`（History 模式）
6. **CSS 变量主题化**：全局主题色通过 CSS 自定义属性管理，修改 `:root` 中的变量即可切换整体配色
7. **导航高亮精确匹配**：`router-link` 需同时设置 `active-class` 和 `exact-active-class`，防止前缀路径匹配导致多项高亮
8. **v-html 样式穿透**：使用 `v-html` 渲染的内容（如 ReportView 中的 Markdown），需使用 `:deep()` 穿透 scoped 样式才能生效
9. **BigDecimal 数值处理**：后端返回的金额/百分比字段为 BigDecimal 类型，前端计算前需用 `Number()` 转换后再做加减比较

---

## 常用约定速查

### 记录提交 Body 字段（POST /api/records）

```json
{
  "type": 0,                  // Integer：0=支出 1=收入
  "categoryId": 1,            // Integer
  "amount": 100.00,           // 数字
  "recordDate": "2026-06-19", // YYYY-MM-DD
  "remark": "备注"            // 可选字符串
}
```

### 预算提交 Body（POST /api/budget）

```json
{
  "budgetMonth": "2026-06",   // YYYY-MM
  "amount": 5000.00           // 数字
}
```

### 反馈提交 Body（POST /api/feedback）

```json
{
  "content": "反馈内容"        // 必填字符串
}
```

### 用户更新 Body（PUT /api/user/info）

```json
{
  "username": "新用户名",
  "avatar": "头像URL"
}
```
