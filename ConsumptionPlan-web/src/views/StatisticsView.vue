<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { getRecords } from '@/api/record'
import * as echarts from 'echarts'

// ---- 工具函数 ----
function defaultMonth() {
  const d = new Date()
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0')
}

function getMonthRange(monthStr) {
  const [y, m] = monthStr.split('-').map(Number)
  const first = monthStr + '-01'
  const last = monthStr + '-' + String(new Date(y, m, 0).getDate()).padStart(2, '0')
  return { first, last }
}

// ---- 状态 ----
const selectedMonth = ref(defaultMonth())
const loading = ref(false)
const allRecords = ref([])

// DOM 引用
const expenseChartRef = ref(null)
const incomeChartRef = ref(null)

let expenseChart = null
let incomeChart = null

// 热力图数据
const heatmapData = ref({})
const heatmapMonth = ref('')

const fillerCount = computed(() => {
  const [y, m] = selectedMonth.value.split('-').map(Number)
  return new Date(y, m - 1, 1).getDay()
})

const calendarDays = computed(() => {
  const month = selectedMonth.value
  const [y, m] = month.split('-').map(Number)
  const daysInMonth = new Date(y, m, 0).getDate()
  const maxAmount = Math.max(...Object.values(heatmapData.value), 0)

  const cells = []
  for (let day = 1; day <= daysInMonth; day++) {
    const dateStr = `${month}-${String(day).padStart(2, '0')}`
    const amount = heatmapData.value[dateStr] || 0

    let level = 0
    if (amount > 0 && maxAmount > 0) {
      if (amount <= maxAmount * 0.25) level = 1
      else if (amount <= maxAmount * 0.5) level = 2
      else if (amount <= maxAmount * 0.75) level = 3
      else level = 4
    }

    cells.push({ date: dateStr, day, amount, level })
  }
  return cells
})

// ---- 数据处理 ----
function groupByCategory(records, type) {
  const map = {}
  records
    .filter(r => r.type === type)
    .forEach(r => {
      const name = r.categoryName || '其他'
      map[name] = (map[name] || 0) + Number(r.amount || 0)
    })
  return Object.entries(map).map(([name, value]) => ({ name, value }))
}

// ---- 图表初始化 ----
function setNoData(chart) {
  chart.setOption({
    graphic: {
      type: 'text',
      left: 'center',
      top: 'center',
      style: { text: '暂无数据', fill: '#909399', fontSize: 14 }
    }
  })
}

function renderExpenseChart(data) {
  if (!data || data.length === 0) return setNoData(expenseChart)
  expenseChart.setOption({
    title: { text: '支出分类占比', left: 'center', top: 0, textStyle: { fontSize: 15 } },
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '48%'],
      label: { formatter: '{b}\n{d}%' },
      data
    }]
  })
}

function renderIncomeChart(data) {
  if (!data || data.length === 0) return setNoData(incomeChart)
  incomeChart.setOption({
    title: { text: '收入来源占比', left: 'center', top: 0, textStyle: { fontSize: 15 } },
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '48%'],
      label: { formatter: '{b}\n{d}%' },
      data
    }]
  })
}

// ---- 热力图数据加载 ----
async function loadHeatmapData() {
  const month = selectedMonth.value
  const { first, last } = getMonthRange(month)
  try {
    const res = await getRecords({ type: 0, page: 1, size: 200, startDate: first, endDate: last })
    const records = res?.records ?? res ?? []

    const map = {}
    records.forEach(r => {
      const date = r.recordDate
      if (date) {
        map[date] = (map[date] || 0) + Number(r.amount || 0)
      }
    })
    heatmapData.value = map
    heatmapMonth.value = month
  } catch (err) {
    console.error('加载热力图数据失败', err)
    heatmapData.value = {}
  }
}

