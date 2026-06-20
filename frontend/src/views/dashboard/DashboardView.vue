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

    <!-- 快捷入口 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="never" class="quick-card">
          <template #header>
            <span class="card-title">快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button v-if="!isLeader" @click="router.push('/plan?action=create')">
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
      <el-col :span="12">
        <el-card shadow="never" class="quick-card">
          <template #header>
            <span class="card-title">最近通知</span>
          </template>
          <el-empty description="暂无通知" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/index'
import { getStatistics } from '../../api/statistics'

const router = useRouter()
const userStore = useUserStore()
const isLeader = computed(() => userStore.role === 'LEADER')

const stats = ref({})

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

onMounted(async () => {
  try {
    const res = await getStatistics()
    stats.value = res.data
  } catch (e) {
    // 静默失败
  }
})
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
}
</style>
