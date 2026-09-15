import request from './request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function getProfile() {
  return request.get('/system/user/profile')
}

export function getMenuTree() {
  return request.get('/system/menu/tree')
}
