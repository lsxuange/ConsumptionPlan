<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/userStore'
import { getUserInfo, updateUserInfo, changePassword } from '@/api/user'
import { getCheckInStatus, checkIn, getCheckedDates } from '@/api/checkin'
import { getBudget } from '@/api/budget'
import { getDashboard } from '@/api/dashboard'
import { getNotifications, markAllRead, getUnreadCount } from '@/api/notification'
import { submitFeedback, getMyFeedback } from '@/api/feedback'
import { logout } from '@/api/auth'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)

// ==================== ① 用户信息 ====================
const userInfo = reactive({
  id: null,
  username: '',
  email: '',
  avatar: '',
  createTime: ''
})

const editDialog = ref(false)
const editForm = reactive({ username: '', avatar: '' })
const editSubmitting = ref(false)

const avatarText = computed(() => {
  return userInfo.username ? userInfo.username.charAt(0).toUpperCase() : 'U'
})

async function loadUserInfo() {
  try {
    const res = await getUserInfo()
    if (res) Object.assign(userInfo, res)
  } catch (err) {
    console.error('加载用户信息失败', err)
  }
}

function openEdit() {
  editForm.username = userInfo.username
  editForm.avatar = userInfo.avatar || ''
  editDialog.value = true
}

async function handleEditSubmit() {
  editSubmitting.value = true
  try {
    await updateUserInfo({ username: editForm.username, avatar: editForm.avatar })
    ElMessage.success('信息更新成功')
    editDialog.value = false
    await loadUserInfo()
  } catch (err) {
    ElMessage.error(err.message || '更新失败')
  } finally {
    editSubmitting.value = false
  }
}

// ==================== ② 记账打卡 ====================
const checkInData = reactive({
  todayChecked: false,
  continuousDays: 0,
  monthCheckedDays: 0
})
const checkInLoading = ref(false)

const budgetData = ref(null)
const dashboardData = ref(null)

const checkedDates = ref([])
const calendarMonth = ref(dayjs().format('YYYY-MM'))
const today = dayjs().format('YYYY-MM-DD')

const calendarDays = computed(() => {
  const firstDay = dayjs(calendarMonth.value).startOf('month')
  const totalDays = dayjs(calendarMonth.value).daysInMonth()
  const startDayOfWeek = firstDay.day()
  const offset = startDayOfWeek === 0 ? 6 : startDayOfWeek - 1

  const days = []
  for (let i = 0; i < offset; i++) {
    days.push(null)
  }
  for (let d = 1; d <= totalDays; d++) {
    const dateStr = dayjs(calendarMonth.value).date(d).format('YYYY-MM-DD')
    days.push({ date: dateStr, day: d })
  }
  return days
})

async function loadCheckInStatus() {
  try {
    const res = await getCheckInStatus()
    if (res) Object.assign(checkInData, res)
  } catch (err) {
    console.error('加载签到状态失败', err)
  }
}

async function handleCheckIn() {
  checkInLoading.value = true
  try {
    await checkIn()
    ElMessage.success('打卡成功！')
    await loadCheckInStatus()
    await loadCheckedDates()
  } catch (err) {
    ElMessage.error(err.message || '打卡失败')
  } finally {
    checkInLoading.value = false
  }
}

async function loadCheckedDates() {
  try {
    const res = await getCheckedDates(calendarMonth.value)
    checkedDates.value = res || []
  } catch (err) {
    console.error('加载打卡日期失败', err)
  }
}

// ==================== ③ 通知中心 ====================
const notifications = ref([])
const unreadCount = ref(0)
const markAllLoading = ref(false)

async function loadNotifications() {
  try {
    notifications.value = await getNotifications()
  } catch (err) {
    console.error('加载通知失败', err)
  }
}

async function loadUnreadCount() {
  try {
    unreadCount.value = await getUnreadCount()
  } catch (err) {
    console.error('加载未读数失败', err)
  }
}

async function handleMarkAllRead() {
  markAllLoading.value = true
  try {
    await markAllRead()
    ElMessage.success('已全部标为已读')
    await loadNotifications()
    await loadUnreadCount()
  } catch (err) {
    ElMessage.error(err.message || '操作失败')
  } finally {
    markAllLoading.value = false
  }
}

// ==================== ④ 意见反馈 ====================
const feedbackContent = ref('')
const feedbackList = ref([])
const feedbackSubmitting = ref(false)

