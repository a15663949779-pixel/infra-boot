<template>
  <el-container class="admin-shell">
    <el-aside class="admin-aside" :width="collapsed ? '72px' : '240px'">
      <div class="module-head" :class="{ 'is-collapsed': collapsed }">
        <span class="module-kicker">当前模块</span>
        <strong>{{ activeTopMenu?.menuName || '工作台' }}</strong>
      </div>
      <el-scrollbar class="aside-scroll">
        <el-menu
          :collapse="collapsed"
          :default-active="activeMenu"
          class="side-menu"
          router
          unique-opened
        >
          <template v-if="activeSideMenus.length">
            <template v-for="menu in activeSideMenus" :key="menu.id">
              <MenuItem :menu="menu" :parent-path="activeTopMenu?.fullPath || ''" />
            </template>
          </template>
          <el-menu-item v-else index="/dashboard" class="module-home-item">
            <el-icon><DataBoard /></el-icon>
            <template #title>工作台</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <div class="brand">
            <div class="brand-mark">V</div>
            <div class="brand-text">
              <strong>Vibe Admin</strong>
              <span>后台管理系统</span>
            </div>
          </div>
          <el-button class="icon-button" text @click="collapsed = !collapsed">
            <el-icon :size="20">
              <Fold v-if="!collapsed" />
              <Expand v-else />
            </el-icon>
          </el-button>
          <div class="top-menu-wrap">
            <el-menu
              :default-active="activeTopKey"
              class="top-menu"
              mode="horizontal"
              ellipsis
              @select="handleTopSelect"
            >
              <el-menu-item v-for="menu in topMenus" :key="menu.topKey" :index="menu.topKey">
                <el-icon>
                  <component :is="menu.iconComponent" />
                </el-icon>
                <template #title>{{ menu.menuName }}</template>
              </el-menu-item>
            </el-menu>
          </div>
        </div>
        <div class="header-right">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ activeTopMenu?.menuName || '首页' }}</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title || '工作台' }}</el-breadcrumb-item>
          </el-breadcrumb>
          <el-button
            class="icon-button theme-toggle"
            text
            :title="isDark ? '切换到日间模式' : '切换到黑暗模式'"
            @click="toggleTheme"
          >
            <el-icon :size="18">
              <Sunny v-if="isDark" />
              <Moon v-else />
            </el-icon>
          </el-button>
          <el-tag effect="plain" round>{{ userStore.roles.join(', ') || 'user' }}</el-tag>
          <el-dropdown @command="handleCommand">
            <button class="user-entry">
              <span>{{ userStore.nickname.slice(0, 1).toUpperCase() }}</span>
              <strong>{{ userStore.nickname }}</strong>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowDown,
  DataBoard,
  Expand,
  Fold,
  Grid,
  Menu as MenuIcon,
  Moon,
  Setting,
  Sunny,
  Tickets,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { normalizeMenuPath } from '../utils/menu'
import MenuItem from './MenuItem.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)
const activeMenu = computed(() => route.path)

const isDark = ref(localStorage.getItem('app-theme') === 'dark')
document.documentElement.classList.toggle('dark', isDark.value)

function toggleTheme() {
  isDark.value = !isDark.value
  document.documentElement.classList.toggle('dark', isDark.value)
  localStorage.setItem('app-theme', isDark.value ? 'dark' : 'light')
}
const iconMap = {
  setting: Setting,
  user: User,
  peoples: UserFilled,
  'tree-table': Grid,
  menu: MenuIcon,
  role: Tickets
}

const dashboardMenu = {
  id: 'dashboard',
  topKey: 'dashboard',
  menuName: '工作台',
  fullPath: '/dashboard',
  iconComponent: DataBoard,
  children: []
}

const topMenus = computed(() => [
  dashboardMenu,
  ...userStore.menus
    .filter((menu) => isVisibleMenu(menu))
    .map((menu) => decorateTopMenu(menu))
])

const activeTopMenu = computed(() => {
  const matched = topMenus.value.find((menu) => isRouteInMenu(route.path, menu))
  return matched || topMenus.value[0]
})

const activeTopKey = computed(() => activeTopMenu.value?.topKey || 'dashboard')
const activeSideMenus = computed(() => {
  const menu = activeTopMenu.value
  return menu?.children?.filter((item) => isVisibleMenu(item)) || []
})

async function handleCommand(command) {
  if (command === 'logout') {
    await userStore.logoutCurrent()
    router.replace('/login')
  }
}

function handleTopSelect(topKey) {
  const menu = topMenus.value.find((item) => item.topKey === topKey)
  const targetPath = getFirstRoutePath(menu)
  if (targetPath && targetPath !== route.path) {
    router.push(targetPath)
  }
}

function decorateTopMenu(menu) {
  const fullPath = normalizeMenuPath('', menu)
  return {
    ...menu,
    topKey: String(menu.id ?? fullPath),
    fullPath,
    iconComponent: iconMap[menu.icon] || MenuIcon
  }
}

function isVisibleMenu(menu) {
  return menu?.visible !== 0 && menu?.menuType !== 2
}

function isRouteInMenu(path, menu) {
  if (!menu) {
    return false
  }
  if (path === menu.fullPath) {
    return true
  }
  return menu.children?.some((child) => isRouteInChild(path, child, menu.fullPath)) || false
}

function isRouteInChild(path, menu, parentPath) {
  if (!isVisibleMenu(menu)) {
    return false
  }
  const fullPath = normalizeMenuPath(parentPath, menu)
  if (path === fullPath) {
    return true
  }
  return menu.children?.some((child) => isRouteInChild(path, child, fullPath)) || false
}

function getFirstRoutePath(menu) {
  if (!menu) {
    return '/dashboard'
  }
  if (!menu.children?.some((child) => isVisibleMenu(child))) {
    return menu.fullPath
  }
  return findFirstLeafPath(menu.children, menu.fullPath) || menu.fullPath
}

function findFirstLeafPath(menus, parentPath) {
  for (const menu of menus || []) {
    if (!isVisibleMenu(menu)) {
      continue
    }
    const fullPath = normalizeMenuPath(parentPath, menu)
    if (!menu.children?.some((child) => isVisibleMenu(child))) {
      return fullPath
    }
    const childPath = findFirstLeafPath(menu.children, fullPath)
    if (childPath) {
      return childPath
    }
  }
  return ''
}
</script>
