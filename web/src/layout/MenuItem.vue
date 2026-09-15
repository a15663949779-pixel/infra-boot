<template>
  <el-sub-menu v-if="hasChildren" :index="fullPath">
    <template #title>
      <el-icon><component :is="iconComponent" /></el-icon>
      <span>{{ menu.menuName }}</span>
    </template>
    <MenuItem v-for="child in menu.children" :key="child.id" :menu="child" :parent-path="fullPath" />
  </el-sub-menu>
  <el-menu-item v-else-if="menu.visible !== 0 && menu.menuType !== 2" :index="fullPath">
    <el-icon><component :is="iconComponent" /></el-icon>
    <template #title>{{ menu.menuName }}</template>
  </el-menu-item>
</template>

<script setup>
import { computed } from 'vue'
import { Grid, Menu as MenuIcon, Setting, Tickets, User, UserFilled } from '@element-plus/icons-vue'
import { normalizeMenuPath } from '../utils/menu'

const props = defineProps({
  menu: {
    type: Object,
    required: true
  },
  parentPath: {
    type: String,
    default: ''
  }
})

const iconMap = {
  setting: Setting,
  user: User,
  peoples: UserFilled,
  'tree-table': Grid,
  menu: MenuIcon,
  role: Tickets
}

const fullPath = computed(() => normalizeMenuPath(props.parentPath, props.menu))
const hasChildren = computed(() => props.menu.children?.some((item) => item.visible !== 0 && item.menuType !== 2))
const iconComponent = computed(() => iconMap[props.menu.icon] || MenuIcon)
</script>
