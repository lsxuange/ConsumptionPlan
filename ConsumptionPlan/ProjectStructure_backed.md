# ConsumptionPlan 项目结构

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.3.5 | 基础框架 |
| Spring Security | 6.3.x | 无状态认证 + JWT |
| Spring Validation | 3.3.5 | 参数校验（jakarta.validation） |
| MyBatis Plus | 3.5.7 | 持久层 ORM |
| MySQL | 8 | 关系型数据库 |
| JJWT | 0.12.6 | JWT 令牌生成与解析 |
| Lombok | 1.18.34 | 代码简化 |
| RestTemplate | 6.1.x | HTTP 客户端（调用 DeepSeek API） |
| Maven | 3.9.x | 构建工具 |
| Java | 17 | 语言版本 |

---

## 项目根目录

```
ConsumptionPlan/
├── pom.xml                         # Maven 依赖配置
├── settings.xml                    # Maven 阿里云镜像 + 本地仓库
├── schema.sql                      # 建表 SQL（8 张表 + 分类初始数据）
├── src/main/
│   ├── resources/
│   │   └── application.yml         # 应用配置
│   └── java/com/example/consumptionplan/
│       ├── ConsumptionPlanApplication.java   # Spring Boot 启动类
│       ├── common/                             # 通用响应/异常
│       ├── config/                             # 配置类（4 个）
│       ├── filter/                             # JWT 过滤器
│       ├── utils/                              # JWT 工具类
│       └── module/                             # 9 个业务模块
```

---

## 基础设施层

### ConsumptionPlanApplication.java

`@SpringBootApplication` 主启动类，位于根包 `com.example.consumptionplan`。

### common/ — 通用响应

| 文件 | 说明 |
|------|------|
| `Result.java` | 统一响应体 `{code, message, data}`，含 `@NoArgsConstructor` + `@AllArgsConstructor` |
| `ResultCode.java` | 错误码枚举：SUCCESS(200) / FAIL(500) / UNAUTHORIZED(401) / FORBIDDEN(403) / NOT_FOUND(404) |
| `GlobalExceptionHandler.java` | `@RestControllerAdvice`，捕获 RuntimeException + Exception，打印完整堆栈并返回 `Result.fail(msg)`（携带异常信息） |

### config/ — 配置类

| 文件 | 说明 |
|------|------|
| `SecurityConfig.java` | `@EnableWebSecurity`，禁用 CSRF + Session(STATELESS)，放行 `/api/auth/**`，注册 JWT Filter |
| `MybatisPlusConfig.java` | `PaginationInnerInterceptor` 分页插件 + `@MapperScan("...module.*.mapper")` |
| `DeepSeekConfig.java` | `@Bean RestTemplate` + DeepSeek API `apiKey` / `apiUrl` 配置 |
| `MyMetaObjectHandler.java` | MyBatis Plus `MetaObjectHandler`，自动填充 `createTime` / `updateTime` |

### filter/ — 过滤器

| 文件 | 说明 |
|------|------|
| `JwtAuthenticationFilter.java` | `OncePerRequestFilter`，从 `Authorization: Bearer <token>` 提取 → 验证 → `SecurityContext` 存 `userId(Long)` |

### utils/ — 工具类

| 文件 | 说明 |
|------|------|
| `JwtUtils.java` | `generateToken(userId)` / `parseToken(token) → userId` / `isTokenValid(token)`，HS256 签名 |

---

## 业务模块（module/）

### user/ — 用户认证（5 端点）

| 层 | 文件 | 字段/方法 |
|----|------|-----------|
| entity | `User.java` | id, username, email, password(@JsonIgnore), avatar, status, createTime(fill), updateTime(fill) |
| mapper | `UserMapper.java` | `BaseMapper<User>` |
| dto | `RegisterDTO.java` | @NotBlank username / email / password |
| dto | `LoginDTO.java` | @NotBlank username / password |
| dto | `UpdateUserDTO.java` | @NotBlank username / avatar |
| dto | `UserDTO.java` | 骨架占位 |
| vo | `LoginVO.java` | token, username, avatar（`@AllArgsConstructor`） |
| vo | `UserVO.java` | id, username, email, avatar, createTime |
| service | `UserService.java` | register / login / getUserInfo / updateUserInfo |
| impl | `UserServiceImpl.java` | BCrypt 加密 + 唯一性校验 + JWT 生成 |
| controller | `UserController.java` | `@RequestMapping("/api")` |

### record/ — 消费记录（4 端点）

