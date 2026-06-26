<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getBudget, saveBudget, getBudgetHistory } from '@/api/budget'
import { ElMessage } from 'element-plus'

// ---- 工具函数 ----
function fmt(val) {
  return val != null ? '¥' + Number(val).toFixed(2) : '—'
}

// ---- 状态 ----
const selectedMonth = ref('')
const budgetVO = ref(null)
const historyList = ref([])
const dialogVisible = ref(false)
const form = ref({ budgetMonth: '', amount: null })

const gaugeChartRef = ref(null)
const trendChartRef = ref(null)
let gaugeChart = null
let trendChart = null

// ---- 预警 ----
const warningInfo = computed(() => {
  if (!budgetVO.value?.usageRate) return null
  const rate = budgetVO.value.usageRate
  if (rate >= 100) return {
    type: 'error',
    icon: '🚨',
    title: '预算已超支',
    text: `本月预算 ¥${Number(budgetVO.value.amount).toFixed(2)}，当前支出已达 ¥${Number(budgetVO.value.totalExpense).toFixed(2)}，超出预算，请立即控制消费！`
  }
  if (rate >= 80) return {
    type: 'warning',
    icon: '⚠️',
    title: '预算接近超支',
    text: `本月预算使用率已达 ${rate}%，剩余 ¥${Number(budgetVO.value.remaining).toFixed(2)}，请注意消费节奏。`
  }
  if (rate >= 60) return {
    type: 'info',
    icon: '💡',
    title: '预算使用提醒',
    text: `本月预算已使用 ${rate}%，还剩 ¥${Number(budgetVO.value.remaining).toFixed(2)}，继续保持！`
  }
  return null
})

// ---- 预算 ----
async function loadBudget() {
  try {
    budgetVO.value = await getBudget(selectedMonth.value)
  } catch {
    budgetVO.value = null
  }
  updateGaugeChart()
}

async function loadHistory() {
  historyList.value = await getBudgetHistory()
  updateTrendChart()
}

async function handleQuery() {
  await loadBudget()
}

// ---- 弹窗 ----
function openDialog() {
  form.value = {
    budgetMonth: budgetVO.value?.budgetMonth ?? selectedMonth.value,
    amount: budgetVO.value?.amount ?? null
  }
  dialogVisible.value = true
}

async function handleSaveBudget() {
  if (!form.value.budgetMonth || !form.value.amount) {
    ElMessage.warning('请填写完整信息')
    return
  }
  await saveBudget(form.value)
  ElMessage.success('预算设置成功')
  dialogVisible.value = false
  await loadBudget()
  await loadHistory()
}

// ---- 环形图 ----
function initGaugeChart() {
  if (!gaugeChartRef.value) return
  gaugeChart = echarts.init(gaugeChartRef.value)
  updateGaugeChart()
}

function updateGaugeChart() {
  if (!gaugeChart) return
  const used = budgetVO.value?.usageRate ?? 0
  const rest = Math.max(0, 100 - used)
  gaugeChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {d}%' },
    legend: { show: false },
    series: [{
      type: 'pie',
      radius: ['55%', '75%'],
      label: {
        show: true,
        position: 'center',
        formatter: () => `${used}%`,
        fontSize: 22,
        fontWeight: 'bold',
        color: used >= 100 ? '#f56c6c' : used >= 80 ? '#e6a23c' : '#00b96b'
      },
      data: [
        { value: used, name: '已使用', itemStyle: { color: used >= 100 ? '#f56c6c' : used >= 80 ? '#e6a23c' : '#00b96b' } },
        { value: rest, name: '剩余',   itemStyle: { color: '#ebedf0' } }
      ]
    }]
  })
}

// ---- 趋势折线图 ----
function initTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  updateTrendChart()
}

function updateTrendChart() {
  if (!trendChart) return
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['预算', '支出'] },
    grid: { left: 50, right: 20, top: 40, bottom: 30 },
    xAxis: {
      type: 'category',
      data: historyList.value.map(h => h.budgetMonth)
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '预算',
        type: 'line',
        lineStyle: { type: 'dashed' },
        itemStyle: { color: '#00b96b' },
        data: historyList.value.map(h => h.budgetAmount ?? 0)
      },
      {
        name: '支出',
        type: 'line',
        itemStyle: { color: '#f56c6c' },
        data: historyList.value.map(h => h.totalExpense)
      }
    ]
  })
}

// ---- 生命周期 ----
const handleResize = () => {
  gaugeChart?.resize()
  trendChart?.resize()
}

onMounted(async () => {
  selectedMonth.value = dayjs().format('YYYY-MM')
  await loadBudget()
  await loadHistory()
  setTimeout(() => {
    initGaugeChart()
    initTrendChart()
  }, 100)
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  gaugeChart?.dispose()
  trendChart?.dispose()
})
</script>

