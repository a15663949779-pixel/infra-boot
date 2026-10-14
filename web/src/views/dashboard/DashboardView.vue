<template>
  <div class="page dashboard-page">
    <!-- 顶部极光控制台横幅 -->
    <header class="hero-banner">
      <div class="hero-glow hero-glow--primary"></div>
      <div class="hero-glow hero-glow--accent"></div>
      <div class="hero-grid-pattern"></div>

      <div class="hero-content">
        <div class="hero-user-area">
          <div class="user-avatar">
            {{ userInitial }}
            <span class="avatar-status"></span>
          </div>

          <div class="hero-text">
            <div class="hero-badges">
              <span class="badge-console">
                <span class="pulse-dot"></span>
                SYSTEM CONSOLE
              </span>
              <span class="badge-date">{{ currentDate }}</span>
            </div>

            <h1 class="hero-title">
              {{ greeting }}，
              <span class="hero-nickname">{{ userStore.nickname || userStore.profile?.username || '系统用户' }}</span>
            </h1>

            <p class="hero-subtitle">
              系统安全上下文已就绪，当前账户拥有 <strong>{{ userStore.roles.length }}</strong> 个角色与 <strong>{{ userStore.permissions.length }}</strong> 项受控权限。
            </p>
          </div>
        </div>

        <div class="hero-actions">
          <el-button
            class="refresh-btn"
            type="primary"
            :icon="Refresh"
            :loading="refreshing"
            round
            @click="refresh"
          >
            {{ refreshing ? '正在同步上下文...' : '刷新上下文' }}
          </el-button>
        </div>
      </div>
    </header>

    <!-- Bento Grid 4 维核心指标卡片 -->
    <div class="overview-grid">
      <!-- 1. 当前用户 -->
      <section class="stat-card stat-card--user">
        <div class="stat-card__glow"></div>
        <el-icon class="stat-watermark"><User /></el-icon>

        <div class="stat-header">
          <div class="stat-icon">
            <el-icon :size="22"><User /></el-icon>
          </div>
          <span class="stat-tag">IDENTITY</span>
        </div>

        <div class="stat-body">
          <span class="stat-label">当前用户</span>
          <strong class="stat-value" :title="userStore.profile?.username">
            {{ userStore.profile?.username || '-' }}
          </strong>
        </div>

        <div class="stat-footer">
          <span class="status-dot"></span>
          <p class="stat-desc" :title="userStore.profile?.nickname">
            昵称：{{ userStore.profile?.nickname || '-' }}
          </p>
        </div>
      </section>

      <!-- 2. 角色数量 -->
      <section class="stat-card stat-card--role">
        <div class="stat-card__glow"></div>
        <el-icon class="stat-watermark"><Tickets /></el-icon>

        <div class="stat-header">
          <div class="stat-icon">
            <el-icon :size="22"><Tickets /></el-icon>
          </div>
          <span class="stat-tag">ROLES</span>
        </div>

        <div class="stat-body">
          <span class="stat-label">角色数量</span>
          <div class="stat-value-wrap">
            <strong class="stat-value">{{ userStore.roles.length }}</strong>
            <span class="stat-unit">个</span>
          </div>
        </div>

        <div class="stat-footer">
          <span class="status-dot"></span>
          <p class="stat-desc" :title="userStore.roles.join('、') || '暂无角色'">
            {{ userStore.roles.join('、') || '暂无角色' }}
          </p>
        </div>
      </section>

      <!-- 3. 权限数量 -->
      <section class="stat-card stat-card--perm">
        <div class="stat-card__glow"></div>
        <el-icon class="stat-watermark"><Lock /></el-icon>

        <div class="stat-header">
          <div class="stat-icon">
            <el-icon :size="22"><Lock /></el-icon>
          </div>
          <span class="stat-tag">AUTHORITIES</span>
        </div>

        <div class="stat-body">
          <span class="stat-label">权限数量</span>
          <div class="stat-value-wrap">
            <strong class="stat-value">{{ userStore.permissions.length }}</strong>
            <span class="stat-unit">项</span>
          </div>
        </div>

        <div class="stat-footer">
          <span class="status-dot"></span>
          <p class="stat-desc">Spring Security Authority</p>
        </div>
      </section>

      <!-- 4. 菜单数量 -->
      <section class="stat-card stat-card--menu">
        <div class="stat-card__glow"></div>
        <el-icon class="stat-watermark"><Menu /></el-icon>

        <div class="stat-header">
          <div class="stat-icon">
            <el-icon :size="22"><Menu /></el-icon>
          </div>
          <span class="stat-tag">MENU TREE</span>
        </div>

        <div class="stat-body">
          <span class="stat-label">菜单数量</span>
          <div class="stat-value-wrap">
            <strong class="stat-value">{{ menuCount }}</strong>
            <span class="stat-unit">项</span>
          </div>
        </div>

        <div class="stat-footer">
          <span class="status-dot"></span>
          <p class="stat-desc">后端菜单树</p>
        </div>
      </section>
    </div>

    <!-- 已加载菜单树数据面板 -->
    <section class="content-section">
      <div class="section-head">
        <div class="section-title-group">
          <div class="title-indicator"></div>
          <div>
            <div class="title-row">
              <h2>已加载菜单</h2>
              <span class="count-badge">{{ menuCount }} 项</span>
            </div>
            <p class="section-desc">当前用户会话已挂载的后端动态路由树与权限映射表</p>
          </div>
        </div>

        <div class="section-toolbar">
          <el-input
            v-model="searchQuery"
            placeholder="过滤菜单名称 / 路径 / 权限标识..."
            :prefix-icon="Search"
            clearable
            class="search-input"
          />
          <el-button class="action-btn" @click="toggleExpandAll">
            {{ isExpandAll ? '折叠全部' : '展开全部' }}
          </el-button>
        </div>
      </div>

      <div class="table-wrapper">
        <el-table
          v-if="renderTable"
          :data="filteredMenuTree"
          row-key="id"
          :default-expand-all="isExpandAll"
          :tree-props="{ children: 'children' }"
          class="custom-table"
        >
          <el-table-column prop="menuName" label="菜单名称" min-width="200">
            <template #default="{ row }">
              <div class="menu-cell">
                <span
                  class="tree-dot"
                  :class="row.children && row.children.length ? 'tree-dot--branch' : 'tree-dot--leaf'"
                ></span>
                <span class="menu-name-text">{{ row.menuName }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="fullPath" label="前端路径" min-width="200">
            <template #default="{ row }">
              <span
                v-if="row.fullPath"
                class="mono-chip mono-chip--path"
                title="点击复制前端路径"
                @click="copyValue(row.fullPath, '前端路径')"
              >
                {{ row.fullPath }}
              </span>
              <span v-else class="cell-empty">—</span>
            </template>
          </el-table-column>

          <el-table-column prop="perms" label="权限标识" min-width="240" show-overflow-tooltip>
            <template #default="{ row }">
              <span
                v-if="row.perms"
                class="mono-chip mono-chip--perm"
                title="点击复制权限标识"
                @click="copyValue(row.perms, '权限标识')"
              >
                {{ row.perms }}
              </span>
              <span v-else class="cell-empty">—</span>
            </template>
          </el-table-column>

          <el-table-column prop="menuType" label="类型" width="120" align="center">
            <template #default="{ row }">
              <dict-tag :options="dicts.sys_menu_type" :value="row.menuType" />
            </template>
          </el-table-column>

          <template #empty>
            <div class="table-empty">
              <p>未检索到匹配的菜单项</p>
            </div>
          </template>
        </el-table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { Refresh, User, Tickets, Lock, Menu, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { addFullPathToTree, countMenuItems } from '../../utils/menu'
import { useDict } from '../../hooks/useDict'
import DictTag from '../../components/DictTag/index.vue'

const dicts = useDict('sys_menu_type')
const userStore = useUserStore()

const refreshing = ref(false)
const searchQuery = ref('')
const isExpandAll = ref(true)
const renderTable = ref(true)

const menuTree = computed(() => addFullPathToTree(userStore.menus))
const menuCount = computed(() => countMenuItems(userStore.menus))

// 提取用户昵称或用户名首字符用于头像展示
const userInitial = computed(() => {
  const name = userStore.nickname || userStore.profile?.nickname || userStore.profile?.username || 'U'
  return name.trim().charAt(0).toUpperCase()
})

// 递归过滤菜单树
const filteredMenuTree = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  if (!keyword) return menuTree.value

  const filterTree = (nodes) => {
    if (!Array.isArray(nodes)) return []
    return nodes.reduce((acc, node) => {
      const isMatch =
        (node.menuName && node.menuName.toLowerCase().includes(keyword)) ||
        (node.fullPath && node.fullPath.toLowerCase().includes(keyword)) ||
        (node.perms && node.perms.toLowerCase().includes(keyword))

      const matchedChildren = node.children ? filterTree(node.children) : []

      if (isMatch || matchedChildren.length > 0) {
        acc.push({
          ...node,
          children: matchedChildren.length > 0 ? matchedChildren : node.children
        })
      }
      return acc
    }, [])
  }

  return filterTree(menuTree.value)
})

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