| 层 | 文件 | 字段/方法 |
|----|------|-----------|
| entity | `ExpenseRecord.java` | userId, categoryId, type(0支出/1收入), amount, remark, recordDate(LocalDate), createTime(fill), updateTime(fill) |
| mapper | `ExpenseRecordMapper.java` | `BaseMapper<ExpenseRecord>` |
| dto | `RecordDTO.java` | @NotNull categoryId / type / amount(@DecimalMin) / recordDate，remark 可选 |
| dto | `RecordQueryDTO.java` | type / categoryId / startDate / endDate / page(默认1) / size(默认10) |
| vo | `RecordVO.java` | 全字段 + categoryName（关联填充） |
| service | `RecordService.java` | addRecord / updateRecord / deleteRecord / pageRecords |
| impl | `RecordServiceImpl.java` | CRUD + userId 权限校验 + LambdaQueryWrapper 动态条件 + CategoryMapper 填 categoryName |
| controller | `RecordController.java` | `@RequestMapping("/api")` |

### category/ — 分类（1 端点）

| 层 | 文件 | 字段/方法 |
|----|------|-----------|
| entity | `Category.java` | id, name, type(0支出/1收入), icon, sortNum, createTime |
| mapper | `CategoryMapper.java` | `BaseMapper<Category>` |
| dto | `CategoryDTO.java` | 骨架占位 |
| vo | `CategoryVO.java` | 骨架占位 |
| service | `CategoryService.java` | listByType(Integer type) |
| impl | `CategoryServiceImpl.java` | type=null 查全部，否则按 type 过滤，sortNum ASC |
| controller | `CategoryController.java` | `@RequestMapping("/api")` |

### budget/ — 预算管理（3 端点）

| 层 | 文件 | 字段/方法 |
|----|------|-----------|
| entity | `Budget.java` | userId, budgetMonth(String "2026-06"), amount, createTime(fill), updateTime(fill) |
| mapper | `BudgetMapper.java` | `BaseMapper<Budget>` |
| dto | `BudgetDTO.java` | budgetMonth(@NotBlank @Pattern YYYY-MM)，amount(@NotNull @DecimalMin) |
| vo | `BudgetVO.java` | budgetMonth, amount, totalExpense, remaining, usageRate |
| vo | `BudgetHistoryVO.java` | budgetMonth, budgetAmount(可为null), totalExpense, usageRate(可为null)（`@AllArgsConstructor` + `@NoArgsConstructor`） |
| service | `BudgetService.java` | saveOrUpdateBudget / getBudgetVO / getHistory |
| impl | `BudgetServiceImpl.java` | upsert + YearMonth 月度支出统计 + **三档预警通知**（60%/80%/100%）+ 最近6月历史聚合 |
| controller | `BudgetController.java` | `@RequestMapping("/api")` |

### report/ — AI 分析报告（3 端点）

| 层 | 文件 | 字段/方法 |
|----|------|-----------|
| entity | `AiReport.java` | userId, reportMonth, totalIncome, totalExpense, budgetAmount, reportContent, createTime(fill) |
| mapper | `AiReportMapper.java` | `BaseMapper<AiReport>` |
| dto | `ReportDTO.java` | 骨架占位 |
| vo | `ReportVO.java` | reportMonth, totalIncome, totalExpense, budgetAmount, reportContent, createTime |
| service | `ReportService.java` | generateReport / getReport / listReports |
| impl | `ReportServiceImpl.java` | **六步流程**：数据收集→构造 prompt→RestTemplate 调 DeepSeek→解析 choices→存库(update/insert)→通知 |
| controller | `ReportController.java` | `@RequestMapping("/api")` |

### checkin/ — 记账打卡（3 端点）

| 层 | 文件 | 字段/方法 |
|----|------|-----------|
| entity | `CheckInRecord.java` | userId, checkDate(LocalDate), createTime |
| mapper | `CheckInRecordMapper.java` | `BaseMapper<CheckInRecord>` |
| vo | `CheckInVO.java` | todayChecked(Boolean), continuousDays, monthCheckedDays |
| service | `CheckInService.java` | checkIn / getCheckInVO / listCheckedDates |
| impl | `CheckInServiceImpl.java` | 判重→insert→通知 + 连续天数计算（从今天/昨天向前遍历）+ 按月查询打卡日期列表（LambdaQueryWrapper BETWEEN） |
| controller | `CheckInController.java` | `@RequestMapping("/api")` |

### notification/ — 通知中心（3 端点）

