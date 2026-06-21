<template>
  <el-popover
    placement="bottom-end"
    :width="360"
    trigger="click"
    @show="loadNotifications"
  >
    <template #reference>
      <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" class="bell-badge">
        <el-icon class="bell-icon" :size="20"><Bell /></el-icon>
      </el-badge>
      <el-icon v-else class="bell-icon" :size="20"><Bell /></el-icon>
    </template>

    <div class="notification-panel">
      <div class="panel-header">
        <span class="panel-title">消息通知</span>
        <div class="header-actions">
          <el-button
            type="primary"
            link
            size="small"
            @click="confirmMarkAllRead"
          >
            全部已读
          </el-button>
          <el-button
            type="danger"
            link
            size="small"
            @click="confirmClearRead"
          >
            全部清除
          </el-button>
        </div>
      </div>

      <el-scrollbar max-height="320px">
        <div v-if="notifications.length === 0" class="empty-tip">
          <el-empty description="暂无通知" :image-size="48" />
        </div>
        <div
          v-for="item in notifications"
          :key="item.id"
          class="notification-item"
          :class="{ unread: item.isRead === 0 }"
          @click="handleClickItem(item)"
        >
          <div class="item-title">
            <span class="dot" v-if="item.isRead === 0"></span>
            {{ item.title }}
          </div>
          <div class="item-content">{{ item.content }}</div>
          <div class="item-time">{{ formatTime(item.createTime) }}</div>
        </div>
      </el-scrollbar>
    </div>
  </el-popover>

  <!-- 全部已读确认弹框 -->
  <el-dialog v-model="showMarkAllDialog" title="确认操作" width="360px" :append-to-body="true">
    <p>确定将所有未读消息标记为已读吗？</p>
    <template #footer>
      <el-button @click="showMarkAllDialog = false">取消</el-button>
      <el-button type="primary" @click="handleMarkAllRead">确定</el-button>
    </template>
  </el-dialog>

  <!-- 全部清除确认弹框 -->
  <el-dialog v-model="showClearDialog" title="确认操作" width="360px" :append-to-body="true">
    <p>确定清除所有已读消息吗？此操作不可恢复。</p>
    <template #footer>
      <el-button @click="showClearDialog = false">取消</el-button>
      <el-button type="danger" @click="handleClearRead">确定清除</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getNotificationList,
  getUnreadCount,
  markAsRead,
  markAllAsRead,
  clearReadNotifications,
} from '../api/notification'

const router = useRouter()
const unreadCount = ref(0)
const notifications = ref([])
const showMarkAllDialog = ref(false)
const showClearDialog = ref(false)

async function loadUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data.count
  } catch (e) {
    // 静默失败
  }
}

async function loadNotifications() {
  try {
    const res = await getNotificationList({ page: 1, size: 20 })
    notifications.value = res.data.records
  } catch (e) {
    // 静默失败
  }
}

async function handleClickItem(item) {
  if (item.isRead === 0) {
    try {
      await markAsRead(item.id)
      item.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (e) {
      // 静默失败
    }
  }
  // 跳转到关联页面，携带高亮 ID
  if (item.relatedType === 'PLAN') {
    router.push({ path: '/plan', query: { highlight: item.relatedId } })
  } else if (item.relatedType === 'RESULT') {
    router.push({ path: '/result', query: { highlight: item.relatedId } })
  }
}

function confirmMarkAllRead() {
  if (unreadCount.value === 0) {
    ElMessage.info('没有未读消息')
    return
  }
  showMarkAllDialog.value = true
}

async function handleMarkAllRead() {
  try {
    await markAllAsRead()
    notifications.value.forEach(n => { n.isRead = 1 })
    unreadCount.value = 0
    showMarkAllDialog.value = false
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    // 静默失败
  }
}

function confirmClearRead() {
  const hasRead = notifications.value.some(n => n.isRead === 1)
  if (!hasRead) {
    ElMessage.info('没有已读消息可清除')
    return
  }
  showClearDialog.value = true
}

async function handleClearRead() {
  try {
    await clearReadNotifications()
    notifications.value = notifications.value.filter(n => n.isRead === 0)
    showClearDialog.value = false
    ElMessage.success('已读消息已清除')
  } catch (e) {
    // 静默失败
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

onMounted(() => {
  loadUnreadCount()
})

defineExpose({ loadUnreadCount })
</script>

<style scoped>
.bell-badge {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.bell-icon {
  color: #606266;
  transition: color 0.2s;
  cursor: pointer;
}
.bell-icon:hover {
  color: #409eff;
}

.notification-panel {
  margin: -12px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.empty-tip {
  padding: 20px 0;
}

.notification-item {
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.15s;
  border-bottom: 1px solid #f5f5f5;
}
.notification-item:hover {
  background: #f5f7fa;
}
.notification-item.unread {
  background: #ecf5ff;
}

.item-title {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #409eff;
  flex-shrink: 0;
}

.item-content {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
}
</style>
