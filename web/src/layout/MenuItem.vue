<template>
  <template v-if="isVisible">
    <el-sub-menu
      v-if="visibleChildren.length"
      :index="fullPath"
      class="nav-menu-group"
      :class="{ 'is-root': level === 1 }"
      :style="{ '--nav-depth': level - 1 }"
      popper-class="nav-menu-popup"
    >
      <template #title>
        <el-icon class="nav-item-icon"><component :is="iconComponent" /></el-icon>
        <span class="nav-item-label">{{ menu.menuName }}</span>
        <span class="nav-item-count">{{ visibleChildren.length }}</span>
      </template>
      <MenuItem
        v-for="child in visibleChildren"
        :key="child.id"
        :menu="child"
        :parent-path="fullPath"
        :level="level + 1"
      />
    </el-sub-menu>
    <el-menu-item
      v-else
      :index="fullPath"
      class="nav-menu-item"
      :class="{ 'is-root': level === 1 }"
      :style="{ '--nav-depth': level - 1 }"
    >
      <el-icon class="nav-item-icon"><component :is="iconComponent" /></el-icon>
      <template #title><span class="nav-item-label">{{ menu.menuName }}</span></template>
    </el-menu-item>
  </template>
</template>

<script setup>
import { computed } from 'vue'
import {
  Collection, DataBoard, Document, Folder, Grid, Key,
  Menu as MenuIcon, Setting, Tickets, User, UserFilled
} from '@element-plus/icons-vue'
import { normalizeMenuPath } from '../utils/menu'

const props = defineProps({
  menu: { type: Object, required: true },
  parentPath: { type: String, default: '' },
  level: { type: Number, default: 1 }
})
const iconMap = {
  setting: Setting, user: User, peoples: UserFilled, 'tree-table': Grid,
  menu: MenuIcon, role: Key, 'data-board': DataBoard, log: Tickets, dict: Collection
}
const isVisible = computed(() => props.menu.visible !== 0 && props.menu.menuType !== 2)
const visibleChildren = computed(() => (props.menu.children || []).filter(
  item => item.visible !== 0 && item.menuType !== 2
))
const fullPath = computed(() => normalizeMenuPath(props.parentPath, props.menu))
const iconComponent = computed(() => iconMap[props.menu.icon] || (visibleChildren.value.length ? Folder : Document))
</script>