| 层 | 文件 | 字段/方法 |
|----|------|-----------|
| entity | `Notification.java` | userId, title, content, isRead(0/1), createTime |
| mapper | `NotificationMapper.java` | `BaseMapper<Notification>` |
| dto | `NotificationDTO.java` | 骨架占位 |
| vo | `NotificationVO.java` | 骨架占位 |
| service | `NotificationService.java` | send / listNotifications / markAllRead / getUnreadCount |
| impl | `NotificationServiceImpl.java` | insert + selectList(DESC) + LambdaUpdateWrapper + selectCount |
| controller | `NotificationController.java` | `@RequestMapping("/api")` |

### feedback/ — 用户反馈（2 端点）

| 层 | 文件 | 字段/方法 |
|----|------|-----------|
| entity | `Feedback.java` | userId, content, status(0未处理/1已处理), createTime(fill) |
| mapper | `FeedbackMapper.java` | `BaseMapper<Feedback>` |
| dto | `FeedbackDTO.java` | @NotBlank @Size(max=1000) content |
| vo | `FeedbackVO.java` | 骨架占位 |
| service | `FeedbackService.java` | submitFeedback / listMyFeedback |
| impl | `FeedbackServiceImpl.java` | insert(status=0) + selectList(userId DESC) |
| controller | `FeedbackController.java` | `@RequestMapping("/api")` |

### dashboard/ — 首页统计（1 端点）

| 层 | 文件 | 字段/方法 |
|----|------|-----------|
| vo | `DashboardVO.java` | monthIncome, monthExpense, monthBalance, budgetAmount, budgetUsageRate, expenseCategoryList, trendList, incomeSourceList |
| vo | `CategoryStatVO.java` | categoryName, amount, percentage（HALF_UP 2位） |
| vo | `TrendVO.java` | month("2026-06"), income, expense |
| service | `DashboardService.java` | getDashboard(Long userId) |
| impl | `DashboardServiceImpl.java` | 当月收支 + 预算使用率 + 分类占比(groupingBy, DESC) + 最近6月趋势 |
| controller | `DashboardController.java` | `@RequestMapping("/api")` |

---

## API 端点汇总（25 个）

### 认证（permitAll，无需 Token）

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/auth/register` | 注册（username + email + password） |
| `POST` | `/api/auth/login` | 登录 → 返回 `{token, username, avatar}` |
| `POST` | `/api/auth/logout` | 登出（JWT 无状态，直接返回成功） |

### 用户信息

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/user/info` | 获取当前用户信息 |
| `PUT` | `/api/user/info` | 更新用户名 / 头像 |

### 消费记录

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/records` | 新增记录 |
| `PUT` | `/api/records/{id}` | 修改记录 |
| `DELETE` | `/api/records/{id}` | 删除记录（userId 校验） |
| `GET` | `/api/records?type=&categoryId=&startDate=&endDate=&page=&size=` | 分页查询 + categoryName 填充 |

### 分类

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/categories[?type=]` | 分类列表（type 可选，sortNum ASC） |

### 预算

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/budget` | 设置/修改月预算（三档预警通知：60%/80%/100%） |
| `GET` | `/api/budget[?month=]` | 查预算执行情况（默认当月） |
| `GET` | `/api/budget/history` | 最近6月预算历史（每月预算金额+支出+使用率） |

### AI 报告

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/report/generate[?month=]` | 生成 AI 消费分析报告 |
| `GET` | `/api/report[?month=]` | 查单月报告 |
| `GET` | `/api/reports` | 历史报告列表 |

### 打卡

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/checkin` | 今日打卡（每日限一次） |
| `GET` | `/api/checkin/status` | 查打卡状态 + 连续天数 + 本月打卡天数 |
| `GET` | `/api/checkin/month[?month=]` | 查当月打卡日期列表（默认当月，返回 `["YYYY-MM-DD", ...]`） |

### 通知

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/notifications` | 通知列表（按时间 DESC） |
| `PUT` | `/api/notifications/read-all` | 全部标为已读 |
| `GET` | `/api/notifications/unread-count` | 未读数量 |

### 反馈

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/feedback` | 提交反馈（content ≤ 1000字） |
| `GET` | `/api/feedback/my` | 我的反馈列表 |

### 首页

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/dashboard` | 首页统计（收支/预算/分类占比/6月趋势） |

---

## 数据库（8 张表）