const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

async function refresh() {
  if (refreshing.value) return
  refreshing.value = true
  try {
    await userStore.loadUserContext()
    ElMessage.success('系统上下文已刷新')
  } finally {
    setTimeout(() => {
      refreshing.value = false
    }, 300)
  }
}

async function toggleExpandAll() {
  renderTable.value = false
  isExpandAll.value = !isExpandAll.value
  await nextTick()
  renderTable.value = true
}

async function copyValue(val, label) {
  if (!val) return
  try {
    await navigator.clipboard.writeText(val)
    ElMessage.success(`${label}已复制：${val}`)
  } catch {
    ElMessage.info(val)
  }
}
</script>

<style scoped>
/* ==================== 全局设计变量与布局容器 ==================== */
.dashboard-page {
  --surface-bg: #ffffff;
  --text-primary: #0f172a;
  --text-secondary: #475569;
  --text-muted: #94a3b8;
  --border-color: #e2e8f0;
  --radius-xl: 20px;
  --radius-lg: 16px;

  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 24px;
  color: var(--text-primary);
}

/* ==================== 顶部极光横幅 (Hero Banner) ==================== */
.hero-banner {
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-xl);
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%);
  padding: 30px 36px;
  color: #ffffff;
  box-shadow: 0 20px 40px -15px rgba(15, 23, 42, 0.25),
              inset 0 1px 0 0 rgba(255, 255, 255, 0.1);
}

