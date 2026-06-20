import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
  const userId = ref(null)
  const username = ref('')
  const name = ref('')
  const role = ref('')

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

  return { userId, username, name, role, setUser, clearUser }
})
