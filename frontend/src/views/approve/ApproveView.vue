<template>
  <div class="approve-page">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 待审批计划 -->
      <el-tab-pane label="待审批计划" name="plan">
        <el-card shadow="never" class="table-card">
          <el-table :data="planList" v-loading="loading" stripe>
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="title" label="计划标题" min-width="200" />
            <el-table-column prop="type" label="类型" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.type === 1 ? 'info' : 'warning'" size="small">
                  {{ row.type === 1 ? '日计划' : '月计划' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="planDate" label="计划日期" width="120" align="center" />
            <el-table-column prop="submitTime" label="提交时间" width="170" />
            <el-table-column prop="content" label="内容摘要" min-width="250" show-overflow-tooltip />
            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openApprove(row, 'plan')">
                  审批
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="planPagination.page"
              v-model:page-size="planPagination.size"
              :total="planPagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="fetchPlans"
              @current-change="fetchPlans"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 待审批成果 -->
      <el-tab-pane label="待审批成果" name="result">
        <el-card shadow="never" class="table-card">
          <el-table :data="resultList" v-loading="loading" stripe>
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="title" label="成果标题" min-width="200" />
            <el-table-column prop="planId" label="关联计划" width="140" align="center">
              <template #default="{ row }">
                <el-tag type="info" size="small">{{ row.planId }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="170" />
            <el-table-column prop="content" label="内容摘要" min-width="250" show-overflow-tooltip />
            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openApprove(row, 'result')">
                  审批
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="resultPagination.page"
              v-model:page-size="resultPagination.size"
              :total="resultPagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="fetchResults"
              @current-change="fetchResults"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 审批弹框 -->
    <ApproveDialog
      :visible="approveVisible"
      :plan="approveData"
      :type="approveType"
      @close="approveVisible = false"
      @success="handleApproveSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPlanList } from '../../api/plan'
import { getResultList } from '../../api/result'
import ApproveDialog from '../../components/ApproveDialog.vue'

const activeTab = ref('plan')
const loading = ref(false)

// 计划相关
const planList = ref([])
const planPagination = ref({ page: 1, size: 10, total: 0 })

// 成果相关
const resultList = ref([])
const resultPagination = ref({ page: 1, size: 10, total: 0 })

// 审批弹框
const approveVisible = ref(false)
const approveData = ref(null)
const approveType = ref('plan')

// 获取待审批计划
async function fetchPlans() {
  loading.value = true
  try {
    const res = await getPlanList({
      status: 1, // 待审批
      page: planPagination.value.page,
      size: planPagination.value.size,
    })
    planList.value = res.data.records
    planPagination.value.total = res.data.total
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

// 获取待审批成果
async function fetchResults() {
  loading.value = true
  try {
    const res = await getResultList({
      status: 1, // 待审批
      page: resultPagination.value.page,
      size: resultPagination.value.size,
    })
    resultList.value = res.data.records
    resultPagination.value.total = res.data.total
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

// 切换标签
function handleTabChange(tab) {
  if (tab === 'plan') {
    fetchPlans()
  } else {
    fetchResults()
  }
}

// 打开审批弹框
function openApprove(row, type) {
  approveData.value = row
  approveType.value = type
  approveVisible.value = true
}

// 审批成功回调
function handleApproveSuccess() {
  if (approveType.value === 'plan') {
    fetchPlans()
  } else {
    fetchResults()
  }
}

onMounted(() => {
  fetchPlans()
})
</script>

<style scoped>
.approve-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.table-card {
  border: 1px solid #f0f0f0;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