/* 科技感微网格纹理 */
.hero-grid-pattern {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(255, 255, 255, 0.08) 1px, transparent 1px);
  background-size: 24px 24px;
  pointer-events: none;
  z-index: 0;
}

.hero-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(65px);
  pointer-events: none;
  z-index: 0;
}

.hero-glow--primary {
  width: 340px;
  height: 340px;
  background: rgba(59, 130, 246, 0.38);
  top: -160px;
  right: 10%;
}

.hero-glow--accent {
  width: 280px;
  height: 280px;
  background: rgba(139, 92, 246, 0.32);
  bottom: -140px;
  left: 18%;
}

.hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
}

.hero-user-area {
  display: flex;
  align-items: center;
  gap: 22px;
}

.user-avatar {
  position: relative;
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 800;
  color: #ffffff;
  flex-shrink: 0;
  box-shadow: 0 12px 24px -6px rgba(59, 130, 246, 0.5),
              inset 0 1px 1px rgba(255, 255, 255, 0.35);
}

.avatar-status {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #10b981;
  border: 3px solid #0f172a;
}

.hero-badges {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.badge-console {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 3px 11px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(8px);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.8px;
  color: #dbeafe;
}

.pulse-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #38bdf8;
  box-shadow: 0 0 0 0 rgba(56, 189, 248, 0.7);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(56, 189, 248, 0.7);
  }
  70% {
    box-shadow: 0 0 0 8px rgba(56, 189, 248, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(56, 189, 248, 0);
  }
}

.badge-date {
  font-size: 12px;
  color: rgba(226, 232, 240, 0.75);
  font-weight: 500;
}

.hero-title {
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.5px;
  line-height: 1.25;
  color: #ffffff;
}

.hero-nickname {
  background: linear-gradient(90deg, #93c5fd 0%, #c4b5fd 60%, #6ee7b7 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero-subtitle {
  margin: 0;
  font-size: 13.5px;
  color: rgba(203, 213, 225, 0.85);
}

.hero-subtitle strong {
  color: #ffffff;
  font-weight: 700;
}

.refresh-btn {
  height: 42px;
  padding: 0 22px;
  font-weight: 600;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.24);
  backdrop-filter: blur(12px);
  color: #ffffff;
  transition: all 0.25s ease;
}

.refresh-btn:hover {
  background: rgba(255, 255, 255, 0.22);
  border-color: rgba(255, 255, 255, 0.45);
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -5px rgba(0, 0, 0, 0.35);
}

/* ==================== Bento 统计卡片网格 ==================== */
.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.stat-card {
  --theme-color: #4a79f0;
  --theme-soft: rgba(74, 121, 240, 0.1);
  --theme-border: rgba(74, 121, 240, 0.38);

  position: relative;
  overflow: hidden;
  background: var(--surface-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 22px 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 16px;
  box-shadow: 0 4px 20px -4px rgba(15, 23, 42, 0.04);
  transition: all 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}

.stat-card--user {
  --theme-color: #3b82f6;
  --theme-soft: rgba(59, 130, 246, 0.1);
  --theme-border: rgba(59, 130, 246, 0.4);
}

.stat-card--role {
  --theme-color: #00a870;
  --theme-soft: rgba(0, 168, 112, 0.1);
  --theme-border: rgba(0, 168, 112, 0.4);
}

.stat-card--perm {
  --theme-color: #d9822b;
  --theme-soft: rgba(217, 130, 43, 0.1);
  --theme-border: rgba(217, 130, 43, 0.4);
}

.stat-card--menu {
  --theme-color: #8064df;
  --theme-soft: rgba(128, 100, 223, 0.1);
  --theme-border: rgba(128, 100, 223, 0.4);
}

/* 顶部主题色装饰条 */
.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--theme-color);
  opacity: 0.75;
  transition: all 0.3s ease;
}

