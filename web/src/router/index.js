import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getToken } from '../utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('../layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/DashboardView.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('../views/system/user/UserView.vue'),
        meta: { title: '用户管理', permission: 'system:user:list' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('../views/system/role/RoleView.vue'),
        meta: { title: '角色管理', permission: 'system:role:list' }
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('../views/system/menu/MenuView.vue'),
        meta: { title: '菜单管理', permission: 'system:menu:list' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  const token = getToken()
  if (to.meta.public) {
    return token ? '/dashboard' : true
  }
  if (!token) {
    return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  }
  if (!userStore.profile) {
    try {
      await userStore.loadUserContext()
    } catch (error) {
      userStore.reset()
      return `/login?redirect=${encodeURIComponent(to.fullPath)}`
    }
  }
  const permission = to.meta.permission
  if (permission && !userStore.permissions.includes(permission)) {
    ElMessage.warning('当前账号没有访问该页面的权限')
    return '/dashboard'
  }
  return true
})

export default router
