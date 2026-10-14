<template>
  <div class="dashboard-page">
    <!-- 顶部极光欢迎横幅 -->
    <header class="hero-banner">
      <div class="hero-glow hero-glow--1"></div>
      <div class="hero-glow hero-glow--2"></div>
      
      <div class="hero-content">
        <div class="hero-left">
          <div class="hero-badges">
            <span class="badge-live">
              <span class="pulse-dot"></span>
              WORKBENCH ONLINE
            </span>
            <span class="badge-date">{{ currentDate }}</span>
          </div>
          <h1 class="hero-title">
            {{ greeting }}，
            <span class="hero-nickname">{{ userStore.nickname || userStore.profile?.username || '管理员' }}</span>
          </h1>
          <p class="hero-subtitle">
            欢迎回到运营控制台，当前系统权限上下文与动态路由已就绪。
          </p>
        </div>

        <div class="hero-actions">
          <el-button
            class="refresh-btn"
            type="primary"
            :icon="Refresh"
            :loading="refreshing"
            @click="refresh"
          >
            {{ refreshing ? '同步中...' : '刷新上下文' }}
          </el-button>
        </div>
      </div>
    </header>

    <!-- Bento Grid 核心指标卡片 -->
    <div class="overview-grid">
      <!-- 用户卡片 -->
      <section class="stat-card stat-card--user">
        <div class="stat-card__glow"></div>
        <el-icon class="stat-watermark"><User /></el-icon>
        
        <div class="stat-header">
          <div class="stat-icon">
            <el-icon :size="22"><User /></el-icon>
          </div>
          <span class="stat-tag">ACCOUNT</span>
        </div>

        <div class="stat-body">
          <span class="stat-label">当前登录账户</span>
          <strong class="stat-value">{{ userStore.profile?.username || '-' }}</strong>
        </div>

        <div class="stat-footer">
          <span class="status-indicator"></span>
          <span class="stat-desc">昵称：{{ userStore.profile?.nickname || '未设置' }}</span>
        </div>
      </section>

      <!-- 角色卡片 -->
      <section class="stat-card stat-card--role">
        <div class="stat-card__glow"></div>
        <el-icon class="stat-watermark"><DataLine /></el-icon>

        <div class="stat-header">
          <div class="stat-icon">
            <el-icon :size="22"><DataLine /></el-icon>
          </div>
          <span class="stat-tag">RBAC ROLE</span>
        </div>

        <div class="stat-body">
          <span class="stat-label">所属角色组</span>
          <strong class="stat-value" :title="userStore.roles.join('、')">
            {{ userStore.roles.join('、') || '-' }}
          </strong>
        </div>

        <div class="stat-footer">
          <span class="status-indicator"></span>
          <span class="stat-desc">运营看板专属视图</span>
        </div>
      </section>

      <!-- 权限卡片 -->
      <section class="stat-card stat-card--perm">
        <div class="stat-card__glow"></div>
        <el-icon class="stat-watermark"><Lock /></el-icon>

        <div class="stat-header">
          <div class="stat-icon">
            <el-icon :size="22"><Lock /></el-icon>
          </div>
          <span class="stat-tag">SECURITY</span>
        </div>

        <div class="stat-body">
          <span class="stat-label">授权策略数量</span>
          <div class="stat-value-group">
            <strong class="stat-value">{{ userStore.permissions.length }}</strong>
            <span class="stat-unit">项</span>
          </div>
        </div>

        <div class="stat-footer">
          <span class="status-indicator"></span>
          <span class="stat-desc">Spring Security Authority</span>
        </div>
      </section>

      <!-- 菜单卡片 -->
      <section class="stat-card stat-card--menu">
        <div class="stat-card__glow"></div>
        <el-icon class="stat-watermark"><Menu /></el-icon>

        <div class="stat-header">
          <div class="stat-icon">
            <el-icon :size="22"><Menu /></el-icon>
          </div>
          <span class="stat-tag">ROUTES</span>
        </div>

        <div class="stat-body">
          <span class="stat-label">可访问菜单节点</span>
          <div class="stat-value-group">
            <strong class="stat-value">{{ menuCount }}</strong>
            <span class="stat-unit">个</span>
          </div>
        </div>

        <div class="stat-footer">
          <span class="status-indicator"></span>
          <span class="stat-desc">已挂载动态路由树</span>
        </div>
      </section>
    </div>

    <!-- 底部数据面板 -->
    <section class="content-section">
      <div class="section-head">
        <div class="section-title-group">
          <div class="title-accent"></div>
          <div>
            <div class="title-row">
              <h2>运营权限与路由概览</h2>
              <span class="count-pill">共 {{ menuCount }} 项节点</span>
            </div>
            <p class="section-subtitle">展示当前角色下已授权的菜单树结构、前端路由地址与后端权限标识符</p>
          </div>
        </div>

        <div class="section-toolbar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索菜单名称 / 路径 / 权限标识..."
            :prefix-icon="Search"
            clearable
            class="search-input"
          />
          <el-button class="tool-btn" @click="toggleExpandAll">
            {{ isExpandAll ? '折叠全部' : '展开全部' }}
          </el-button>
        </div>
      </div>

      <div class="table-container">
        <el-table
          v-if="renderTable"
          :data="filteredMenuTree"
          row-key="id"
          :default-expand-all="isExpandAll"
          :tree-props="{ children: 'children' }"
          class="modern-table"
        >
          <el-table-column prop="menuName" label="菜单名称" min-width="200">
            <template #default="{ row }">
              <div class="menu-name-cell">
                <span
                  class="node-dot"
                  :class="row.children && row.children.length ? 'node-dot--folder' : 'node-dot--leaf'"
                ></span>
                <span class="menu-title-text">{{ row.menuName }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="fullPath" label="前端路由路径" min-width="220">
            <template #default="{ row }">
              <span
                v-if="row.fullPath"
                class="code-badge code-badge--path"
                title="点击复制路径"
                @click="copyText(row.fullPath, '路由路径')"
              >
                {{ row.fullPath }}
              </span>
              <span v-else class="empty-placeholder">—</span>
            </template>
          </el-table-column>

          <el-table-column prop="perms" label="权限标识 (Authority)" min-width="240" show-overflow-tooltip>
            <template #default="{ row }">
              <span
                v-if="row.perms"
                class="code-badge code-badge--perm"
                title="点击复制权限标识"
                @click="copyText(row.perms, '权限标识')"
              >
                {{ row.perms }}
              </span>
              <span v-else class="empty-placeholder">—</span>
            </template>
          </el-table-column>

          <el-table-column prop="menuType" label="节点类型" width="130" align="center">
            <template #default="{ row }">
              <dict-tag :options="dicts.sys_menu_type" :value="row.menuType" />
            </template>
          </el-table-column>

          <template #empty>
            <div class="empty-state">
              <p>未找到匹配的菜单或权限节点</p>
            </div>
          </template>
        </el-table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { Refresh, User, DataLine, Lock, Menu, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { addFullPathToTree, countMenuItems } from '../../utils/menu'
import { useDict } from '../../hooks/useDict'
import DictTag from '../../components/DictTag/index.vue'

const dicts = useDict('sys_menu_type')
const userStore = useUserStore()

const refreshing = ref(false)
const searchKeyword = ref('')
const isExpandAll = ref(true)
const renderTable = ref(true)

const menuTree = computed(() => addFullPathToTree(userStore.menus))
const menuCount = computed(() => countMenuItems(userStore.menus))

// 递归过滤树节点（支持按菜单名、路径、权限标识搜索）
const filteredMenuTree = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()
  if (!kw) return menuTree.value

  const filterNodes = (nodes) => {
    if (!Array.isArray(nodes)) return []
    return nodes.reduce((acc, node) => {
      const matchSelf =
        (node.menuName && node.menuName.toLowerCase().includes(kw)) ||
        (node.fullPath && node.fullPath.toLowerCase().includes(kw)) ||
        (node.perms && node.perms.toLowerCase().includes(kw))

      const filteredChildren = node.children ? filterNodes(node.children) : []

      if (matchSelf || filteredChildren.length > 0) {
        acc.push({
          ...node,
          children: filteredChildren.length > 0 ? filteredChildren : node.children
        })
      }
      return acc
    }, [])
  }

  return filterNodes(menuTree.value)
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
  const now = new Date()
  return now.toLocaleDateString('zh-CN', {
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
    ElMessage.success('用户上下文与权限数据已更新')
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

async function copyText(text, label) {
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(`${label}已复制：${text}`)
  } catch {
    ElMessage.info(text)
  }
}
</script>

<style scoped>
/* ==================== 页面容器与基础变量 ==================== */
.dashboard-page {
  --bg-surface: #ffffff;
  --text-main: #0f172a;
  --text-secondary: #475569;
  --text-muted: #94a3b8;
  --border-subtle: #e2e8f0;
  --radius-xl: 20px;
  --radius-lg: 16px;
  --radius-md: 10px;

  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 24px;
  color: var(--text-main);
}

/* ==================== 顶部极光横幅 (Hero Banner) ==================== */
.hero-banner {
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-xl);
  background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 52%, #172554 100%);
  padding: 32px 36px;
  color: #ffffff;
  box-shadow: 0 20px 40px -15px rgba(15, 23, 42, 0.25),
              inset 0 1px 0 0 rgba(255, 255, 255, 0.12);
}

/* 极光装饰背景球 */
.hero-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  pointer-events: none;
  z-index: 0;
}

.hero-glow--1 {
  width: 320px;
  height: 320px;
  background: rgba(79, 70, 229, 0.45);
  top: -140px;
  right: 8%;
}

.hero-glow--2 {
  width: 260px;
  height: 260px;
  background: rgba(14, 165, 233, 0.3);
  bottom: -120px;
  left: 25%;
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

.hero-badges {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.badge-live {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(8px);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.8px;
  color: #e0e7ff;
}

.pulse-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 0 0 rgba(52, 211, 153, 0.7);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(52, 211, 153, 0.7);
  }
  70% {
    box-shadow: 0 0 0 8px rgba(52, 211, 153, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(52, 211, 153, 0);
  }
}

.badge-date {
  font-size: 12px;
  color: rgba(226, 232, 240, 0.75);
  font-weight: 500;
}

.hero-title {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.5px;
  line-height: 1.25;
  color: #ffffff;
}

.hero-nickname {
  background: linear-gradient(90deg, #93c5fd 0%, #c4b5fd 50%, #f9a8d4 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero-subtitle {
  margin: 0;
  font-size: 14px;
  color: rgba(203, 213, 225, 0.85);
}

.refresh-btn {
  height: 42px;
  padding: 0 22px;
  border-radius: 12px;
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
  border-color: rgba(255, 255, 255, 0.4);
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -5px rgba(0, 0, 0, 0.3);
}

/* ==================== Bento 统计卡片网格 ==================== */
.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.stat-card {
  --card-color: #4f46e5;
  --card-bg-soft: rgba(79, 70, 229, 0.08);
  --card-border-hover: rgba(79, 70, 229, 0.35);

  position: relative;
  overflow: hidden;
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: 22px 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 16px;
  box-shadow: 0 4px 20px -4px rgba(15, 23, 42, 0.04);
  transition: all 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}

/* 4 种卡片专属配色方案 */
.stat-card--user {
  --card-color: #3b82f6;
  --card-bg-soft: rgba(59, 130, 246, 0.1);
  --card-border-hover: rgba(59, 130, 246, 0.4);
}

.stat-card--role {
  --card-color: #10b981;
  --card-bg-soft: rgba(16, 185, 129, 0.1);
  --card-border-hover: rgba(16, 185, 129, 0.4);
}

.stat-card--perm {
  --card-color: #f59e0b;
  --card-bg-soft: rgba(245, 158, 11, 0.1);
  --card-border-hover: rgba(245, 158, 11, 0.4);
}

.stat-card--menu {
  --card-color: #8b5cf6;
  --card-bg-soft: rgba(139, 92, 246, 0.1);
  --card-border-hover: rgba(139, 92, 246, 0.4);
}

/* 顶部彩色细线装饰 */
.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--card-color);
  opacity: 0.75;
  transition: height 0.3s ease, opacity 0.3s ease;
}

.stat-card__glow {
  position: absolute;
  top: -40px;
  right: -40px;
  width: 130px;
  height: 130px;
  border-radius: 50%;
  background: var(--card-bg-soft);
  filter: blur(28px);
  pointer-events: none;
  transition: transform 0.4s ease;
}

/* 右下角半透明巨型水印图标 */
.stat-watermark {
  position: absolute;
  right: -12px;
  bottom: -14px;
  font-size: 96px;
  color: var(--card-color);
  opacity: 0.04;
  transform: rotate(-15deg);
  pointer-events: none;
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

.stat-card:hover {
  transform: translateY(-5px);
  border-color: var(--card-border-hover);
  box-shadow: 0 18px 35px -10px rgba(15, 23, 42, 0.09);
}

.stat-card:hover::before {
  opacity: 1;
  height: 4px;
}

.stat-card:hover .stat-watermark {
  transform: rotate(-5deg) scale(1.1);
  opacity: 0.08;
}

.stat-card:hover .stat-icon {
  transform: scale(1.08) rotate(-4deg);
  background: var(--card-color);
  color: #ffffff;
  box-shadow: 0 8px 16px -4px var(--card-border-hover);
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 1;
}

.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--card-bg-soft);
  color: var(--card-color);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.stat-tag {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.6px;
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--card-bg-soft);
  color: var(--card-color);
}

