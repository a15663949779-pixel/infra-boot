import request from './request'

export const userApi = {
  list: () => request.get('/system/user'),
  detail: (id) => request.get(`/system/user/${id}`),
  roles: (id) => request.get(`/system/user/${id}/roles`),
  create: (data) => request.post('/system/user', data),
  update: (data) => request.put('/system/user', data),
  remove: (id) => request.delete(`/system/user/${id}`)
}

export const roleApi = {
  list: () => request.get('/system/role'),
  detail: (id) => request.get(`/system/role/${id}`),
  menus: (id) => request.get(`/system/role/${id}/menus`),
  create: (data) => request.post('/system/role', data),
  update: (data) => request.put('/system/role', data),
  remove: (id) => request.delete(`/system/role/${id}`)
}

export const menuApi = {
  list: () => request.get('/system/menu'),
  tree: () => request.get('/system/menu/tree'),
  detail: (id) => request.get(`/system/menu/${id}`),
  create: (data) => request.post('/system/menu', data),
  update: (data) => request.put('/system/menu', data),
  remove: (id) => request.delete(`/system/menu/${id}`)
}
