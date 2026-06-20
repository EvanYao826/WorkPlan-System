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
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
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
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleDetail(row)">详情</el-button>
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
import { ref, reactive, onMounted } from 'vue'
import { getPlanList, getPlanDetail } from '../../api/plan'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const detailData = ref(null)

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

async function handleDetail(row) {
  const res = await getPlanDetail(row.id)
  detailData.value = res.data
  detailVisible.value = true
}

onMounted(() => {
  fetchData()
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