.stat-card__glow {
  position: absolute;
  top: -40px;
  right: -40px;
  width: 130px;
  height: 130px;
  border-radius: 50%;
  background: var(--theme-soft);
  filter: blur(28px);
  pointer-events: none;
}

.stat-watermark {
  position: absolute;
  right: -12px;
  bottom: -14px;
  font-size: 96px;
  color: var(--theme-color);
  opacity: 0.04;
  transform: rotate(-15deg);
  pointer-events: none;
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

.stat-card:hover {
  transform: translateY(-5px);
  border-color: var(--theme-border);
  box-shadow: 0 18px 35px -10px rgba(15, 23, 42, 0.09);
}

.stat-card:hover::before {
  height: 4px;
  opacity: 1;
}

.stat-card:hover .stat-watermark {
  transform: rotate(-5deg) scale(1.1);
  opacity: 0.08;
}

.stat-card:hover .stat-icon {
  transform: scale(1.08) rotate(-4deg);
  background: var(--theme-color);
  color: #ffffff;
  box-shadow: 0 8px 16px -4px var(--theme-border);
}

.stat-header {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--theme-soft);
  color: var(--theme-color);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.stat-tag {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.6px;
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--theme-soft);
  color: var(--theme-color);
}

.stat-body {
  position: relative;
  z-index: 1;
}

.stat-label {
  display: block;
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
  margin-bottom: 6px;
}

