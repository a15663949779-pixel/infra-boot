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

export function updateProfile(data) {
  return request.put('/system/user/profile', data)
}

export function changePassword(data) {
  return request.put('/system/user/password', data)
}

export function getMenuTree() {
  return request.get('/system/menu/tree')
}
