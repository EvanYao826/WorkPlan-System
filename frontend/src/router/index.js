import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/LoginView.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/dashboard/DashboardView.vue'),
  },
  {
    path: '/plan',
    name: 'Plan',
    component: () => import('../views/plan/PlanView.vue'),
  },
  {
    path: '/result',
    name: 'Result',
    component: () => import('../views/result/ResultView.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫：未登录跳转登录页
router.beforeEach((to, from, next) => {
  if (to.meta.public) {
    next()
  } else if (!getToken()) {
    next('/login')
  } else {
    next()
  }
})

export default router
