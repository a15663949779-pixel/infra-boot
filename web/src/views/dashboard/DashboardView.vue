<template>
  <div class="page">
    <div class="page-title">
      <div>
        <span>Dashboard</span>
        <h1>{{ greeting }}，{{ userStore.nickname }}</h1>
      </div>
      <el-button type="primary" :icon="Refresh" @click="refresh" round>
        刷新上下文
      </el-button>
    </div>

    <div class="overview-grid">
      <section class="stat-card stat-card--user">
        <div class="stat-icon">
          <el-icon :size="22"><User /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-label">当前用户</span>
          <strong class="stat-value">{{ userStore.profile?.username || '-' }}</strong>
          <p class="stat-desc">{{ userStore.profile?.nickname || '-' }}</p>
        </div>
      </section>

      <section class="stat-card stat-card--role">
        <div class="stat-icon">
          <el-icon :size="22"><Tickets /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-label">角色数量</span>
          <strong class="stat-value">{{ userStore.roles.length }}</strong>
          <p class="stat-desc">{{ userStore.roles.join('、') || '暂无角色' }}</p>
        </div>
      </section>

      <section class="stat-card stat-card--perm">
        <div class="stat-icon">
          <el-icon :size="22"><Lock /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-label">权限数量</span>
          <strong class="stat-value">{{ userStore.permissions.length }}</strong>
          <p class="stat-desc">Spring Security Authority</p>
        </div>
      </section>

      <section class="stat-card stat-card--menu">
        <div class="stat-icon">
          <el-icon :size="22"><Menu /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-label">菜单数量</span>
          <strong class="stat-value">{{ menuCount }}</strong>
          <p class="stat-desc">后端菜单树</p>
        </div>
      </section>
    </div>

    <section class="content-section">
      <div class="section-head">
        <h2>已加载菜单</h2>
        <span>{{ menuCount }} 项</span>
      </div>
      <el-table :data="menuTree" row-key="id" :tree-props="{ children: 'children' }">
        <el-table-column prop="menuName" label="菜单名称" min-width="160" />
        <el-table-column prop="fullPath" label="前端路径" min-width="180" />
        <el-table-column prop="perms" label="权限标识" min-width="220" show-overflow-tooltip />
        <el-table-column prop="menuType" label="类型" width="100">
          <template #default="{ row }">
            <dict-tag :options="dicts.sys_menu_type" :value="row.menuType" />
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Refresh, User, Tickets, Lock, Menu } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { addFullPathToTree, countMenuItems } from '../../utils/menu'
import { useDict } from '../../hooks/useDict'
import DictTag from '../../components/DictTag/index.vue'

const dicts = useDict('sys_menu_type')
const userStore = useUserStore()
const menuTree = computed(() => addFullPathToTree(userStore.menus))
const menuCount = computed(() => countMenuItems(userStore.menus))

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
})

function refresh() {
  userStore.loadUserContext()
}
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.stat-icon {
  flex: 0 0 48px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(74, 121, 240, 0.08), rgba(128, 100, 223, 0.08));
  color: var(--brand);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.stat-card:hover .stat-icon {
  transform: scale(1.08) rotate(-4deg);
}

.stat-card--user .stat-icon {
  background: linear-gradient(135deg, rgba(74, 121, 240, 0.12), rgba(74, 121, 240, 0.04));
  color: #4a79f0;
}

.stat-card--role .stat-icon {
  background: linear-gradient(135deg, rgba(0, 168, 112, 0.12), rgba(0, 168, 112, 0.04));
  color: #00a870;
}

.stat-card--perm .stat-icon {
  background: linear-gradient(135deg, rgba(217, 130, 43, 0.12), rgba(217, 130, 43, 0.04));
  color: #d9822b;
}

.stat-card--menu .stat-icon {
  background: linear-gradient(135deg, rgba(128, 100, 223, 0.12), rgba(128, 100, 223, 0.04));
  color: #8064df;
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-label {
  display: block;
  font-size: 13px;
  color: var(--muted);
  font-weight: 500;
  margin-bottom: 4px;
}

.stat-value {
  display: block;
  margin: 0;
  font-size: 28px;
  line-height: 1.2;
  background: var(--grad-text);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.stat-desc {
  display: block;
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--muted);
  opacity: 0.8;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
