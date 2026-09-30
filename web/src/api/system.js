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
  detail: (id) => request.get(`/system/menu/${id}`),
  create: (data) => request.post('/system/menu', data),
  update: (data) => request.put('/system/menu', data),
  remove: (id) => request.delete(`/system/menu/${id}`)
}

export const operLogApi = {
  list: (params) => request.get('/system/operlog/list', { params }),
  remove: (ids) => request.delete(`/system/operlog/${ids}`),
  clean: () => request.delete('/system/operlog/clean')
}

export const dictTypeApi = {
  list: (params) => request.get('/system/dict/type/list', { params }),
  detail: (id) => request.get(`/system/dict/type/${id}`),
  create: (data) => request.post('/system/dict/type', data),
  update: (data) => request.put('/system/dict/type', data),
  remove: (ids) => request.delete(`/system/dict/type/${ids}`),
  clearCache: () => request.delete('/system/dict/type/clearCache')
}

export const dictDataApi = {
  list: (params) => request.get('/system/dict/data/list', { params }),
  getByType: (dictType) => request.get(`/system/dict/data/type/${dictType}`),
  detail: (id) => request.get(`/system/dict/data/${id}`),
  create: (data) => request.post('/system/dict/data', data),
  update: (data) => request.put('/system/dict/data', data),
  remove: (ids) => request.delete(`/system/dict/data/${ids}`)
}