async function loadFeedbackList() {
  try {
    feedbackList.value = await getMyFeedback()
  } catch (err) {
    console.error('加载反馈列表失败', err)
  }
}

async function handleSubmitFeedback() {
  if (!feedbackContent.value.trim()) {
    ElMessage.warning('请输入反馈内容')
    return
  }
  feedbackSubmitting.value = true
  try {
    await submitFeedback({ content: feedbackContent.value })
    ElMessage.success('反馈提交成功')
    feedbackContent.value = ''
    await loadFeedbackList()
  } catch (err) {
    ElMessage.error(err.message || '提交失败')
  } finally {
    feedbackSubmitting.value = false
  }
}

// ==================== 预算 & 仪表盘数据 ====================
async function loadBudgetData() {
  try {
    budgetData.value = await getBudget(dayjs().format('YYYY-MM'))
  } catch {
    budgetData.value = null
  }
}

async function loadDashboardData() {
  try {
    dashboardData.value = await getDashboard()
  } catch {
    dashboardData.value = null
  }
}

// ==================== ⑤ 退出登录 ====================
async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await logout()
  } catch (err) {
    // 即使后端退出失败，本地也清除
  }
  userStore.clearUser()
  router.push('/login')
}

// ==================== ⑥ 修改密码 ====================
const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

function openPasswordDialog() {
  passwordDialogVisible.value = true
}

function resetPasswordForm() {
  passwordFormRef.value?.resetFields()
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

async function submitPasswordChange() {
  try {
    await passwordFormRef.value.validate()
  } catch {
    return
  }
  passwordLoading.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
    resetPasswordForm()
  } catch (err) {
    // request.js 拦截器已 ElMessage.error
  } finally {
    passwordLoading.value = false
  }
}

// ==================== 消费健康度 ====================
const healthScore = computed(() => {
  let score = 0
  const checks = []
  const warnings = []

  // 维度1：预算执行（35分）
  const usageRate = budgetData.value?.usageRate ?? null
  if (usageRate === null) {
    score += 20
    checks.push('暂无预算数据')
  } else if (usageRate <= 80) {
    score += 35
    checks.push('预算执行优秀')
  } else if (usageRate <= 100) {
    score += 20
    warnings.push('预算使用率偏高')
  } else {
    score += 0
    warnings.push('本月预算已超支')
  }

  // 维度2：收支盈余（30分）
  const income = Number(dashboardData.value?.monthIncome ?? 0)
  const expense = Number(dashboardData.value?.monthExpense ?? 0)
  const balance = income - expense
  if (balance > 0) {
    score += 30
    checks.push('收支保持盈余')
  } else if (balance === 0) {
    score += 15
    warnings.push('本月收支刚好持平')
  } else {
    score += 0
    warnings.push('本月支出超过收入')
  }

  // 维度3：连续记账（20分）
  const days = checkInData?.continuousDays ?? 0
  if (days >= 7) {
    score += 20
    checks.push(`连续记账 ${days} 天`)
  } else if (days >= 3) {
    score += 12
    checks.push(`连续记账 ${days} 天`)
  } else if (days >= 1) {
    score += 6
    warnings.push(`连续记账仅 ${days} 天`)
  } else {
    score += 0
    warnings.push('今日尚未打卡记账')
  }

  // 维度4：分类均衡（15分）
  const categoryList = dashboardData.value?.expenseCategoryList ?? []
  const topCategory = categoryList[0]
  if (!topCategory) {
    score += 15
  } else if (Number(topCategory.percentage ?? 0) <= 50) {
    score += 15
    checks.push('消费分类均衡')
  } else if (Number(topCategory.percentage ?? 0) <= 70) {
    score += 8
    warnings.push(`${topCategory.categoryName}占比偏高`)
  } else {
    score += 0
    warnings.push(`${topCategory.categoryName}支出过于集中`)
  }

  // 等级
  const level = score >= 90 ? 'S' : score >= 75 ? 'A' : score >= 60 ? 'B' : 'C'
  const levelColor = score >= 90 ? '#00b96b' : score >= 75 ? '#409eff' : score >= 60 ? '#e6a23c' : '#f56c6c'

  return { score, level, levelColor, checks, warnings }
})

// ==================== 初始化 ====================
onMounted(async () => {
  loading.value = true
  await Promise.all([
    loadUserInfo(),
    loadCheckInStatus(),
    loadCheckedDates(),
    loadNotifications(),
    loadUnreadCount(),
    loadFeedbackList(),
    loadBudgetData(),
    loadDashboardData()
  ])
  loading.value = false
})
</script>

