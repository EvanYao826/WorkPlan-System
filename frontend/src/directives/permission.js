import { useUserStore } from '../store/index'

/**
 * 权限指令 v-permission
 * 用法：v-permission="'LEADER'" 或 v-permission="'EMPLOYEE'"
 * 不匹配角色时，元素会被隐藏
 */
export const permission = {
  mounted(el, binding) {
    checkPermission(el, binding)
  },
  updated(el, binding) {
    checkPermission(el, binding)
  },
}

function checkPermission(el, binding) {
  const requiredRole = binding.value
  if (!requiredRole) return

  const userStore = useUserStore()
  el.style.display = userStore.role === requiredRole ? '' : 'none'
}