// ---- 数据加载 ----
async function loadData() {
  loading.value = true
  const { first, last } = getMonthRange(selectedMonth.value)
  try {
    const res = await getRecords({ page: 1, size: 200, startDate: first, endDate: last })
    allRecords.value = res?.records ?? res ?? []
  } catch (err) {
    console.error('加载数据失败', err)
    allRecords.value = []
  } finally {
    loading.value = false
  }
}

async function renderAll() {
  const records = allRecords.value
  renderExpenseChart(groupByCategory(records, 0))
  renderIncomeChart(groupByCategory(records, 1))
  await loadHeatmapData()
}

// ---- 响应式 ----
watch(selectedMonth, async () => {
  await loadData()
  await renderAll()
})

// ---- resize ----
function handleResize() {
  expenseChart?.resize()
  incomeChart?.resize()
}

onMounted(async () => {
  // 初始化图表实例
  expenseChart = echarts.init(expenseChartRef.value)
  incomeChart = echarts.init(incomeChartRef.value)

  await loadData()
  await renderAll()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  expenseChart?.dispose()
  incomeChart?.dispose()
})
</script>

<template>
  <div class="stats-page">
    <!-- 顶部月份选择 -->
    <el-card shadow="never" class="toolbar-card">
      <el-form :inline="true">
        <el-form-item label="选择月份">
          <el-date-picker
            v-model="selectedMonth"
            type="month"
            placeholder="选择月份"
            value-format="YYYY-MM"
            style="width: 180px"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 图表区 -->
    <div v-loading="loading">
      <el-row :gutter="20" class="chart-row">
        <el-col :span="12">
          <el-card shadow="never">
            <div ref="expenseChartRef" class="chart-box"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never">
            <div ref="incomeChartRef" class="chart-box"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="24">
          <el-card shadow="never">
            <template #header>
              <span class="card-title">消费日历</span>
            </template>
            <div class="heatmap-container heatmap-wrapper">
              <div class="heatmap-header">
                <span v-for="d in ['日','一','二','三','四','五','六']" :key="d">{{ d }}</span>
              </div>
              <div class="heatmap-grid">
                <div v-for="i in fillerCount" :key="'f-'+i" class="heatmap-cell filler"></div>
                <div
                  v-for="cell in calendarDays"
                  :key="cell.date"
                  class="heatmap-cell"
                  :class="`level-${cell.level}`"
                  :title="`${cell.date}  ¥${cell.amount.toFixed(2)}`"
                >
                  <span class="day-num">{{ cell.day }}</span>
                </div>
              </div>
              <div class="heatmap-legend">
                <span>消费少</span>
                <div class="level-0 legend-box"></div>
                <div class="level-1 legend-box"></div>
                <div class="level-2 legend-box"></div>
                <div class="level-3 legend-box"></div>
                <div class="level-4 legend-box"></div>
                <span>消费多</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
.stats-page {
  padding: 20px;
}

.toolbar-card {
  margin-bottom: 20px;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-box {
  width: 100%;
  height: 300px;
}

/* 消费日历热力图 */
.card-title {
  font-size: 15px;
  font-weight: 600;
}

.heatmap-container { padding: 8px 0; }
.heatmap-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}
.heatmap-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}
.heatmap-cell {
  height: 52px;
  border-radius: 6px;
  position: relative;
  cursor: default;
  display: flex;
  align-items: center;
  justify-content: center;
}
.heatmap-cell .day-num {
  font-size: 13px;
  color: #fff;
  opacity: 0.85;
  pointer-events: none;
}
.filler { background: transparent !important; }
.level-0 { background: #ebedf0; }
.level-0 .day-num { color: #909399; }
.level-1 { background: #c6e48b; }
.level-2 { background: #7bc96f; }
.level-3 { background: #239a3b; }
.level-4 { background: #196127; }
.heatmap-legend {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}
/* 容器限高 */
.heatmap-wrapper {
  max-height: 420px;
}

.legend-box { width: 14px; height: 14px; border-radius: 3px; }
</style>

