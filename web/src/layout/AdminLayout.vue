<template>
  <el-container class="admin-shell">
    <el-aside class="admin-aside" :width="collapsed ? '72px' : '240px'">
      <div class="brand">
        <div class="brand-mark">V</div>
        <div v-show="!collapsed" class="brand-text">
          <strong>Vibe Admin</strong>
          <span>后台管理系统</span>
        </div>
      </div>
      <el-scrollbar class="aside-scroll">
        <el-menu
          :collapse="collapsed"
          :default-active="activeMenu"
          class="side-menu"
          router
          unique-opened
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataBoard /></el-icon>
            <template #title>工作台</template>
          </el-menu-item>
          <template v-for="menu in menus" :key="menu.id">
            <MenuItem :menu="menu" />
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-button class="icon-button" text @click="collapsed = !collapsed">
            <el-icon :size="20">
              <Fold v-if="!collapsed" />
              <Expand v-else />
            </el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title || '工作台' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
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
import { ArrowDown, DataBoard, Expand, Fold } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import MenuItem from './MenuItem.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)
const activeMenu = computed(() => route.path)
const menus = computed(() => userStore.menus)

async function handleCommand(command) {
  if (command === 'logout') {
    await userStore.logoutCurrent()
    router.replace('/login')
  }
}
</script>
