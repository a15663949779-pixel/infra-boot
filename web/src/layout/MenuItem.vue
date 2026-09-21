<template>

  <!-- =====================================================
       有子菜单：父级菜单 (目录)
  ====================================================== -->
  <el-sub-menu
    v-if="hasChildren"
    :index="fullPath"
    class="premium-sub-menu"
    :class="{ 'is-top-level': level === 1 }"
  >
    <template #title>
      <div class="menu-title-wrap menu-parent-title">
        <!-- 父级菜单图标 -->
        <span class="menu-icon-shell">
          <el-icon class="menu-icon">
            <component :is="iconComponent" />
          </el-icon>
        </span>

        <!-- 父级菜单名称 -->
        <span class="menu-label">
          {{ menu.menuName }}
        </span>

        <!-- 父级菜单状态光点 -->
        <span class="menu-active-dot"></span>
      </div>
    </template>

    <!-- ===================================================
         递归子菜单 (注意：这里传递 level + 1 告知子组件层级)
    ==================================================== -->
    <MenuItem
      v-for="child in menu.children"
      :key="child.id"
      :menu="child"
      :parent-path="fullPath"
      :level="level + 1"
    />

  </el-sub-menu>


  <!-- =====================================================
       无子菜单：最终菜单 (叶子节点，例如：工作台)
  ====================================================== -->
  <el-menu-item
    v-else-if="menu.visible !== 0 && menu.menuType !== 2"
    :index="fullPath"
    class="premium-menu-item"
    :class="{ 'is-top-level': level === 1 }"
  >
    <div class="menu-title-wrap menu-leaf-title">
      <!-- 叶子菜单图标 -->
      <span class="menu-icon-shell">
        <el-icon class="menu-icon">
          <component :is="iconComponent" />
        </el-icon>
      </span>

      <!-- 叶子菜单名称 -->
      <span class="menu-label">
        {{ menu.menuName }}
      </span>

      <!-- 当前菜单状态点 -->
      <span class="menu-active-dot"></span>
    </div>
  </el-menu-item>

</template>


<script setup>
import { computed } from 'vue'

import {
  Grid,
  Menu as MenuIcon,
  Setting,
  Tickets,
  User,
  UserFilled
} from '@element-plus/icons-vue'

import { normalizeMenuPath } from '../utils/menu'

/* =========================================================
   Props
========================================================= */
const props = defineProps({
  menu: {
    type: Object,
    required: true
  },
  parentPath: {
    type: String,
    default: ''
  },
  // 新增：用于跟踪当前菜单在树形结构中的层级深度，默认最外层为 1
  level: {
    type: Number,
    default: 1
  }
})

/* =========================================================
   图标映射
========================================================= */
const iconMap = {
  setting: Setting,
  user: User,
  peoples: UserFilled,
  'tree-table': Grid,
  menu: MenuIcon,
  role: Tickets
}

/* =========================================================
   当前完整路径
========================================================= */
const fullPath = computed(() => {
  return normalizeMenuPath(
    props.parentPath,
    props.menu
  )
})

/* =========================================================
   是否存在有效子菜单
========================================================= */
const hasChildren = computed(() => {
  return props.menu.children?.some(
    (item) =>
      item.visible !== 0 &&
      item.menuType !== 2
  )
})

/* =========================================================
   当前菜单图标
========================================================= */
const iconComponent = computed(() => {
  return (
    iconMap[props.menu.icon] ||
    MenuIcon
  )
})
</script>


<style scoped>
/* =========================================================
   菜单节点基础（浅色玻璃 + 蓝紫渐变，与登录页统一）
========================================================= */
:deep(.premium-sub-menu),
:deep(.premium-menu-item) {
  position: relative;
}

.menu-title-wrap {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 11px;
  overflow: hidden;
}

