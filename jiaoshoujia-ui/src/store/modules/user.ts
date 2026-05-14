import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, getInfo as getInfoApi, type LoginData } from '@/api/system/auth'
import { refreshToken as refreshTokenApi } from '@/api/system/auth'
import { getToken, setToken, removeToken, setRefreshToken, removeRefreshToken } from '@/utils/auth'

interface UserState {
  token: string
  name: string
  avatar: string
  roles: string[]
  permissions: string[]
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: getToken() || '',
    name: '',
    avatar: '',
    roles: [],
    permissions: [],
  }),

  actions: {
    async login(userInfo: LoginData) {
      const { username, password } = userInfo
      const res = await loginApi({ username: username.trim(), password })
      const { token, refreshToken } = res.data
      setToken(token)
      this.token = token
      if (refreshToken) {
        setRefreshToken(refreshToken)
      }
    },

    async getInfo() {
      const res = await getInfoApi()
      const { user, roles, permissions } = res.data

      if (roles && roles.length > 0) {
        this.roles = roles
        this.permissions = permissions
      } else {
        this.roles = ['ROLE_DEFAULT']
      }

      this.name = user.nickName || user.userName
      this.avatar = user.avatar || ''
      return res.data
    },

    async logout() {
      try {
        await logoutApi()
      } finally {
        this.resetState()
      }
    },

    async refreshToken() {
      const { getRefreshToken } = await import('@/utils/auth')
      const rt = getRefreshToken()
      if (!rt) throw new Error('No refresh token')
      const res = await refreshTokenApi(rt)
      const { token, refreshToken } = res.data
      setToken(token)
      this.token = token
      if (refreshToken) {
        setRefreshToken(refreshToken)
      }
    },

    resetState() {
      this.token = ''
      this.roles = []
      this.permissions = []
      this.name = ''
      this.avatar = ''
      removeToken()
      removeRefreshToken()
    },
  },
})
