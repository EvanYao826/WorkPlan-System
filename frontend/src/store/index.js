import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserInfo } from '../utils/auth'

/**
 * 用户状态管理
 * 初始化时从 localStorage 恢复，刷新页面不丢数据
 */
export const useUserStore = defineStore('user', () => {
  const saved = getUserInfo()

  const userId = ref(saved?.userId || null)
  const username = ref(saved?.username || '')
  const name = ref(saved?.name || '')
  const role = ref(saved?.role || '')

  const roleName = computed(() => role.value === 'LEADER' ? '领导' : '同事')

  function setUser(info) {
    userId.value = info.userId
    username.value = info.username
    name.value = info.name
    role.value = info.role
  }

  function clearUser() {
    userId.value = null
    username.value = ''
    name.value = ''
    role.value = ''
  }

  return { userId, username, name, role, roleName, setUser, clearUser }
})