:deep(.el-sub-menu__title),
:deep(.el-menu-item) {
  position: relative;
  margin: 4px 10px;
  border-radius: 12px;
  background: transparent;
  transition:
    background 0.26s ease,
    color 0.26s ease,
    box-shadow 0.26s ease,
    transform 0.45s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* 点击果冻感：按下明显收缩，松开回弹（回弹来自上面的回弹曲线） */
:deep(.premium-menu-item:active),
:deep(.premium-sub-menu > .el-sub-menu__title:active) {
  transform: scale(0.9);
  transition-duration: 0.1s;
}

/* 顶层菜单入场动画（依次滑入，用 backwards 避免结束后锁定 transform） */
:deep(.premium-menu-item.is-top-level),
:deep(.premium-sub-menu.is-top-level) {
  animation: menuItemIn 0.5s cubic-bezier(0.16, 1, 0.3, 1) backwards;
}

:deep(.premium-menu-item.is-top-level:nth-child(2)),
:deep(.premium-sub-menu.is-top-level:nth-child(2)) { animation-delay: 0.05s; }
:deep(.premium-menu-item.is-top-level:nth-child(3)),
:deep(.premium-sub-menu.is-top-level:nth-child(3)) { animation-delay: 0.1s; }
:deep(.premium-menu-item.is-top-level:nth-child(4)),
:deep(.premium-sub-menu.is-top-level:nth-child(4)) { animation-delay: 0.15s; }
:deep(.premium-menu-item.is-top-level:nth-child(5)),
:deep(.premium-sub-menu.is-top-level:nth-child(5)) { animation-delay: 0.2s; }
:deep(.premium-menu-item.is-top-level:nth-child(6)),
:deep(.premium-sub-menu.is-top-level:nth-child(6)) { animation-delay: 0.25s; }
:deep(.premium-menu-item.is-top-level:nth-child(n+7)),
:deep(.premium-sub-menu.is-top-level:nth-child(n+7)) { animation-delay: 0.3s; }

@keyframes menuItemIn {
  from { opacity: 0; transform: translateX(-16px); }
  to { opacity: 1; transform: translateX(0); }
}

/* 图标玻璃容器 */
.menu-icon-shell {
  position: relative;
  flex: 0 0 30px;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  background: var(--menu-shell-bg);
  border: 1px solid var(--menu-shell-border);
  box-shadow: 0 3px 9px rgba(70, 96, 160, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.08);
  transition:
    background 0.26s ease,
    border-color 0.26s ease,
    box-shadow 0.26s ease,
    transform 0.45s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.menu-icon {
  position: relative;
  z-index: 2;
  width: 16px;
  height: 16px;
  color: var(--menu-icon);
  transition: color 0.26s ease, transform 0.45s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.menu-label {
  position: relative;
  z-index: 2;
  flex: 1;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  font-size: 14px;
  letter-spacing: 0.2px;
  color: var(--menu-label);
  font-weight: 500;
  transition: color 0.26s ease, transform 0.26s ease;
}

/* 右侧状态点 */
.menu-active-dot {
  position: absolute;
  right: 14px;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #6f8ff2;
  box-shadow: 0 0 8px rgba(111, 143, 242, 0.55);
  opacity: 0;
  transform: scale(0.5);
  transition: opacity 0.26s ease, transform 0.26s ease;
}

/* =========================================================
   顶层叶子菜单 & 父级目录：文字更醒目
========================================================= */
:deep(.premium-menu-item.is-top-level:not(.is-active)) .menu-label,
.menu-parent-title .menu-label {
  color: var(--menu-label-strong);
  font-weight: 600;
}

:deep(.premium-sub-menu > .el-sub-menu__title .el-sub-menu__icon-arrow) {
  color: var(--menu-arrow);
  font-size: 13px;
  transition: transform 0.3s ease, color 0.26s ease;
}

/* =========================================================
   Hover（叶子与父级统一）：白色玻璃 + 轻微上浮
========================================================= */
:deep(.premium-menu-item:not(.is-active):hover),
:deep(.premium-sub-menu > .el-sub-menu__title:hover) {
  transform: translateY(-1px);
  background: var(--menu-hover-bg) !important;
  box-shadow: 0 8px 18px rgba(70, 100, 170, 0.10), inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

:deep(.premium-menu-item:not(.is-active):hover) .menu-icon-shell,
:deep(.premium-sub-menu > .el-sub-menu__title:hover) .menu-icon-shell {
  transform: translateY(-1px) scale(1.05);
  background: var(--menu-shell-hover-bg);
  box-shadow: 0 7px 16px rgba(74, 110, 200, 0.14), inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

:deep(.premium-menu-item:not(.is-active):hover) .menu-icon,
:deep(.premium-sub-menu > .el-sub-menu__title:hover) .menu-icon {
  color: var(--brand);
  transform: scale(1.06);
}

:deep(.premium-menu-item:not(.is-active):hover) .menu-label,
:deep(.premium-sub-menu > .el-sub-menu__title:hover) .menu-label {
  color: var(--menu-hover-label);
}

:deep(.premium-sub-menu > .el-sub-menu__title:hover .el-sub-menu__icon-arrow) {
  color: var(--brand);
}

:deep(.premium-menu-item:not(.is-active):not(.is-top-level):hover) .menu-active-dot {
  opacity: 0.45;
  transform: scale(1);
}

/* =========================================================
   父级目录展开状态：淡蓝玻璃
========================================================= */
:deep(.premium-sub-menu.is-opened > .el-sub-menu__title) {
  background: var(--menu-opened-bg) !important;
  box-shadow: 0 6px 16px rgba(74, 110, 200, 0.10), inset 0 1px 0 rgba(255, 255, 255, 0.08);
  animation: titlePop 0.45s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes titlePop {
  0% { transform: scale(0.94); }
  55% { transform: scale(1.03); }
  100% { transform: scale(1); }
}

:deep(.premium-sub-menu.is-opened > .el-sub-menu__title) .menu-label {
  color: var(--menu-opened-label);
  font-weight: 700;
}

:deep(.premium-sub-menu.is-opened > .el-sub-menu__title) .menu-icon-shell {
  background: var(--menu-shell-hover-bg);
  border-color: var(--menu-shell-border);
  box-shadow: 0 7px 16px rgba(74, 121, 240, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

:deep(.premium-sub-menu.is-opened > .el-sub-menu__title) .menu-icon {
  color: var(--brand);
}

:deep(.premium-sub-menu.is-opened > .el-sub-menu__title .el-sub-menu__icon-arrow) {
  color: var(--brand);
}

:deep(.premium-sub-menu.is-opened > .el-sub-menu__title) .menu-active-dot {
  opacity: 0.75;
  transform: scale(1);
}

/* =========================================================
   当前选中叶子菜单：蓝紫渐变胶囊 + 流光动画
========================================================= */
:deep(.premium-menu-item.is-active) {
  border-radius: 12px;
  background: linear-gradient(135deg, #417ff0, #596fe7, #8064df, #417ff0) !important;
  background-size: 260% 260% !important;
  box-shadow:
    0 10px 24px rgba(76, 102, 225, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.28);
  animation:
    activeGradient 7s ease infinite,
    activePop 0.55s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* 选中时的果冻弹跳：收缩 → 过冲 → 回稳 */
@keyframes activePop {
  0% { transform: scale(0.86); }
  40% { transform: scale(1.07); }
  65% { transform: scale(0.97); }
  85% { transform: scale(1.02); }
  100% { transform: scale(1); }
}

@keyframes activeGradient {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

:deep(.premium-menu-item.is-active:hover) {
  transform: translateY(-1px);
  box-shadow:
    0 14px 30px rgba(76, 102, 225, 0.42),
    inset 0 1px 0 rgba(255, 255, 255, 0.32);
}

:deep(.premium-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.55);
}

:deep(.premium-menu-item.is-active) .menu-label {
  color: #fff !important;
  font-weight: 600;
  text-shadow: 0 1px 3px rgba(40, 60, 140, 0.25);
}

:deep(.premium-menu-item.is-active) .menu-icon-shell {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.38);
  box-shadow: 0 2px 8px rgba(30, 50, 120, 0.22), inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

:deep(.premium-menu-item.is-active) .menu-icon {
  color: #fff !important;
}

:deep(.premium-menu-item.is-active) .menu-active-dot {
  opacity: 1;
  transform: scale(1);
  background: #fff;
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.85);
  animation: menuDotPulse 2.8s ease-in-out infinite;
}

@keyframes menuDotPulse {
  0%, 100% { opacity: 0.55; transform: scale(0.85); }
  50% { opacity: 1; transform: scale(1.12); }
}

/* =========================================================
   子菜单容器与层级引导线
========================================================= */
:deep(.premium-sub-menu .el-menu) {
  background: transparent !important;
}

:deep(.premium-sub-menu.is-top-level > .el-menu) {
  position: relative;
}

:deep(.premium-sub-menu.is-top-level > .el-menu)::before {
  content: '';
  position: absolute;
  left: 25px;
  top: 6px;
  bottom: 6px;
  width: 1px;
  background: var(--menu-guide);
}

/* 层级缩进 */
:deep(.premium-sub-menu .premium-menu-item) {
  padding-left: 52px !important;
}

:deep(.premium-sub-menu .premium-sub-menu > .el-sub-menu__title) {
  padding-left: 52px !important;
}

:deep(.premium-sub-menu .premium-sub-menu .premium-menu-item) {
  padding-left: 68px !important;
}

/* 去掉 Element Plus 默认 Active 指示 */
:deep(.el-menu-item.is-active::after) {
  display: none !important;
}

/* 防止 Element Plus 覆盖选中颜色 */
:deep(.el-menu-item.is-active) {
  color: inherit !important;
}

/* 点击反馈 */
:deep(.premium-menu-item:active) .menu-icon-shell {
  transform: scale(0.92);
}

:deep(.premium-menu-item:active) .menu-label {
  transform: translateX(1px);
}

/* =========================================================
   减少动画
========================================================= */
@media (prefers-reduced-motion: reduce) {
  :deep(.premium-menu-item),
  :deep(.premium-sub-menu > .el-sub-menu__title),
  .menu-icon-shell,
  .menu-icon,
  .menu-label,
  .menu-active-dot {
    transition: none !important;
    animation: none !important;
  }
  :deep(.premium-menu-item.is-top-level),
  :deep(.premium-sub-menu.is-top-level),
  :deep(.premium-menu-item.is-active) {
    animation: none !important;
  }
}
</style>