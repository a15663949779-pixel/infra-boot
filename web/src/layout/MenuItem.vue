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
   菜单节点基础
========================================================= */
:deep(.premium-sub-menu),
:deep(.premium-menu-item) {
  position: relative;
}

/* =========================================================
   菜单标题布局
========================================================= */
.menu-title-wrap {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 11px;
  overflow: hidden;
}

/* =========================================================
   Element Plus 菜单基础
========================================================= */
:deep(.el-sub-menu__title),
:deep(.el-menu-item) {
  position: relative;
  margin: 4px 10px;
  border-radius: 12px;
  background: transparent;
  transition:
    background 0.28s ease,
    color 0.28s ease,
    box-shadow 0.28s ease,
    transform 0.28s cubic-bezier(0.2, 0.8, 0.2, 1);
}

/* =========================================================
   菜单图标玻璃容器
========================================================= */
.menu-icon-shell {
  position: relative;
  flex: 0 0 30px;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.80), rgba(239, 245, 252, 0.46));
  border: 1px solid rgba(255, 255, 255, 0.86);
  box-shadow: 0 3px 10px rgba(55, 76, 108, 0.055), inset 0 1px 0 rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  transition: transform 0.28s ease, background 0.28s ease, border-color 0.28s ease, box-shadow 0.28s ease;
}

/* =========================================================
   菜单图标
========================================================= */
.menu-icon {
  position: relative;
  z-index: 2;
  width: 16px;
  height: 16px;
  color: #68798d;
  transition: color 0.28s ease, transform 0.28s ease;
}

/* =========================================================
   菜单文字基础
========================================================= */
.menu-label {
  position: relative;
  z-index: 2;
  flex: 1;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  font-size: 14px;
  letter-spacing: 0.1px;
  color: #2c3d52;
  font-weight: 500;
  transition: color 0.28s ease, transform 0.28s ease;
}

/* =========================================================
   右侧状态点
========================================================= */
.menu-active-dot {
  position: absolute;
  right: 14px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #4c9ae8;
  box-shadow: 0 0 9px rgba(76, 154, 232, 0.50);
  opacity: 0;
  transform: scale(0.5);
  transition: opacity 0.28s ease, transform 0.28s ease;
}

/* =========================================================
   ========================================================
   ★ 顶层无子节点菜单（工作台）统一样式
   解决与带有子菜单的“菜单管理”外观断层区分的问题
   ========================================================
========================================================= */
:deep(.premium-menu-item.is-top-level:not(.is-active)) {
  background: linear-gradient(105deg, rgba(255, 255, 255, 0.88) 0%, rgba(249, 252, 255, 0.72) 100%) !important;
  box-shadow: 
    0 5px 15px rgba(42, 67, 95, 0.085), 
    0 1px 3px rgba(42, 67, 95, 0.045), 
    inset 0 1px 0 rgba(255, 255, 255, 0.96), 
    inset 0 -1px 0 rgba(205, 220, 236, 0.18);
  z-index: 4;
}

:deep(.premium-menu-item.is-top-level:not(.is-active)) .menu-label {
  color: #172f48 !important;
  font-weight: 650;
  text-shadow: 0 1px 1px rgba(255, 255, 255, 0.38);
}

:deep(.premium-menu-item.is-top-level:not(.is-active)) .menu-icon-shell {
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.99), rgba(224, 238, 253, 0.76));
  border-color: rgba(255, 255, 255, 0.96);
  box-shadow: 0 6px 16px rgba(47, 87, 130, 0.10), inset 0 1px 0 rgba(255, 255, 255, 1);
}

:deep(.premium-menu-item.is-top-level:not(.is-active)) .menu-icon {
  color: #3b6b93;
}

/* 顶层叶子菜单 Hover (与父级展开样式一致) */
:deep(.premium-menu-item.is-top-level:not(.is-active):hover) {
  transform: translateY(-1px);
  background: linear-gradient(105deg, rgba(255, 255, 255, 0.97), rgba(239, 248, 255, 0.82)) !important;
  box-shadow: 
    0 8px 20px rgba(42, 72, 105, 0.115), 
    0 2px 5px rgba(42, 72, 105, 0.055), 
    inset 0 1px 0 rgba(255, 255, 255, 1);
  z-index: 8;
}

:deep(.premium-menu-item.is-top-level:not(.is-active):hover) .menu-icon-shell {
  transform: translateY(-1px) scale(1.04);
  background: linear-gradient(145deg, rgba(255, 255, 255, 1), rgba(215, 235, 255, 0.84));
  box-shadow: 0 8px 19px rgba(47, 89, 137, 0.13), inset 0 1px 0 rgba(255, 255, 255, 1);
}

:deep(.premium-menu-item.is-top-level:not(.is-active):hover) .menu-icon {
  color: #2f70a5;
  transform: scale(1.05);
}

:deep(.premium-menu-item.is-top-level:not(.is-active):hover) .menu-label {
  color: #124f82 !important;
}