<template>
  <div class="budget-page">
    <!-- ① 操作栏 -->
    <el-card style="margin-bottom:16px">
      <div style="display:flex;align-items:center;gap:12px">
        <span>选择月份</span>
        <el-date-picker
          v-model="selectedMonth"
          type="month"
          format="YYYY-MM"
          value-format="YYYY-MM"
          style="width:160px"
        />
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="openDialog">修改预算</el-button>
      </div>
    </el-card>

    <!-- ② 四卡片行 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6">
        <el-card>
          <div style="text-align:center;padding:8px 0">
            <p style="font-size:13px;color:#909399;margin:0 0 8px">预算金额</p>
            <p style="font-size:24px;font-weight:700;color:#00b96b;margin:0">{{ fmt(budgetVO?.amount) }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align:center;padding:8px 0">
            <p style="font-size:13px;color:#909399;margin:0 0 8px">本月支出</p>
            <p style="font-size:24px;font-weight:700;color:#00b96b;margin:0">{{ fmt(budgetVO?.totalExpense) }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align:center;padding:8px 0">
            <p style="font-size:13px;color:#909399;margin:0 0 8px">剩余预算</p>
            <p style="font-size:24px;font-weight:700;color:#00b96b;margin:0">{{ fmt(budgetVO?.remaining) }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align:center;padding:8px 0">
            <p style="font-size:13px;color:#909399;margin:0 0 8px">使用率</p>
            <p
              :style="{
                fontSize: '24px',
                fontWeight: 700,
                margin: 0,
                color: budgetVO?.usageRate >= 100 ? '#f56c6c' : '#00b96b'
              }"
            >
              {{ budgetVO?.usageRate != null ? budgetVO.usageRate + '%' : '—' }}
            </p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ③ 预警横幅 -->
    <div v-if="warningInfo" class="budget-warning" :class="`budget-warning--${warningInfo.type}`" style="margin-bottom:16px">
      <div class="budget-warning__icon">{{ warningInfo.icon }}</div>
      <div class="budget-warning__body">
        <div class="budget-warning__title">{{ warningInfo.title }}</div>
        <div class="budget-warning__text">{{ warningInfo.text }}</div>
      </div>
    </div>

    <!-- ④ 左右双图 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="10">
        <el-card header="预算使用率">
          <div ref="gaugeChartRef" style="height:260px"></div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card header="最近6个月趋势">
          <div ref="trendChartRef" style="height:260px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ⑤ 历史记录表 -->
    <el-card header="预算历史记录">
      <el-table :data="historyList" stripe>
        <el-table-column prop="budgetMonth" label="月份" width="120" />
        <el-table-column label="预算金额">
          <template #default="{ row }">{{ fmt(row.budgetAmount) }}</template>
        </el-table-column>
        <el-table-column label="实际支出">
          <template #default="{ row }">{{ fmt(row.totalExpense) }}</template>
        </el-table-column>
        <el-table-column label="使用率" width="220">
          <template #default="{ row }">
            <template v-if="row.usageRate != null">
              <el-progress
                :percentage="Math.min(Number(row.usageRate), 100)"
                :status="row.usageRate >= 100 ? 'exception' : row.usageRate >= 80 ? 'warning' : ''"
                :stroke-width="8"
              />
            </template>
            <span v-else>—</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- ⑥ 修改预算弹窗 -->
    <el-dialog v-model="dialogVisible" title="设置预算" width="400px">
      <el-form label-width="80px">
        <el-form-item label="月份">
          <el-date-picker
            v-model="form.budgetMonth"
            type="month"
            format="YYYY-MM"
            value-format="YYYY-MM"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="预算金额">
          <el-input-number
            v-model="form.amount"
            :min="0"
            :precision="2"
            style="width:100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveBudget">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.budget-page {
  padding: 20px;
  min-height: 100%;
}

.budget-warning {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 8px;
  border-left: 5px solid;
}
.budget-warning--error {
  background: #fff2f0;
  border-color: #f56c6c;
}
.budget-warning--warning {
  background: #fffbe6;
  border-color: #e6a23c;
}
.budget-warning--info {
  background: #f0f9eb;
  border-color: #00b96b;
}
.budget-warning__icon {
  font-size: 28px;
  line-height: 1;
  flex-shrink: 0;
}
.budget-warning__title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}
.budget-warning--error .budget-warning__title   { color: #f56c6c; }
.budget-warning--warning .budget-warning__title { color: #e6a23c; }
.budget-warning--info .budget-warning__title    { color: #00b96b; }
.budget-warning__text {
  font-size: 13px;
  color: #606266;
}
</style>
