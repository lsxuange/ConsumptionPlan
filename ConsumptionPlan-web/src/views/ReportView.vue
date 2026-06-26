<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { generateReport, getReport, listReports } from '@/api/report'
import MarkdownIt from 'markdown-it'

const md = new MarkdownIt()

// ---- 工具函数 ----
function formatMoney(value) {
  if (value === null || value === undefined) return '¥ 0.00'
  return '¥ ' + Number(value).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

function defaultMonth() {
  const d = new Date()
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0')
}

// ---- 状态 ----
const selectedMonth = ref(defaultMonth())
const generating = ref(false)
const loading = ref(false)

const currentReport = reactive({
  reportMonth: '',
  totalIncome: 0,
  totalExpense: 0,
  budgetAmount: 0,
  reportContent: '',
  createTime: ''
})
const hasReport = ref(false)

const reportList = ref([])

// ---- 加载当月报告 ----
async function loadReport() {
  loading.value = true
  try {
    const res = await getReport(selectedMonth.value)
    if (res && res.reportMonth) {
      Object.assign(currentReport, res)
      hasReport.value = true
    } else {
      Object.assign(currentReport, {
        reportMonth: '',
        totalIncome: 0,
        totalExpense: 0,
        budgetAmount: 0,
        reportContent: '',
        createTime: ''
      })
      hasReport.value = false
    }
  } catch (err) {
    console.error('加载报告失败', err)
    hasReport.value = false
  } finally {
    loading.value = false
  }
}

// ---- 加载历史列表 ----
async function loadReportList() {
  try {
    const res = await listReports()
    reportList.value = res || []
  } catch (err) {
    console.error('加载历史报告失败', err)
  }
}

// ---- 生成报告 ----
async function handleGenerate() {
  generating.value = true
  try {
    await generateReport(selectedMonth.value)
    ElMessage.success('AI报告生成成功')
    await loadReport()
    await loadReportList()
  } catch (err) {
    ElMessage.error(err.message || '生成失败')
  } finally {
    generating.value = false
  }
}

// ---- 点击历史报告 ----
async function handleHistoryClick(item) {
  selectedMonth.value = item.reportMonth
  await loadReport()
}

// ---- 初始化 ----
onMounted(() => {
  loadReport()
  loadReportList()
})
</script>

<template>
  <div class="report-page">
    <!-- 顶部操作栏 -->
    <el-card shadow="never" class="toolbar-card">
      <el-form :inline="true" @submit.prevent="handleGenerate">
        <el-form-item label="选择月份">
          <el-date-picker
            v-model="selectedMonth"
            type="month"
            placeholder="选择月份"
            value-format="YYYY-MM"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="generating" @click="handleGenerate">
            {{ generating ? 'AI分析中...' : '生成AI报告' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 报告展示区 -->
    <el-card shadow="never" class="report-card" v-loading="loading">
      <!-- 有报告 -->
      <template v-if="hasReport">
        <!-- 统计卡片 -->
        <el-row :gutter="20" class="stat-row">
          <el-col :span="8">
            <div class="mini-stat bg-green">
              <p class="mini-label">总收入</p>
              <p class="mini-value">{{ formatMoney(currentReport.totalIncome) }}</p>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="mini-stat bg-red">
              <p class="mini-label">总支出</p>
              <p class="mini-value">{{ formatMoney(currentReport.totalExpense) }}</p>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="mini-stat bg-blue">
              <p class="mini-label">月预算</p>
              <p class="mini-value">{{ formatMoney(currentReport.budgetAmount) }}</p>
            </div>
          </el-col>
        </el-row>

        <!-- 报告正文 -->
        <div class="report-body">
          <div class="report-header">
            <h3>{{ currentReport.reportMonth }} 消费分析报告</h3>
            <span class="report-time" v-if="currentReport.createTime">
              生成时间：{{ currentReport.createTime }}
            </span>
          </div>
          <div class="report-content" v-html="md.render(currentReport.reportContent || '')"></div>
        </div>
      </template>

      <!-- 无报告 -->
      <el-empty v-else description="暂无报告，点击上方按钮生成" />
    </el-card>

    <!-- 历史报告列表 -->
    <el-card shadow="never" class="history-card" v-if="reportList.length > 0">
      <template #header>
        <span class="history-title">历史报告</span>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="item in reportList"
          :key="item.reportMonth || item.createTime"
          :timestamp="item.reportMonth"
          placement="top"
          color="#409EFF"
        >
          <el-button
            type="primary"
            link
            @click="handleHistoryClick(item)"
          >
            {{ item.reportMonth }} 报告
          </el-button>
          <span class="history-create-time" v-if="item.createTime">
            （{{ item.createTime }}）
          </span>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<style scoped>
.report-page {
  padding: 20px;
}

.toolbar-card {
  margin-bottom: 20px;
}

.report-card {
  margin-bottom: 20px;
  text-align: left;
  max-width: 860px;
  margin-left: auto;
  margin-right: auto;
}

/* 统计卡片 */
.stat-row {
  margin-bottom: 20px;
}

.mini-stat {
  padding: 20px;
  border-radius: 10px;
  color: #fff;
  text-align: center;
}

.bg-green  { background: linear-gradient(135deg, #67C23A, #529b2e); }
.bg-red    { background: linear-gradient(135deg, #F56C6C, #c45656); }
.bg-blue   { background: linear-gradient(135deg, #409EFF, #337ecc); }

.mini-label {
  font-size: 14px;
  margin: 0 0 8px;
  opacity: 0.9;
}

.mini-value {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

/* 报告正文 */
.report-body {
  padding: 10px 0;
}

.report-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.report-header h3 {
  margin: 0 0 4px;
  font-size: 18px;
  color: #303133;
}

.report-time {
  font-size: 12px;
  color: #909399;
}

.report-content {
  text-align: left;
  padding: 0 8px;
}

.report-content :deep(h3) {
  font-size: 16px;
  font-weight: bold;
  margin: 16px 0 8px;
  color: #303133;
  border-bottom: 1px solid #eee;
  padding-bottom: 4px;
}

.report-content :deep(h4) {
  font-size: 14px;
  font-weight: bold;
  margin: 12px 0 6px;
  color: #606266;
}

.report-content :deep(p) {
  margin: 8px 0;
  line-height: 1.8;
  color: #303133;
}

.report-content :deep(ul) {
  padding-left: 24px;
  margin: 6px 0;
}

.report-content :deep(ol) {
  padding-left: 24px;
  margin: 6px 0;
}

.report-content :deep(li) {
  margin: 4px 0;
  line-height: 1.7;
}

.report-content :deep(hr) {
  margin: 16px 0;
  border: none;
  border-top: 1px solid #eee;
}

.report-content :deep(strong) {
  color: #303133;
  font-weight: 600;
}

/* 历史报告 */
.history-card {
  margin-bottom: 20px;
}

.history-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.history-create-time {
  font-size: 12px;
  color: #909399;
}
</style>