/* =========================================================
   ★ 父级菜单 (目录)
   默认状态
========================================================= */
:deep(.premium-sub-menu > .el-sub-menu__title) {
  background: linear-gradient(105deg, rgba(255, 255, 255, 0.88) 0%, rgba(249, 252, 255, 0.72) 100%) !important;
  color: #172f48 !important;
  font-weight: 650;
  box-shadow: 0 5px 15px rgba(42, 67, 95, 0.085), 0 1px 3px rgba(42, 67, 95, 0.045), inset 0 1px 0 rgba(255, 255, 255, 0.96), inset 0 -1px 0 rgba(205, 220, 236, 0.18);
  z-index: 4;
}

.menu-parent-title .menu-label {
  color: #172f48 !important;
  font-weight: 650;
  text-shadow: 0 1px 1px rgba(255, 255, 255, 0.38);
}

.menu-parent-title .menu-icon-shell {
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.99), rgba(224, 238, 253, 0.76));
  border-color: rgba(255, 255, 255, 0.96);
  box-shadow: 0 6px 16px rgba(47, 87, 130, 0.10), inset 0 1px 0 rgba(255, 255, 255, 1);
}

.menu-parent-title .menu-icon {
  color: #3b6b93;
}

:deep(.premium-sub-menu > .el-sub-menu__title .el-sub-menu__icon-arrow) {
  color: #617b93;
  font-size: 13px;
  transition: transform 0.30s ease, color 0.30s ease;
}

/* 父级 Hover */
:deep(.premium-sub-menu > .el-sub-menu__title:hover) {
  transform: translateY(-1px);
  background: linear-gradient(105deg, rgba(255, 255, 255, 0.97), rgba(239, 248, 255, 0.82)) !important;
  box-shadow: 0 8px 20px rgba(42, 72, 105, 0.115), 0 2px 5px rgba(42, 72, 105, 0.055), inset 0 1px 0 rgba(255, 255, 255, 1);
  z-index: 8;
}

:deep(.premium-sub-menu > .el-sub-menu__title:hover) .menu-icon-shell {
  transform: translateY(-1px) scale(1.04);
  background: linear-gradient(145deg, rgba(255, 255, 255, 1), rgba(215, 235, 255, 0.84));
  box-shadow: 0 8px 19px rgba(47, 89, 137, 0.13), inset 0 1px 0 rgba(255, 255, 255, 1);
}

:deep(.premium-sub-menu > .el-sub-menu__title:hover) .menu-icon {
  color: #2f70a5;
  transform: scale(1.05);
}

:deep(.premium-sub-menu > .el-sub-menu__title:hover) .menu-label {
  color: #124f82 !important;
}

:deep(.premium-sub-menu > .el-sub-menu__title:hover .el-sub-menu__icon-arrow) {
  color: #326f9f;
}

/* 父级菜单展开状态 */
:deep(.premium-sub-menu.is-opened > .el-sub-menu__title) {
  background: linear-gradient(105deg, rgba(217, 239, 255, 0.90) 0%, rgba(231, 246, 255, 0.78) 100%) !important;
  color: #103f64 !important;
  box-shadow: 0 7px 20px rgba(44, 95, 142, 0.12), 0 2px 6px rgba(44, 95, 142, 0.06), inset 0 1px 0 rgba(255, 255, 255, 0.97), inset 0 -1px 0 rgba(159, 201, 231, 0.16);
  z-index: 10;
}

:deep(.premium-sub-menu.is-opened > .el-sub-menu__title) .menu-label {
  color: #103f64 !important;
  font-weight: 700;
}

:deep(.premium-sub-menu.is-opened > .el-sub-menu__title) .menu-icon-shell {
  background: linear-gradient(145deg, rgba(255, 255, 255, 1), rgba(197, 225, 250, 0.92));
  border-color: rgba(255, 255, 255, 1);
  box-shadow: 0 9px 21px rgba(47, 101, 157, 0.15), 0 0 13px rgba(68, 150, 219, 0.09), inset 0 1px 0 rgba(255, 255, 255, 1);
}

:deep(.premium-sub-menu.is-opened > .el-sub-menu__title) .menu-icon {
  color: #2c78b4;
}

:deep(.premium-sub-menu.is-opened > .el-sub-menu__title .el-sub-menu__icon-arrow) {
  color: #2d71a3;
}

:deep(.premium-sub-menu.is-opened > .el-sub-menu__title) .menu-active-dot {
  opacity: 0.78;
  transform: scale(1);
}

/* =========================================================
   内层叶子菜单 (非第一级的工作台)
========================================================= */
:deep(.premium-menu-item) {
  background: transparent;
  color: #2e4056;
  box-shadow: none;
}

