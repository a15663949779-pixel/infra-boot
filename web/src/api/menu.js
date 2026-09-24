import request from './request'

export function getNavTree() {
  return request.get('/system/menu/nav-tree')
}
