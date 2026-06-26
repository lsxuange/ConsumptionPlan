<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { getDashboard } from '@/api/dashboard'
import { getRecords } from '@/api/record'
import * as echarts from 'echarts'

const loading = ref(true)

const data = reactive({
  monthIncome: 0,
  monthExpense: 0,
  monthBalance: 0,
  budgetAmount: 0,
  budgetUsageRate: 0,
  expenseCategoryList: [],
  trendList: [],
  incomeSourceList: []
})

// 图表 DOM 引用
const trendChartRef = ref(null)
const dailyChartRef = ref(null)
const gaugeChartRef = ref(null)

let trendChart = null
let dailyChart = null
let gaugeChart = null

// 金额格式化：¥ x,xxx.xx
function formatMoney(value) {
  return '¥ ' + Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 初始化收支趋势折线图
function initTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)

  if (!data.trendList || data.trendList.length === 0) {
    trendChart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#909399', fontSize: 14 } }
    })
    return
  }

  trendChart.setOption({
    title: { text: '收支趋势', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    legend: { data: ['收入', '支出'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '40px', top: '50px', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.trendList.map(t => t.month)
    },
    yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
    series: [
      {
        name: '收入',
        type: 'line',
        smooth: true,
        data: data.trendList.map(t => t.income),
        itemStyle: { color: '#67c23a' },
        areaStyle: { opacity: 0.1 }
      },
      {
        name: '支出',
        type: 'line',
        smooth: true,
        data: data.trendList.map(t => t.expense),
        itemStyle: { color: '#f56c6c' },
        areaStyle: { opacity: 0.1 }
      }
    ]
  })
}

// 初始化当月每日收支柱状图
async function initDailyChart() {
  if (!dailyChartRef.value) return

  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const startDate = `${year}-${month}-01`
  const lastDay = new Date(year, today.getMonth() + 1, 0).getDate()
  const endDate = `${year}-${month}-${String(lastDay).padStart(2, '0')}`

  let records = []
  try {
    const recordRes = await getRecords({ page: 1, size: 500, startDate, endDate })
    records = recordRes?.records ?? recordRes ?? []
  } catch (err) {
    console.error('获取本月记录失败', err)
  }

  const days = Array.from({ length: lastDay }, (_, i) =>
    `${year}-${month}-${String(i + 1).padStart(2, '0')}`
  )

  const dailyExpense = days.map(d =>
    records.filter(r => r.recordDate === d && r.type === 0)
           .reduce((sum, r) => sum + Number(r.amount), 0)
  )
  const dailyIncome = days.map(d =>
    records.filter(r => r.recordDate === d && r.type === 1)
           .reduce((sum, r) => sum + Number(r.amount), 0)
  )

  const xLabels = days.map(d => String(Number(d.split('-')[2])))

  dailyChart = echarts.init(dailyChartRef.value)
  dailyChart.setOption({
    title: { text: '本月每日收支', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        let str = `${params[0].axisValue}日<br/>`
        params.forEach(p => { str += `${p.marker}${p.seriesName}: ¥${p.value.toFixed(2)}<br/>` })
        return str
      }
    },
    legend: { data: ['支出', '收入'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '40px', top: '50px', containLabel: true },
    xAxis: {
      type: 'category',
      data: xLabels,
      axisLabel: { fontSize: 11 }
    },
    yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
    series: [
      {
        name: '支出',
        type: 'bar',
        data: dailyExpense,
        itemStyle: { color: '#f56c6c', borderRadius: [3, 3, 0, 0] },
        barMaxWidth: 20
      },
      {
        name: '收入',
        type: 'bar',
        data: dailyIncome,
        itemStyle: { color: '#67c23a', borderRadius: [3, 3, 0, 0] },
        barMaxWidth: 20
      }
    ]
  })
}

// 初始化预算仪表盘
function initGaugeChart() {
  if (!gaugeChartRef.value) return

  const usageRate = Number(data.budgetUsageRate) || 0

  if (usageRate === 0) {
    gaugeChart = echarts.init(gaugeChartRef.value)
    gaugeChart.setOption({
      title: { text: '暂未设置预算', left: 'center', top: 'center', textStyle: { color: '#909399', fontSize: 14 } }
    })
    return
  }

  gaugeChart = echarts.init(gaugeChartRef.value)
  gaugeChart.setOption({
    title: { text: '预算执行情况', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { formatter: '{a} <br/>{b}: {c}%' },
    series: [
      {
        name: '预算使用率',
        type: 'gauge',
        min: 0,
        max: 100,
        radius: '80%',
        axisLine: {
          lineStyle: {
            width: 20,
            color: [
              [0.6, '#67c23a'],
              [0.8, '#e6a23c'],
              [1, '#f56c6c']
            ]
          }
        },
        pointer: { itemStyle: { color: 'auto' } },
        axisTick: { distance: -20, length: 6, lineStyle: { color: '#fff', width: 1 } },
        splitLine: { distance: -20, length: 14, lineStyle: { color: '#fff', width: 2 } },
        axisLabel: { color: 'inherit', distance: 28, fontSize: 11 },
        detail: {
          valueAnimation: true,
          formatter: '{value}%',
          color: 'inherit',
          fontSize: 20,
          fontWeight: 'bold',
          offsetCenter: [0, '70%']
        },
        data: [{ value: Math.min(Number(usageRate.toFixed(1)), 100), name: '使用率' }]
      }
    ]
  })
}

// 窗口 resize 时重绘图表
function handleResize() {
  trendChart?.resize()
  dailyChart?.resize()
  gaugeChart?.resize()
}

onMounted(async () => {
  try {
    const res = await getDashboard()
    Object.assign(data, res)
  } catch (err) {
    console.error('获取仪表盘数据失败', err)
  } finally {
    loading.value = false
  }

  // 等 DOM 渲染后再初始化图表
  setTimeout(() => {
    initTrendChart()
    initDailyChart()
    initGaugeChart()
  }, 100)

  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  dailyChart?.dispose()
  gaugeChart?.dispose()
})
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-body">
            <div class="stat-icon income-icon">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">本月收入</p>
              <p class="stat-value income-text">{{ formatMoney(data.monthIncome) }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-body">
            <div class="stat-icon expense-icon">
              <el-icon :size="28"><ShoppingCart /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">本月支出</p>
              <p class="stat-value expense-text">{{ formatMoney(data.monthExpense) }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-body">
            <div class="stat-icon balance-icon">
              <el-icon :size="28"><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">本月结余</p>
              <p class="stat-value balance-text">{{ formatMoney(data.monthBalance) }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-body">
            <div class="stat-icon budget-icon">
              <el-icon :size="28"><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">预算使用率</p>
              <p class="stat-value budget-text">{{ data.budgetUsageRate }}%</p>
              <el-progress
                :percentage="Math.min(data.budgetUsageRate, 100)"
                :color="data.budgetUsageRate > 100 ? '#F56C6C' : '#E6A23C'"
                :show-text="false"
                :stroke-width="8"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行：全宽 = 本月每日收支柱状图 -->
    <el-card shadow="hover" class="chart-row">
      <div ref="dailyChartRef" class="chart-box"></div>
    </el-card>

    <!-- 第二行：预算执行仪表盘 + 收支趋势折线图 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <div ref="gaugeChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <div ref="trendChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 20px;
  min-height: 100%;
}

/* 统计卡片 */
.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 130px;
  display: flex;
  align-items: center;
}

.stat-card :deep(.el-card__body) {
  width: 100%;
  padding: 20px;
}

.stat-body {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.income-icon { background: linear-gradient(135deg, #67C23A, #529b2e); }
.expense-icon { background: linear-gradient(135deg, #F56C6C, #c45656); }
.balance-icon { background: linear-gradient(135deg, #409EFF, #337ecc); }
.budget-icon { background: linear-gradient(135deg, #E6A23C, #b88230); }

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin: 0 0 6px;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.income-text { color: #67C23A; }
.expense-text { color: #F56C6C; }
.balance-text { color: #409EFF; }
.budget-text { color: #E6A23C; }

/* 图表区域 */
.chart-row {
  margin-bottom: 20px;
}

.chart-box {
  width: 100%;
  height: 300px;
}
</style>

