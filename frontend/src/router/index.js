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
    component: () => import('../components/AppLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/DashboardView.vue'),
        meta: { title: '工作台' },
      },
      {
        path: 'plan',
        name: 'Plan',
        component: () => import('../views/plan/PlanView.vue'),
        meta: { title: '计划管理' },
      },
      {
        path: 'result',
        name: 'Result',
        component: () => import('../views/result/ResultView.vue'),
        meta: { title: '成果管理' },
      },
      {
        path: 'department',
        name: 'Department',
        component: () => import('../views/department/DepartmentView.vue'),
        meta: { title: '部门管理' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

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
