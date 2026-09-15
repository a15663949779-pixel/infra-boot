<template>
  <div class="page">
    <div class="page-title">
      <div>
        <span>Dashboard</span>
        <h1>工作台</h1>
      </div>
      <el-button type="primary" :icon="Refresh" @click="refresh">刷新上下文</el-button>
    </div>

    <div class="overview-grid">
      <section class="stat-card">
        <span>当前用户</span>
        <strong>{{ userStore.profile?.username || '-' }}</strong>
        <p>{{ userStore.profile?.nickname || '-' }}</p>
      </section>
      <section class="stat-card">
        <span>角色数量</span>
        <strong>{{ userStore.roles.length }}</strong>
        <p>{{ userStore.roles.join(', ') || '-' }}</p>
      </section>
      <section class="stat-card">
        <span>权限数量</span>
        <strong>{{ userStore.permissions.length }}</strong>
        <p>Spring Security Authority</p>
      </section>
      <section class="stat-card">
        <span>菜单数量</span>
        <strong>{{ flatMenus.length }}</strong>
        <p>后端菜单树</p>
      </section>
    </div>

    <section class="content-section">
      <div class="section-head">
        <h2>已加载菜单</h2>
        <span>{{ flatMenus.length }} 项</span>
      </div>
      <el-table :data="flatMenus" row-key="id" border>
        <el-table-column prop="menuName" label="菜单名称" min-width="160" />
        <el-table-column prop="fullPath" label="前端路径" min-width="180" />
        <el-table-column prop="perms" label="权限标识" min-width="220" show-overflow-tooltip />
        <el-table-column prop="menuType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.menuType === 0 ? 'primary' : 'success'" effect="plain">
              {{ row.menuType === 0 ? '目录' : '菜单' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { flattenVisibleMenus } from '../../utils/menu'

const userStore = useUserStore()
const flatMenus = computed(() => flattenVisibleMenus(userStore.menus))

function refresh() {
  userStore.loadUserContext()
}
</script>