| 表名 | 说明 | 索引 |
|------|------|------|
| `user` | 用户 | UNIQUE(username), UNIQUE(email) |
| `category` | 分类（14 条初始数据：8 支出 + 6 收入） | — |
| `expense_record` | 消费记录 | idx_user_date, idx_user_category, idx_user_type |
| `budget` | 月预算 | UNIQUE(user_id, budget_month) |
| `ai_report` | AI 分析报告 | — |
| `check_in_record` | 打卡记录 | UNIQUE(user_id, check_date) |
| `notification` | 通知 | — |
| `feedback` | 用户反馈 | — |

> 所有表均无外键约束，无 `deleted` 字段，字符集 `utf8mb4`，引擎 `InnoDB`。

---

## 配置项（application.yml）

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/consumptionplan?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver

jwt:
  secret: ${JWT_SECRET:your-secret-key-at-least-32-chars}   # HS256 需 ≥ 32 字节
  expiration: 86400000                                         # 24 小时（ms）

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true                        # 下划线 → 驼峰
  global-config:
    db-config:
      logic-delete-field: deleted                              # 逻辑删除预留（未落表）
      logic-delete-value: 1
      logic-not-delete-value: 0

deepseek:
  api-key: ${DEEPSEEK_API_KEY:your-key-here}                  # DeepSeek API 密钥
  api-url: https://api.deepseek.com/chat/completions
```

---

## Maven 构建配置（pom.xml + settings.xml）

### pom.xml

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `maven-compiler-plugin` | 3.11.0 | 显式配置 `<source>17</source>` / `<target>17</target>` / `<encoding>UTF-8</encoding>`，确保 IDEA 正确识别 Java 版本 |
| `spring-boot-maven-plugin` | 继承自 parent | 排除 Lombok（不打包进 JAR） |
| `maven.compiler.source` | 17 | properties 级别显式声明，IDEA 同步用 |
| `maven.compiler.target` | 17 | properties 级别显式声明，IDEA 同步用 |

### settings.xml

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `localRepository` | `E:\project\CP\ConsumptionPlan\.m2` | 项目级本地仓库 |
| `mirror` | `https://maven.aliyun.com/repository/public` | 阿里云镜像（解决中央仓库超时） |

> IDEA 需在 Maven 设置中将 User settings file 指向此文件，并勾选 Override。Local repository 同样需勾选 Override 并指向 `E:\project\CP\ConsumptionPlan\.m2`。

---

## 跨模块依赖关系

```
user         → JwtUtils, PasswordEncoder, UserMapper
record       → ExpenseRecordMapper, CategoryMapper（填 categoryName）
budget       → BudgetMapper, ExpenseRecordMapper（月度支出统计）, NotificationService（三档预警：60%/80%/100%）
report       → AiReportMapper, ExpenseRecordMapper, BudgetMapper, CategoryMapper, RestTemplate, NotificationService
checkin      → CheckInRecordMapper, NotificationService（打卡成功通知）
notification → NotificationMapper（被 budget / checkin / report 调用）
dashboard    → ExpenseRecordMapper, BudgetMapper, CategoryMapper
feedback     → FeedbackMapper
```

---

## 编码规范

| 规范项 | 要求 |
|--------|------|
| 依赖注入 | 构造注入（`final` + `@RequiredArgsConstructor`），禁止 `@Autowired` |
| 时间字段 | `LocalDateTime`（填充字段）/ `LocalDate`（记录日期/打卡日期） |
| 密码存储 | `BCryptPasswordEncoder` 加密 |
| JWT 传递 | `Authorization: Bearer <token>` 请求头 |
| 金额类型 | `BigDecimal`，计算用 `RoundingMode.HALF_UP` |
| 查询构建 | `LambdaQueryWrapper` / `LambdaUpdateWrapper` |
| 分页 | `IPage<RecordVO>` + `page.convert(entity → vo)` |
| 异常处理 | 业务层抛 `RuntimeException`，`GlobalExceptionHandler` 统一捕获 |
| 服务注入 | 必须使用接口类型（`NotificationService` 而非 `NotificationServiceImpl`） |

---

## 文件统计

| 类别 | 文件数 |
|------|--------|
| 启动类 | 1 |
| common | 3 |
| config | 4 |
| filter | 1 |
| utils | 1 |
| module/user | 11 |
| module/record | 8 |
| module/category | 7 |
| module/budget | 8 |
| module/report | 7 |
| module/checkin | 6 |
| module/notification | 7 |
| module/feedback | 7 |
| module/dashboard | 6 |
| schema.sql | 1 |
| **合计** | **78** |
