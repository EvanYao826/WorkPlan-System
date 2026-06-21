<template>
  <div class="dashboard">
    <!-- 欢迎栏 -->
    <div class="welcome-bar">
      <div>
        <h2>{{ greeting }}，{{ userStore.name }}</h2>
        <p>{{ today }} | {{ userStore.roleName }} · {{ userStore.username }}</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="item in statCards" :key="item.label">
        <div class="stat-card" :style="{ borderTop: `3px solid ${item.color}` }">
          <div class="stat-icon" :style="{ background: item.bg }">
            <el-icon :size="24" :style="{ color: item.color }">
              <component :is="item.icon" />
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-num">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span class="card-title">计划状态分布</span>
          </template>
          <div ref="planChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span class="card-title">成果状态分布</span>
          </template>
          <div ref="resultChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 通知 + 快捷入口 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="never" class="quick-card">
          <template #header>
            <span class="card-title">最近通知</span>
          </template>
          <div v-if="notifications.length === 0">
            <el-empty description="暂无通知" :image-size="60" />
          </div>
          <div v-else class="notification-list">
            <div
              v-for="item in notifications"
              :key="item.id"
              class="notification-item"
              @click="handleNotificationClick(item)"
            >
              <span class="dot" v-if="item.isRead === 0"></span>
              <span class="noti-title">{{ item.title }}</span>
              <span class="noti-time">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="quick-card">
          <template #header>
            <span class="card-title">快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button v-if="!isLeader" type="primary" @click="router.push('/plan?action=create')">
              <el-icon><Plus /></el-icon>新建计划
            </el-button>
            <el-button @click="router.push('/plan')">
              <el-icon><Document /></el-icon>查看计划
            </el-button>
            <el-button @click="router.push('/result')">
              <el-icon><Finished /></el-icon>查看成果
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/index'
import { getStatistics } from '../../api/statistics'
import { getNotificationList } from '../../api/notification'
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()
const isLeader = computed(() => userStore.role === 'LEADER')

const stats = ref({})
const notifications = ref([])

// 图表相关
const planChartRef = ref(null)
const resultChartRef = ref(null)
let planChart = null
let resultChart = null

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 12) return '上午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const today = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long',
})

const statCards = computed(() => {
  if (isLeader.value) {
    return [
      { label: '待审批计划', value: stats.value.pendingPlans ?? '—', icon: 'Document', color: '#e6a23c', bg: '#fdf6ec' },
      { label: '待审批成果', value: stats.value.pendingResults ?? '—', icon: 'Finished', color: '#f56c6c', bg: '#fef0f0' },
      { label: '待办总计', value: stats.value.pendingTotal ?? '—', icon: 'Bell', color: '#409eff', bg: '#ecf5ff' },
      { label: '审批中心', value: '→', icon: 'Checked', color: '#67c23a', bg: '#f0f9eb', action: () => router.push('/approve') },
    ]
  }
  return [
    { label: '我的计划', value: stats.value.myPlans ?? '—', icon: 'Document', color: '#409eff', bg: '#ecf5ff' },
    { label: '待审批', value: stats.value.pendingPlans ?? '—', icon: 'Clock', color: '#e6a23c', bg: '#fdf6ec' },
    { label: '我的成果', value: stats.value.myResults ?? '—', icon: 'Finished', color: '#67c23a', bg: '#f0f9eb' },
    { label: '未读通知', value: '—', icon: 'Bell', color: '#909399', bg: '#f4f4f5' },
  ]
})

function handleNotificationClick(item) {
  if (item.relatedType === 'PLAN') {
    router.push({ path: '/plan', query: { highlight: item.relatedId } })
  } else if (item.relatedType === 'RESULT') {
    router.push({ path: '/result', query: { highlight: item.relatedId } })
  }
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
}

onMounted(async () => {
  try {
    const res = await getStatistics()
    stats.value = res.data
  } catch (e) {
    // 静默失败
  }
  try {
    const res = await getNotificationList({ page: 1, size: 5 })
    notifications.value = res.data.records
  } catch (e) {
    // 静默失败
  }

  // 初始化图表
  await nextTick()
  initCharts()
})

onBeforeUnmount(() => {
  planChart?.dispose()
  resultChart?.dispose()
  window.removeEventListener('resize', handleResize)
})

function handleResize() {
  planChart?.resize()
  resultChart?.resize()
}

function initCharts() {
  const statusColors = {
    '草稿': '#909399',
    '待审批': '#e6a23c',
    '已通过': '#67c23a',
    '已驳回': '#f56c6c',
    '已撤回': '#409eff',
  }

  if (planChartRef.value) {
    planChart = echarts.init(planChartRef.value)
    const planData = isLeader.value
      ? [
          { value: stats.value.pendingPlans || 0, name: '待审批' },
          { value: stats.value.approvedPlans || 0, name: '已通过' },
          { value: stats.value.rejectedPlans || 0, name: '已驳回' },
        ]
      : [
          { value: stats.value.planDraft || 0, name: '草稿' },
          { value: stats.value.planPending || 0, name: '待审批' },
          { value: stats.value.planApproved || 0, name: '已通过' },
          { value: stats.value.planRejected || 0, name: '已驳回' },
          { value: stats.value.planWithdrawn || 0, name: '已撤回' },
        ]

    planChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, itemWidth: 10, itemHeight: 10 },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        data: planData.map(item => ({
          ...item,
          itemStyle: { color: statusColors[item.name] },
        })),
      }],
    })
  }

  if (resultChartRef.value) {
    resultChart = echarts.init(resultChartRef.value)
    const resultData = isLeader.value
      ? [
          { value: stats.value.pendingResults || 0, name: '待审批' },
          { value: stats.value.approvedResults || 0, name: '已通过' },
          { value: stats.value.rejectedResults || 0, name: '已驳回' },
        ]
      : [
          { value: stats.value.resultDraft || 0, name: '草稿' },
          { value: stats.value.resultPending || 0, name: '待审批' },
          { value: stats.value.resultApproved || 0, name: '已通过' },
          { value: stats.value.resultRejected || 0, name: '已驳回' },
          { value: stats.value.resultWithdrawn || 0, name: '已撤回' },
        ]

    resultChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, itemWidth: 10, itemHeight: 10 },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        data: resultData.map(item => ({
          ...item,
          itemStyle: { color: statusColors[item.name] },
        })),
      }],
    })
  }

  window.addEventListener('resize', handleResize)
}
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
}

.welcome-bar {
  margin-bottom: 20px;
}

.welcome-bar h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1f36;
  margin: 0 0 4px;
}

.welcome-bar p {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-num {
  font-size: 26px;
  font-weight: 700;
  color: #1a1f36;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.quick-card {
  border: 1px solid #f0f0f0;
}

.card-title {
  font-weight: 600;
  font-size: 15px;
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.notification-list {
  display: flex;
  flex-direction: column;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.15s;
  padding-left: 4px;
  padding-right: 4px;
  border-radius: 4px;
}
.notification-item:hover {
  background: #f5f7fa;
}
.notification-item:last-child {
  border-bottom: none;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #409eff;
  flex-shrink: 0;
}

.noti-title {
  flex: 1;
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.noti-time {
  font-size: 11px;
  color: #c0c4cc;
  flex-shrink: 0;
}

.chart-card {
  border: 1px solid #f0f0f0;
}

.chart-container {
  height: 240px;
}
</style>