<template>
  <div class="profile-page" v-loading="loading">
    <!-- 编辑弹窗（全局级，不放入列中） -->
    <el-dialog v-model="editDialog" title="编辑个人信息" width="420px" destroy-on-close>
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="头像URL">
          <el-input v-model="editForm.avatar" placeholder="请输入头像图片地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="handleEditSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px" @close="resetPasswordForm">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="90px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="submitPasswordChange">确认修改</el-button>
      </template>
    </el-dialog>

    <div class="profile-layout">
      <!-- 左列 -->
      <div class="profile-left">
        <!-- ① 用户信息 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="section-title">个人信息</span>
          </template>
          <div class="user-info-row">
            <el-avatar :size="64" :src="userInfo.avatar || undefined">
              {{ avatarText }}
            </el-avatar>
            <div class="user-detail">
              <p class="user-name">{{ userInfo.username || '-' }}</p>
              <p class="user-email">{{ userInfo.email || '-' }}</p>
              <p class="user-time" v-if="userInfo.createTime">
                注册于 {{ userInfo.createTime }}
              </p>
            </div>
            <el-button type="primary" plain @click="openEdit">编辑信息</el-button>
          </div>
        </el-card>

        <!-- ② 记账打卡 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="section-title">记账打卡</span>
          </template>
          <!-- 统计数字 -->
          <div class="checkin-stats">
            <span>🔥 连续打卡 {{ checkInData.continuousDays }} 天</span>
            <span>📅 本月打卡 {{ checkInData.monthCheckedDays }} 天</span>
          </div>
          <!-- 日历网格 -->
          <div class="checkin-calendar">
            <div class="calendar-header">
              <span v-for="d in ['一','二','三','四','五','六','日']" :key="d">{{ d }}</span>
            </div>
            <div class="calendar-grid">
              <div
                v-for="(item, index) in calendarDays"
                :key="index"
                class="calendar-cell"
                :class="{
                  'is-checked': item && checkedDates.includes(item.date),
                  'is-today': item && item.date === today,
                  'is-empty': !item
                }"
              >
                {{ item ? item.day : '' }}
              </div>
            </div>
          </div>
          <!-- 今日打卡按钮 -->
          <div class="checkin-btn-row">
            <el-button
              type="warning"
              :disabled="checkInData.todayChecked"
              :loading="checkInLoading"
              @click="handleCheckIn"
            >
              {{ checkInData.todayChecked ? '今日已打卡 ✓' : '今日打卡' }}
            </el-button>
          </div>
        </el-card>

        <!-- 消费健康度 -->
        <el-card class="health-card">
          <template #header>
            <span style="font-weight:600">消费健康度</span>
          </template>

          <!-- 评分主展示 -->
          <div class="health-score-row">
            <div class="health-score-circle" :style="{ borderColor: healthScore.levelColor }">
              <span class="health-score-num" :style="{ color: healthScore.levelColor }">
                {{ healthScore.score }}
              </span>
              <span class="health-score-label">分</span>
            </div>
            <div class="health-score-meta">
              <div class="health-level" :style="{ color: healthScore.levelColor }">
                {{ healthScore.level }} 级
              </div>
              <div class="health-level-desc">综合消费健康评分</div>
            </div>
          </div>

          <!-- 指标列表 -->
          <div class="health-checks">
            <div v-for="item in healthScore.checks" :key="item" class="health-item health-item--ok">
              <span class="health-item__icon">✓</span>
              <span>{{ item }}</span>
            </div>
            <div v-for="item in healthScore.warnings" :key="item" class="health-item health-item--warn">
              <span class="health-item__icon">⚠</span>
              <span>{{ item }}</span>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右列 -->
      <div class="profile-right">
        <!-- ③ 通知中心 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="section-header-row">
              <span class="section-title">通知中心</span>
              <div>
                <el-badge :value="unreadCount" :hidden="unreadCount === 0" style="margin-right:12px" />
                <el-button
                  size="small"
                  :loading="markAllLoading"
                  :disabled="unreadCount === 0"
                  @click="handleMarkAllRead"
                >
                  全部已读
                </el-button>
              </div>
            </div>
          </template>
          <div v-if="notifications.length === 0" class="empty-tip">暂无通知</div>
          <div class="notify-list">
            <div
              v-for="item in notifications"
              :key="item.id"
              class="notify-item"
              :class="{ unread: item.isRead === 0 }"
            >
              <p class="notify-title">{{ item.title }}</p>
              <p class="notify-content">{{ item.content }}</p>
              <span class="notify-time">{{ item.createTime }}</span>
            </div>
          </div>
        </el-card>

        <!-- ④ 意见反馈 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="section-title">意见反馈</span>
          </template>
          <el-input
            v-model="feedbackContent"
            type="textarea"
            :rows="4"
            maxlength="1000"
            show-word-limit
            placeholder="请描述您的意见或问题..."
          />
          <div class="feedback-btn-row">
            <el-button
              type="primary"
              :loading="feedbackSubmitting"
              @click="handleSubmitFeedback"
            >
              提交反馈
            </el-button>
          </div>
          <div v-if="feedbackList.length > 0" class="feedback-history">
            <p class="feedback-subtitle">我的反馈记录</p>
            <div v-for="item in feedbackList" :key="item.id" class="feedback-item">
              <div class="feedback-item-row">
                <p class="feedback-content">{{ item.content }}</p>
                <el-tag :type="item.status === 1 ? 'success' : 'warning'" size="small">
                  {{ item.status === 1 ? '已处理' : '待处理' }}
                </el-tag>
              </div>
              <span class="feedback-time">{{ item.createTime }}</span>
            </div>
          </div>
        </el-card>

        <!-- 修改密码 & 退出登录 -->
        <el-card shadow="never" class="section-card">
          <div style="display:flex;gap:12px;">
            <el-button type="warning" plain @click="openPasswordDialog">修改密码</el-button>
            <el-button type="danger" @click="handleLogout">退出登录</el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  padding: 20px;
}

