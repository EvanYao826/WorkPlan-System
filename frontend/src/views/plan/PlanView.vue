<template>
  <div class="plan-page">
    <!-- 筛选区 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter">
        <el-form-item label="类型">
          <el-select v-model="filter.type" placeholder="全部" clearable style="width: 120px">
            <el-option label="日计划" :value="1" />
            <el-option label="月计划" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="filter.title" placeholder="搜索标题" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="resetFilter">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
          <el-button v-if="!isLeader" type="success" @click="openCreate">
            <el-icon><Plus /></el-icon>新建计划
          </el-button>
          <el-button type="warning" @click="handleExport">
            <el-icon><Download /></el-icon>导出
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe :row-class-name="rowClassName">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="title" label="计划标题" min-width="180" />
        <el-table-column prop="type" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'info' : 'warning'" size="small">
              {{ row.type === 1 ? '日计划' : '月计划' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="planDate" label="计划日期" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approveComment" label="审批意见" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ 'comment-empty': !row.approveComment }">
              {{ row.approveComment || '—' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <!-- 同事操作 -->
            <template v-if="!isLeader || row.userId === currentUserId">
              <el-button v-if="row.status === 0 || row.status === 3" type="primary" link size="small" @click="handleSubmit(row)">提交</el-button>
              <el-button v-if="row.status === 0 || row.status === 3" type="warning" link size="small" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="row.status === 1" type="danger" link size="small" @click="handleWithdraw(row)">撤回</el-button>
            </template>
            <!-- 领导操作 -->
            <template v-if="isLeader && row.status === 1">
              <el-button type="success" link size="small" @click="openApprove(row)">审批</el-button>
            </template>
            <el-button type="info" link size="small" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 新建/编辑弹框 -->
    <PlanForm
      :visible="formVisible"
      :edit-data="editData"
      @close="formVisible = false"
      @success="fetchData"
    />

    <!-- 审批弹框 -->
    <ApproveDialog
      :visible="approveVisible"
      :plan="approveData"
      @close="approveVisible = false"
      @success="fetchData"
    />

    <!-- 详情弹框 -->
    <el-dialog v-model="detailVisible" title="计划详情" width="600px">
      <template v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题" :span="2">{{ detailData.title }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detailData.type === 1 ? '日计划' : '月计划' }}</el-descriptions-item>
          <el-descriptions-item label="计划日期">{{ detailData.planDate }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detailData.status)" size="small">
              {{ statusLabel(detailData.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
          <el-descriptions-item label="计划内容" :span="2">
            <div style="white-space: pre-wrap; line-height: 1.8">{{ detailData.content || '暂无内容' }}</div>
          </el-descriptions-item>
          <el-descriptions-item v-if="detailData.approveComment" label="审批意见" :span="2">
            {{ detailData.approveComment }}
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPlanList, getPlanDetail, submitPlan, withdrawPlan } from '../../api/plan'
import { exportPlans } from '../../api/export'
import { useUserStore } from '../../store/index'
import PlanForm from '../../components/PlanForm.vue'
import ApproveDialog from '../../components/ApproveDialog.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isLeader = computed(() => userStore.role === 'LEADER')
const currentUserId = computed(() => userStore.userId)

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const detailData = ref(null)
const formVisible = ref(false)
const editData = ref(null)
const approveVisible = ref(false)
const approveData = ref(null)

const filter = reactive({
  type: null,
  status: null,
  title: '',
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

const statusOptions = [
  { label: '草稿', value: 0 },
  { label: '待审批', value: 1 },
  { label: '已通过', value: 2 },
  { label: '已驳回', value: 3 },
  { label: '已撤回', value: 4 },
]

function statusLabel(status) {
  return statusOptions.find(s => s.value === status)?.label || '未知'
}

function statusTagType(status) {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }
  return map[status] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getPlanList({
      ...filter,
      page: pagination.page,
      size: pagination.size,
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  filter.type = null
  filter.status = null
  filter.title = ''
  pagination.page = 1
  fetchData()
}

function openCreate() {
  editData.value = null
  formVisible.value = true
}

function openEdit(row) {
  editData.value = row
  formVisible.value = true
}

function openApprove(row) {
  approveData.value = row
  approveVisible.value = true
}

async function handleSubmit(row) {
  if (!row.approveLeaderId) {
    ElMessage.warning('请先编辑计划并选择审批领导')
    openEdit(row)
    return
  }
  await ElMessageBox.confirm('确认提交审批？', '提示', { type: 'info' })
  try {
    await submitPlan(row.id, row.approveLeaderId)
    ElMessage.success('已提交审批')
    fetchData()
  } catch (e) {
    // 错误已由拦截器处理
  }
}

async function handleWithdraw(row) {
  await ElMessageBox.confirm('确认撤回？撤回后将变为草稿状态。', '提示', { type: 'warning' })
  try {
    await withdrawPlan(row.id)
    ElMessage.success('已撤回')
    fetchData()
  } catch (e) {
    // 错误已由拦截器处理
  }
}

async function handleExport() {
  try {
    const res = await exportPlans()
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '计划列表.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

async function handleDetail(row) {
  const res = await getPlanDetail(row.id)
  detailData.value = res.data
  detailVisible.value = true
}

function rowClassName({ row }) {
  return `row-id-${row.id}`
}

function startHighlight(id) {
  // 多等一会，确保 el-table 渲染完成
  setTimeout(() => {
    const row = document.querySelector(`.row-id-${id}`)
    if (!row) {
      console.warn('[highlight] 未找到行, id=', id, '当前页面数据ids=', tableData.value.map(r => r.id))
      return
    }
    const cells = row.querySelectorAll('td')
    let count = 0
    const interval = setInterval(() => {
      const color = count % 2 === 0 ? '#d9ecff' : ''
      cells.forEach(td => { td.style.backgroundColor = color })
      count++
      if (count >= 6) {
        clearInterval(interval)
        cells.forEach(td => { td.style.backgroundColor = '' })
        router.replace({ query: { ...route.query, highlight: undefined } })
      }
    }, 300)
  }, 800)
}

watch(() => route.query.highlight, (val) => {
  if (val && tableData.value.length > 0) {
    startHighlight(val)
  }
})

onMounted(async () => {
  await fetchData()
  // 快捷操作跳转过来时自动打开新建弹框
  if (route.query.action === 'create') {
    openCreate()
  }
  // 通知跳转过来时高亮对应行
  if (route.query.highlight) {
    startHighlight(route.query.highlight)
  }
})
</script>

<style scoped>
.plan-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card {
  border: 1px solid #f0f0f0;
}

.filter-card .el-form-item {
  margin-bottom: 0;
}

.table-card {
  border: 1px solid #f0f0f0;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.comment-empty {
  color: #c0c4cc;
}
</style>
