import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getToken } from '../utils/auth'
import { resolveComponent } from '../utils/componentMap'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../layout/AdminLayout.vue'),
    children: [
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/profile/ProfileView.vue'),
        meta: { title: '个人资料' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

let routesRegistered = false
const staticPaths = new Set(['profile'])

export function registerRoutes(menus) {
  resetRoutes()
  addMenuRoutes(menus, '')
}

export function resetRoutes() {
  routesRegistered = false
  router.getRoutes().forEach((route) => {
    if (route.name?.toString().startsWith('Menu')) {
      router.removeRoute(route.name)
    }
  })
}

function addMenuRoutes(menus, parentPath) {
  for (const menu of menus) {
    if (menu.visible === 0 || menu.menuType === 2) continue
    let fullPath
    if (menu.path?.startsWith('/')) {
      fullPath = menu.path
    } else if (menu.path) {
      fullPath = `${parentPath}/${menu.path}`.replace(/\/+/g, '/')
    } else {
      fullPath = parentPath || '/'
    }
    const routePath = fullPath.replace(/^\//, '')
    if (menu.component && menu.component !== 'Layout' && !staticPaths.has(routePath)) {
      const componentFn = resolveComponent(menu.component)
      if (componentFn) {
        router.addRoute('Layout', {
          path: routePath,
          name: `Menu${menu.id}`,
          component: componentFn,
          meta: { title: menu.menuName, permission: menu.perms || null }
        })
      }
    }
    if (menu.children?.length) {
      addMenuRoutes(menu.children, fullPath)
    }
  }
}

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  const token = getToken()
  if (to.meta.public) {
    return token ? '/' : true
  }
  if (!token) {
    return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  }
  if (!userStore.profile) {
    try {
      await userStore.loadUserContext()
      registerRoutes(userStore.menus)
      const originalPath = to.redirectedFrom?.fullPath || to.fullPath
      if (originalPath === '/' || originalPath === '/dashboard') {
        const target = findDashboardChildPath(userStore.menus)
        if (target) return target
      }
      return originalPath
    } catch (error) {
      userStore.reset()
      return `/login?redirect=${encodeURIComponent(to.fullPath)}`
    }
  }
  if (to.path === '/' || to.path === '/dashboard') {
    const target = findDashboardChildPath(userStore.menus)
    if (target) return target
  }
  const permission = to.meta.permission
  if (permission && !userStore.permissions.includes(permission)) {
    ElMessage.warning('当前账号没有访问该页面的权限')
    return '/'
  }
  return true
})

function findDashboardChildPath(menus) {
  const dashboard = menus.find((m) => m.path === '/dashboard' && m.menuType === 0)
  if (!dashboard?.children?.length) return null
  for (const child of dashboard.children) {
    if (child.visible !== 0 && child.menuType !== 2) {
      return buildChildPath(dashboard, child)
    }
  }
  return null
}

function buildChildPath(parent, child) {
  if (!child.path) return parent.path
  if (child.path.startsWith('/')) return child.path
  return `${parent.path}/${child.path}`.replace(/\/+/g, '/')
}

export default router