.profile-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}
.profile-left {
  flex: 0 0 52%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.profile-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-card {
  margin-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.section-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 用户信息 */
.user-info-row {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-detail {
  flex: 1;
}

.user-name {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 4px;
  color: #303133;
}

.user-email {
  font-size: 14px;
  color: #606266;
  margin: 0 0 2px;
}

.user-time {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

/* 打卡 */
.checkin-stats {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
  font-size: 15px;
}

.checkin-calendar {
  margin: 12px 0;
}

.calendar-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  color: #909399;
  font-size: 12px;
  margin-bottom: 6px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.calendar-cell {
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 13px;
  background: #f5f5f5;
  color: #606266;
}

.calendar-cell.is-empty {
  background: transparent;
}

.calendar-cell.is-checked {
  background: #67c23a;
  color: #fff;
  font-weight: bold;
}

.calendar-cell.is-today {
  outline: 2px solid #409eff;
  outline-offset: -2px;
}

.checkin-btn-row {
  margin-top: 12px;
}

/* 通知 */
.empty-tip {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}

.notify-item {
  padding: 14px 16px;
  border-radius: 6px;
  margin-bottom: 8px;
  border: 1px solid #ebeef5;
  transition: background 0.2s;
}

.notify-item.unread {
  background: #ecf5ff;
  border-color: #d9ecff;
}

.notify-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 6px;
}

.notify-content {
  font-size: 13px;
  color: #606266;
  margin: 0 0 6px;
  line-height: 1.5;
}

.notify-time {
  font-size: 12px;
  color: #909399;
}

/* 反馈 */
.feedback-btn-row {
  margin-top: 12px;
}

.feedback-history {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.feedback-subtitle {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px;
}

.feedback-item {
  padding: 12px 0;
  border-bottom: 1px solid #f2f2f2;
}

.feedback-item:last-child {
  border-bottom: none;
}

.feedback-item-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.feedback-content {
  font-size: 13px;
  color: #606266;
  margin: 0;
  flex: 1;
  line-height: 1.5;
}

.feedback-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  display: inline-block;
}

/* 消费健康度 */
.health-card {}

.health-score-row {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}
.health-score-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 4px solid;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.health-score-num {
  font-size: 26px;
  font-weight: 700;
  line-height: 1;
}
.health-score-label {
  font-size: 12px;
  color: #909399;
}
.health-level {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
}
.health-level-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.health-checks {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.health-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}
.health-item__icon {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  flex-shrink: 0;
}
.health-item--ok {
  color: #333;
}
.health-item--ok .health-item__icon {
  background: #e8f9f2;
  color: #00b96b;
  font-weight: 700;
}
.health-item--warn {
  color: #666;
}
.health-item--warn .health-item__icon {
  background: #fdf6ec;
  color: #e6a23c;
}

.notify-list {
  max-height: 260px;
  overflow-y: auto;
}
</style>

