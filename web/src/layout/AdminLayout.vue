<template>
  <el-container class="admin-shell">
    <button v-if="isCompact && !collapsed" class="aside-backdrop" aria-label="收起导航" @click="collapsed = true"></button>
    <el-aside class="admin-aside" :class="{ 'is-collapsed': collapsed }" :width="collapsed ? '80px' : '252px'">
      <div class="module-head" :class="{ 'is-collapsed': collapsed }">
        <div class="nav-brand-mark" aria-hidden="true">
          <svg viewBox="0 0 32 32" fill="none">
            <path d="M7 7h7v18H7zM18 7h7v7h-7zM18 18h7v7h-7z" fill="currentColor" />
          </svg>
        </div>
        <div class="brand-text">
          <strong>InfraBoot<span class="brand-period">.</span></strong>
          <span>管理控制台</span>
        </div>
      </div>
      <div class="aside-section-label" :class="{ 'is-collapsed': collapsed }">
        <span>{{ activeTopMenu?.menuName || '工作空间' }}</span>
        <span class="aside-section-count">{{ activeSideMenus.length }}</span>
      </div>
      <el-scrollbar class="aside-scroll">
        <el-menu
          :collapse="collapsed"
          :default-active="activeMenu"
          class="side-menu"
          router
          unique-opened
          @select="onSideSelect"
        >
          <template v-for="menu in activeSideMenus" :key="menu.id">
            <MenuItem :menu="menu" :parent-path="activeTopMenu?.fullPath || ''" />
          </template>
        </el-menu>
      </el-scrollbar>
      <button class="aside-profile" :title="collapsed ? '个人资料' : undefined" @click="handleCommand('profile')">
        <el-icon class="aside-profile-icon"><User /></el-icon>
        <span class="aside-profile-copy"><strong>个人资料</strong><small>{{ userStore.nickname }}</small></span>
        <el-icon class="aside-profile-arrow"><ArrowRight /></el-icon>
      </button>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-top">
          <div class="header-left">
            <el-button class="icon-button nav-collapse" text :aria-label="collapsed ? '展开导航' : '收起导航'" :aria-expanded="!collapsed" @click="collapsed = !collapsed">
              <el-icon :size="20">
                <Fold v-if="!collapsed" />
                <Expand v-else />
              </el-icon>
            </el-button>
            <span class="header-divider" aria-hidden="true"></span>
            <div class="top-menu-wrap">
              <el-menu
                v-if="!isCompact"
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
              <el-dropdown v-else class="compact-module-menu" @command="handleTopSelect">
                <button class="compact-module-trigger" aria-label="切换导航模块">
                  <span>{{ activeTopMenu?.menuName || '工作空间' }}</span>
                  <el-icon><ArrowDown /></el-icon>
                </button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-for="menu in topMenus" :key="menu.topKey" :command="menu.topKey">
                      {{ menu.menuName }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
          <div class="header-right">
            <el-button
              class="icon-button theme-toggle"
              text
              :title="isDark ? '切换到日间模式' : '切换到黑暗模式'"
              :aria-label="isDark ? '切换到日间模式' : '切换到黑暗模式'"
              @click="toggleTheme"
            >
              <el-icon :size="18">
                <Sunny v-if="isDark" />
                <Moon v-else />
              </el-icon>
            </el-button>
            <el-dropdown popper-class="nav-user-dropdown" @command="handleCommand">
              <button class="user-entry" aria-label="打开账号菜单">
                <el-avatar v-if="userStore.avatar" :size="34" :src="userStore.avatar" />
                <span v-else class="user-letter">{{ userStore.nickname.slice(0, 1).toUpperCase() }}</span>
                <span class="user-info"><strong>{{ userStore.nickname }}</strong><small>个人账号</small></span>
                <el-icon class="user-chevron"><ArrowDown /></el-icon>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        <TagsView />
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowDown,
  ArrowRight,
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
import TagsView from './TagsView.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const compactQuery = window.matchMedia('(max-width: 760px)')
const isCompact = ref(compactQuery.matches)
const collapsed = ref(compactQuery.matches)
const activeMenu = computed(() => route.path)

function onViewportChange(event) {
  isCompact.value = event.matches
  if (event.matches) collapsed.value = true
}
onMounted(() => compactQuery.addEventListener('change', onViewportChange))
onUnmounted(() => compactQuery.removeEventListener('change', onViewportChange))

function onSideSelect() {
  if (isCompact.value) collapsed.value = true
}

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
  role: Tickets,
  'data-board': DataBoard
}

const topMenus = computed(() =>
  userStore.menus
    .filter((menu) => isVisibleMenu(menu))
    .map((menu) => decorateTopMenu(menu))
)

const activeTopMenu = computed(() => {
  const matched = topMenus.value.find((menu) => isRouteInMenu(route.path, menu))
  return matched || topMenus.value[0]
})

const activeTopKey = computed(() => activeTopMenu.value?.topKey)
const activeSideMenus = computed(() => {
  const menu = activeTopMenu.value
  return menu?.children?.filter((item) => isVisibleMenu(item)) || []
})

async function handleCommand(command) {
  if (isCompact.value) collapsed.value = true
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
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
