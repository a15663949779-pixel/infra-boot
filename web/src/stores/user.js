import { defineStore } from 'pinia'
import { getMenuTree, getProfile, login, logout } from '../api/auth'
import { getToken, removeToken, setToken } from '../utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    profile: null,
    menus: []
  }),
  getters: {
    permissions: (state) => state.profile?.permissions || [],
    roles: (state) => state.profile?.roles || [],
    nickname: (state) => state.profile?.nickname || state.profile?.username || '用户'
  },
  actions: {
    async loginByPassword(form) {
      const data = await login(form)
      this.token = data.token
      setToken(data.token)
      await this.loadUserContext()
    },
    async loadUserContext() {
      const [profile, menus] = await Promise.all([getProfile(), getMenuTree()])
      this.profile = profile
      this.menus = menus || []
    },
    async logoutCurrent() {
      try {
        await logout()
      } finally {
        this.reset()
      }
    },
    reset() {
      this.token = ''
      this.profile = null
      this.menus = []
      removeToken()
    }
  }
})
