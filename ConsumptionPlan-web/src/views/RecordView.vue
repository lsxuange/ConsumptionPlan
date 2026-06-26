<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRecords, addRecord, updateRecord, deleteRecord } from '@/api/record'
import { getCategories } from '@/api/category'

// ---- 工具函数 ----
function todayStr() {
  const d = new Date()
  return d.getFullYear() + '-' +
    String(d.getMonth() + 1).padStart(2, '0') + '-' +
    String(d.getDate()).padStart(2, '0')
}

// ---- 列表数据 ----
const loading = ref(false)
const records = ref([])
const total = ref(0)

// ---- 筛选 ----
const filter = reactive({
  type: null,      // null=全部, 0=支出, 1=收入
  categoryId: null,
  dateRange: null,  // [startDate, endDate]
  page: 1,
  size: 10
})

const filterCategories = ref([])
const filterCategoryLoading = ref(false)

// 筛选类型变化时加载对应分类
async function onFilterTypeChange(val) {
  filter.categoryId = null
  if (val === null || val === '') {
    filterCategories.value = []
    return
  }
  filterCategoryLoading.value = true
  try {
    filterCategories.value = await getCategories(val)
  } catch (err) {
    console.error('加载筛选分类失败', err)
    filterCategories.value = []
  } finally {
    filterCategoryLoading.value = false
  }
}

// 搜索
async function handleSearch() {
  filter.page = 1
  await loadRecords()
}

// 重置
function handleReset() {
  filter.type = null
  filter.categoryId = null
  filter.dateRange = null
  filterCategories.value = []
  filter.page = 1
  loadRecords()
}

// 加载记录列表
async function loadRecords() {
  loading.value = true
  try {
    const params = {
      page: filter.page,
      size: filter.size
    }
    if (filter.type !== null && filter.type !== '') {
      params.type = filter.type
    }
    if (filter.categoryId !== null && filter.categoryId !== '') {
      params.categoryId = filter.categoryId
    }
    if (filter.dateRange && filter.dateRange.length === 2) {
      params.startDate = filter.dateRange[0]
      params.endDate = filter.dateRange[1]
    }
    const res = await getRecords(params)
    records.value = res.records || res.list || []
    total.value = res.total || 0
  } catch (err) {
    console.error('加载记录失败', err)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  filter.page = page
  loadRecords()
}

// ---- 弹窗 ----
const dialogVisible = ref(false)
const dialogTitle = ref('新增记录')
const formRef = ref(null)
const submitting = ref(false)
const categoryList = ref([])
const categoryLoading = ref(false)

const form = reactive({
  id: null,
  type: 0,         // 0=支出, 1=收入
  categoryId: null,
  amount: null,
  recordDate: todayStr(),
  remark: ''
})

const rules = {
  type: [{ required: true, message: '请选择收支类型', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  recordDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

// 加载弹窗分类
async function loadDialogCategories(type) {
  categoryLoading.value = true
  try {
    categoryList.value = await getCategories(type)
  } catch (err) {
    console.error('加载分类失败', err)
    categoryList.value = []
  } finally {
    categoryLoading.value = false
  }
}

// 弹窗内切换收支类型
function onTypeChange(val) {
  form.categoryId = null
  loadDialogCategories(val)
}

// 打开新增弹窗
async function openAdd() {
  dialogTitle.value = '新增记录'
  Object.assign(form, {
    id: null,
    type: 0,
    categoryId: null,
    amount: null,
    recordDate: todayStr(),
    remark: ''
  })
  await loadDialogCategories(0)
  dialogVisible.value = true
}

// 打开编辑弹窗
async function openEdit(row) {
  dialogTitle.value = '编辑记录'
  Object.assign(form, {
    id: row.id,
    type: row.type,
    categoryId: row.categoryId,
    amount: row.amount,
    recordDate: row.recordDate || '',
    remark: row.remark || ''
  })
  await loadDialogCategories(row.type)
  dialogVisible.value = true
}

// 提交
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const { id, ...data } = form
    if (id) {
      await updateRecord(id, data)
      ElMessage.success('更新成功')
    } else {
      await addRecord(data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadRecords()
  } catch (err) {
    ElMessage.error(err.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteRecord(row.id)
    ElMessage.success('删除成功')
    loadRecords()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err?.message || '删除失败')
    }
  }
}

// ---- 初始化 ----
onMounted(() => {
  loadRecords()
})
</script>

<template>
  <div class="record-page">
    <!-- 筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent="handleSearch">
        <el-form-item label="收支类型">
          <el-select
            v-model="filter.type"
            placeholder="全部"
            clearable
            style="width: 120px"
            @change="onFilterTypeChange"
          >
            <el-option label="全部" :value="null" />
            <el-option label="支出" :value="0" />
            <el-option label="收入" :value="1" />
          </el-select>
        </el-form-item>

        <el-form-item label="分类" v-if="filter.type !== null && filter.type !== ''">
          <el-select
            v-model="filter.categoryId"
            placeholder="全部分类"
            clearable
            :loading="filterCategoryLoading"
            style="width: 160px"
          >
            <el-option
              v-for="cat in filterCategories"
              :key="cat.id"
              :label="cat.name || cat.categoryName"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filter.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="openAdd">+ 新增记录</el-button>
      </div>

      <el-table v-loading="loading" :data="records" stripe style="width: 100%">
        <el-table-column prop="recordDate" label="日期" width="120" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'success' : 'danger'" size="small">
              {{ row.type === 1 ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="amount" label="金额" width="140">
          <template #default="{ row }">
            <span :class="row.type === 1 ? 'text-green' : 'text-red'">
              {{ row.type === 1 ? '+' : '-' }}¥ {{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination" v-if="total > filter.size">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="filter.size"
          :current-page="filter.page"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="收支类型" prop="type">
          <el-radio-group v-model="form.type" @change="onTypeChange">
            <el-radio :value="0">支出</el-radio>
            <el-radio :value="1">收入</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select
            v-model="form.categoryId"
            placeholder="请选择分类"
            :loading="categoryLoading"
            style="width: 100%"
          >
            <el-option
              v-for="cat in categoryList"
              :key="cat.id"
              :label="cat.name || cat.categoryName"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="金额" prop="amount">
          <el-input-number
            v-model="form.amount"
            :precision="2"
            :min="0.01"
            :step="1"
            placeholder="请输入金额"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="日期" prop="recordDate">
          <el-date-picker
            v-model="form.recordDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="备注信息（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.record-page {
  padding: 20px;
}

.filter-card {
  margin-bottom: 16px;
}

.toolbar {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.text-green {
  color: #67C23A;
  font-weight: 600;
}

.text-red {
  color: #F56C6C;
  font-weight: 600;
}
</style>