/* 普通叶子菜单 Hover */
:deep(.premium-menu-item:not(.is-active):not(.is-top-level):hover) {
  transform: translateY(-1px);
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.72), rgba(255, 255, 255, 0.30)) !important;
  box-shadow: 0 5px 14px rgba(49, 76, 112, 0.065), inset 0 1px 0 rgba(255, 255, 255, 0.68);
  z-index: 5;
}

:deep(.premium-menu-item:not(.is-active):not(.is-top-level):hover) .menu-icon-shell {
  transform: translateY(-1px) scale(1.04);
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(224, 238, 253, 0.68));
  box-shadow: 0 7px 17px rgba(55, 91, 150, 0.09), inset 0 1px 0 rgba(255, 255, 255, 0.98);
}

:deep(.premium-menu-item:not(.is-active):not(.is-top-level):hover) .menu-icon {
  color: #3e80be;
  transform: scale(1.05);
}

:deep(.premium-menu-item:not(.is-active):not(.is-top-level):hover) .menu-label {
  color: #225f94;
}

:deep(.premium-menu-item:not(.is-active):not(.is-top-level):hover) .menu-active-dot {
  opacity: 0.38;
  transform: scale(1);
}

/* =========================================================
   当前选中叶子菜单 (不受层级影响，保持极佳的悬浮发光感)
========================================================= */
:deep(.premium-menu-item.is-active) {
  transform: translateY(-2px);
  margin-top: 7px;
  margin-bottom: 7px;
  border-radius: 13px;
  background: linear-gradient(105deg, rgba(201, 228, 255, 0.98) 0%, rgba(214, 235, 255, 0.96) 55%, rgba(228, 241, 255, 0.91) 100%) !important;
  box-shadow: 0 13px 29px rgba(51, 93, 143, 0.18), 0 4px 11px rgba(62, 119, 169, 0.09), 0 0 0 1px rgba(255, 255, 255, 0.38), inset 0 1px 0 rgba(255, 255, 255, 0.96), inset 0 -1px 0 rgba(171, 205, 234, 0.16);
  z-index: 20;
}

:deep(.premium-menu-item.is-active:hover) {
  transform: translateY(-3px);
  background: linear-gradient(105deg, rgba(193, 223, 255, 1) 0%, rgba(207, 231, 255, 0.98) 55%, rgba(222, 238, 255, 0.94) 100%) !important;
  box-shadow: 0 16px 34px rgba(51, 93, 143, 0.21), 0 5px 13px rgba(62, 119, 169, 0.11), 0 0 0 1px rgba(255, 255, 255, 0.42), inset 0 1px 0 rgba(255, 255, 255, 0.98);
}

:deep(.premium-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 7px;
  bottom: 7px;
  width: 4px;
  border-radius: 999px;
  background: linear-gradient(180deg, #328ef1 0%, #40a4ee 52%, #5bb9e9 100%);
  box-shadow: 0 0 9px rgba(50, 142, 241, 0.50), 0 0 18px rgba(68, 168, 235, 0.20);
  z-index: 4;
}

:deep(.premium-menu-item.is-active) .menu-label {
  color: #154b76 !important;
  font-weight: 650;
  transform: translateX(1px);
  text-shadow: 0 1px 4px rgba(255, 255, 255, 0.46);
}

:deep(.premium-menu-item.is-active) .menu-icon-shell {
  background: linear-gradient(145deg, rgba(255, 255, 255, 1), rgba(210, 230, 255, 0.88));
  border-color: rgba(255, 255, 255, 0.98);
  box-shadow: 0 9px 21px rgba(52, 96, 154, 0.16), 0 0 16px rgba(65, 150, 230, 0.11), inset 0 1px 0 rgba(255, 255, 255, 1);
}

:deep(.premium-menu-item.is-active) .menu-icon {
  color: #347fca !important;
  transform: scale(1.06);
}

:deep(.premium-menu-item.is-active) .menu-active-dot {
  opacity: 1;
  transform: scale(1);
  background: #4a9df0;
  box-shadow: 0 0 7px rgba(74, 157, 240, 0.72), 0 0 15px rgba(68, 176, 235, 0.30);
  animation: menuDotPulse 2.8s ease-in-out infinite;
}

@keyframes menuDotPulse {
  0%, 100% { opacity: 0.58; transform: scale(0.82); }
  50% { opacity: 1; transform: scale(1.12); }
}

:deep(.premium-menu-item.is-active:active) {
  transform: translateY(0) scale(0.985);
}

/* =========================================================
   子菜单容器
========================================================= */
:deep(.premium-sub-menu .el-menu) {
  background: transparent !important;
}

/* 二级菜单缩进 */
:deep(.premium-sub-menu .premium-menu-item) {
  padding-left: 52px !important;
}

/* 第三级菜单缩进 */
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
  transform: scale(0.94);
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
  :deep(.premium-menu-item.is-active),
  :deep(.premium-menu-item.is-active:hover) {
    transform: none !important;
    margin-top: 4px;
    margin-bottom: 4px;
  }
}
</style>