.stat-value-wrap {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.stat-value {
  display: block;
  margin: 0;
  font-size: 28px;
  line-height: 1.2;
  color: var(--text-primary);
  font-weight: 800;
  letter-spacing: -0.5px;
  font-variant-numeric: tabular-nums;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stat-unit {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
}

.stat-footer {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px dashed #f1f5f9;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--theme-color);
  flex-shrink: 0;
}

.stat-desc {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ==================== 底部已加载菜单面板 ==================== */
.content-section {
  background: var(--surface-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  padding: 24px 28px;
  box-shadow: 0 4px 24px -4px rgba(15, 23, 42, 0.04);
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 22px;
  flex-wrap: wrap;
}

.section-title-group {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.title-indicator {
  width: 5px;
  height: 40px;
  border-radius: 999px;
  background: linear-gradient(180deg, #3b82f6 0%, #8064df 100%);
  flex-shrink: 0;
  margin-top: 2px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.title-row h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.count-badge {
  font-size: 12px;
  font-weight: 600;
  color: #3b82f6;
  background: rgba(59, 130, 246, 0.08);
  border: 1px solid rgba(59, 130, 246, 0.2);
  padding: 2px 10px;
  border-radius: 999px;
}

.section-desc {
  margin: 0;
  font-size: 13px;
  color: var(--text-muted);
}

.section-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.search-input {
  width: 260px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px var(--border-color) inset;
  background: #f8fafc;
  transition: all 0.2s ease;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  box-shadow: 0 0 0 1px #3b82f6 inset, 0 0 0 3px rgba(59, 130, 246, 0.12);
}

.action-btn {
  border-radius: 10px;
  font-weight: 500;
  color: var(--text-secondary);
  border-color: var(--border-color);
}

.action-btn:hover {
  color: #3b82f6;
  border-color: rgba(59, 130, 246, 0.35);
  background: rgba(59, 130, 246, 0.04);
}

/* ==================== Element Plus 表格深度定制 ==================== */
.table-wrapper {
  border: 1px solid #f1f5f9;
  border-radius: 14px;
  overflow: hidden;
}

.custom-table {
  --el-table-border-color: #f1f5f9;
  --el-table-row-hover-bg-color: #f8fafc;
  --el-table-header-bg-color: #f8fafc;
  --el-table-header-text-color: #475569;
}

.custom-table :deep(th.el-table__cell) {
  font-weight: 600;
  font-size: 13px;
  letter-spacing: 0.3px;
  padding: 14px 0;
  background-color: #f8fafc !important;
  border-bottom: 1px solid #e2e8f0;
}

.custom-table :deep(td.el-table__cell) {
  padding: 14px 0;
  border-bottom: 1px solid #f1f5f9;
}

.menu-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.tree-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.tree-dot--branch {
  background: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
}

.tree-dot--leaf {
  background: #cbd5e1;
}

.menu-name-text {
  font-weight: 600;
  color: #1e293b;
}

.mono-chip {
  display: inline-block;
  font-family: 'JetBrains Mono', 'Fira Code', Consolas, monospace;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: all;
}

.mono-chip--path {
  color: #0369a1;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
}

.mono-chip--path:hover {
  background: #e0f2fe;
  border-color: #7dd3fc;
  transform: translateY(-1px);
}

.mono-chip--perm {
  color: #6d28d9;
  background: #f5f3ff;
  border: 1px solid #ddd6fe;
}

.mono-chip--perm:hover {
  background: #ede9fe;
  border-color: #c4b5fd;
  transform: translateY(-1px);
}

.cell-empty {
  color: #cbd5e1;
  font-size: 13px;
}

.table-empty {
  padding: 32px 0;
  color: var(--text-muted);
  font-size: 14px;
}

/* ==================== 响应式断点适配 ==================== */
@media (max-width: 1280px) {
  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .hero-banner {
    padding: 24px 20px;
  }

  .hero-user-area {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }

  .hero-title {
    font-size: 22px;
  }

  .overview-grid {
    grid-template-columns: 1fr;
  }

  .content-section {
    padding: 18px 16px;
  }

  .search-input {
    width: 100%;
  }
}
</style>

<style>
/* ==================== 管理看板 · 暗色模式适配 ==================== */
html.dark .dashboard-page {
  --surface-bg: #171d2f;
  --text-primary: #e5eaf6;
  --text-secondary: #93a0bb;
  --text-muted: #6b7a99;
  --border-color: #2a3452;
}

html.dark .dashboard-page .stat-card {
  box-shadow: 0 4px 20px -4px rgba(0, 0, 0, 0.3);
}

html.dark .dashboard-page .stat-card:hover {
  box-shadow: 0 18px 35px -10px rgba(0, 0, 0, 0.4);
}

html.dark .dashboard-page .stat-footer {
  border-top-color: #2a3452;
}

html.dark .dashboard-page .content-section {
  box-shadow: 0 4px 24px -4px rgba(0, 0, 0, 0.3);
}

html.dark .dashboard-page .search-input :deep(.el-input__wrapper) {
  background: #1c2338;
}

html.dark .dashboard-page .search-input :deep(.el-input__wrapper.is-focus) {
  background: #232c46;
}

html.dark .dashboard-page .action-btn {
  color: #93a0bb;
}

html.dark .dashboard-page .action-btn:hover {
  color: #7ea2ff;
  background: rgba(74, 121, 240, 0.12);
}

html.dark .dashboard-page .table-wrapper {
  border-color: #2a3452;
}

html.dark .dashboard-page .custom-table {
  --el-table-border-color: #2a3452;
  --el-table-row-hover-bg-color: rgba(74, 121, 240, 0.08);
  --el-table-header-bg-color: #1c2338;
  --el-table-header-text-color: #93a0bb;
}

html.dark .dashboard-page .custom-table :deep(th.el-table__cell) {
  background-color: #1c2338 !important;
  border-bottom-color: #2a3452;
}

html.dark .dashboard-page .custom-table :deep(td.el-table__cell) {
  border-bottom-color: #1e2639;
}

html.dark .dashboard-page .tree-dot--leaf {
  background: #4a5568;
}

html.dark .dashboard-page .menu-name-text {
  color: #e5eaf6;
}

html.dark .dashboard-page .mono-chip--path {
  color: #7dd3fc;
  background: rgba(56, 189, 248, 0.1);
  border-color: rgba(56, 189, 248, 0.25);
}

html.dark .dashboard-page .mono-chip--path:hover {
  background: rgba(56, 189, 248, 0.18);
}

html.dark .dashboard-page .mono-chip--perm {
  color: #c4b5fd;
  background: rgba(139, 92, 246, 0.1);
  border-color: rgba(139, 92, 246, 0.25);
}

html.dark .dashboard-page .mono-chip--perm:hover {
  background: rgba(139, 92, 246, 0.18);
}

html.dark .dashboard-page .cell-empty {
  color: #4a5568;
}

html.dark .dashboard-page .count-badge {
  color: #7ea2ff;
  background: rgba(74, 121, 240, 0.15);
  border-color: rgba(74, 121, 240, 0.3);
}

html.dark .dashboard-page .avatar-status {
  border-color: #171d2f;
}
</style>