.stat-body {
  position: relative;
  z-index: 1;
}

.stat-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.stat-value-group {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.2;
  color: var(--text-main);
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

.status-indicator {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--card-color);
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

/* ==================== 底部表格面板 ==================== */
.content-section {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
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

.title-accent {
  width: 5px;
  height: 40px;
  border-radius: 999px;
  background: linear-gradient(180deg, #4f46e5 0%, #06b6d4 100%);
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
  color: var(--text-main);
}

.count-pill {
  font-size: 12px;
  font-weight: 600;
  color: #4f46e5;
  background: rgba(79, 70, 229, 0.08);
  border: 1px solid rgba(79, 70, 229, 0.18);
  padding: 2px 10px;
  border-radius: 999px;
}

.section-subtitle {
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
  box-shadow: 0 0 0 1px var(--border-subtle) inset;
  background: #f8fafc;
  transition: all 0.2s ease;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  box-shadow: 0 0 0 1px #4f46e5 inset, 0 0 0 3px rgba(79, 70, 229, 0.12);
}

.tool-btn {
  border-radius: 10px;
  font-weight: 500;
  color: var(--text-secondary);
  border-color: var(--border-subtle);
}

.tool-btn:hover {
  color: #4f46e5;
  border-color: rgba(79, 70, 229, 0.35);
  background: rgba(79, 70, 229, 0.04);
}

/* ==================== Element Plus 表格深度定制 ==================== */
.table-container {
  border: 1px solid #f1f5f9;
  border-radius: 14px;
  overflow: hidden;
}

.modern-table {
  --el-table-border-color: #f1f5f9;
  --el-table-row-hover-bg-color: #f8fafc;
  --el-table-header-bg-color: #f8fafc;
  --el-table-header-text-color: #475569;
}

.modern-table :deep(th.el-table__cell) {
  font-weight: 600;
  font-size: 13px;
  letter-spacing: 0.3px;
  padding: 14px 0;
  background-color: #f8fafc !important;
  border-bottom: 1px solid #e2e8f0;
}

.modern-table :deep(td.el-table__cell) {
  padding: 14px 0;
  border-bottom: 1px solid #f1f5f9;
}

.menu-name-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.node-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.node-dot--folder {
  background: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.15);
}

.node-dot--leaf {
  background: #cbd5e1;
}

.menu-title-text {
  font-weight: 600;
  color: #1e293b;
}

/* 路径与权限等宽字体胶囊 */
.code-badge {
  display: inline-block;
  font-family: 'JetBrains Mono', 'Fira Code', Consolas, monospace;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: all;
}

.code-badge--path {
  color: #0369a1;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
}

.code-badge--path:hover {
  background: #e0f2fe;
  border-color: #7dd3fc;
  transform: translateY(-1px);
}

.code-badge--perm {
  color: #6d28d9;
  background: #f5f3ff;
  border: 1px solid #ddd6fe;
}

.code-badge--perm:hover {
  background: #ede9fe;
  border-color: #c4b5fd;
  transform: translateY(-1px);
}

.empty-placeholder {
  color: #cbd5e1;
  font-size: 13px;
}

.empty-state {
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
/* ==================== 运营看板 · 暗色模式适配 ==================== */
html.dark .dashboard-page {
  --bg-surface: #171d2f;
  --text-main: #e5eaf6;
  --text-secondary: #93a0bb;
  --text-muted: #6b7a99;
  --border-subtle: #2a3452;
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

html.dark .dashboard-page .tool-btn {
  color: #93a0bb;
}

html.dark .dashboard-page .tool-btn:hover {
  color: #a795f2;
  background: rgba(128, 100, 223, 0.12);
}

html.dark .dashboard-page .table-container {
  border-color: #2a3452;
}

html.dark .dashboard-page .modern-table {
  --el-table-border-color: #2a3452;
  --el-table-row-hover-bg-color: rgba(74, 121, 240, 0.08);
  --el-table-header-bg-color: #1c2338;
  --el-table-header-text-color: #93a0bb;
}

html.dark .dashboard-page .modern-table :deep(th.el-table__cell) {
  background-color: #1c2338 !important;
  border-bottom-color: #2a3452;
}

html.dark .dashboard-page .modern-table :deep(td.el-table__cell) {
  border-bottom-color: #1e2639;
}

html.dark .dashboard-page .node-dot--leaf {
  background: #4a5568;
}

html.dark .dashboard-page .menu-title-text {
  color: #e5eaf6;
}

html.dark .dashboard-page .code-badge--path {
  color: #7dd3fc;
  background: rgba(56, 189, 248, 0.1);
  border-color: rgba(56, 189, 248, 0.25);
}

html.dark .dashboard-page .code-badge--path:hover {
  background: rgba(56, 189, 248, 0.18);
}

html.dark .dashboard-page .code-badge--perm {
  color: #c4b5fd;
  background: rgba(139, 92, 246, 0.1);
  border-color: rgba(139, 92, 246, 0.25);
}

html.dark .dashboard-page .code-badge--perm:hover {
  background: rgba(139, 92, 246, 0.18);
}

html.dark .dashboard-page .empty-placeholder {
  color: #4a5568;
}

html.dark .dashboard-page .count-pill {
  color: #a795f2;
  background: rgba(128, 100, 223, 0.15);
  border-color: rgba(128, 100, 223, 0.3);
}
</style>