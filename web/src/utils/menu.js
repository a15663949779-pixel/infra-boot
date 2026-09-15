export function normalizeMenuPath(parentPath, menu) {
  if (!menu.path) {
    return parentPath || '/'
  }
  if (menu.path.startsWith('/')) {
    return menu.path
  }
  const prefix = parentPath && parentPath !== '/' ? parentPath : ''
  return `${prefix}/${menu.path}`.replace(/\/+/g, '/')
}

export function flattenVisibleMenus(menus, parentPath = '') {
  const result = []
  menus.forEach((menu) => {
    const fullPath = normalizeMenuPath(parentPath, menu)
    const item = { ...menu, fullPath }
    if (menu.visible !== 0 && menu.menuType !== 2) {
      result.push(item)
    }
    if (menu.children?.length) {
      result.push(...flattenVisibleMenus(menu.children, fullPath))
    }
  })
  return result
}

export function buildTreeFromFlat(list) {
  const nodeMap = new Map()
  const roots = []
  list.forEach((item) => {
    nodeMap.set(item.id, { ...item, children: [] })
  })
  nodeMap.forEach((node) => {
    if (node.parentId && node.parentId !== 0 && nodeMap.has(node.parentId)) {
      nodeMap.get(node.parentId).children.push(node)
    } else {
      roots.push(node)
    }
  })
  const sortTree = (nodes) => {
    nodes.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
    nodes.forEach((node) => sortTree(node.children))
    return nodes
  }
  return sortTree(roots)
}
