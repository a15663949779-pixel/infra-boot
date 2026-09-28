const componentMap = {
  'dashboard/DashboardView': () => import('../views/dashboard/DashboardView.vue'),
  'dashboard/OperationDashboard': () => import('../views/dashboard/OperationDashboard.vue'),
  'system/user/index': () => import('../views/system/user/UserView.vue'),
  'system/role/index': () => import('../views/system/role/RoleView.vue'),
  'system/menu/index': () => import('../views/system/menu/MenuView.vue'),
}

export function resolveComponent(component) {
  return componentMap[component] || null
}
