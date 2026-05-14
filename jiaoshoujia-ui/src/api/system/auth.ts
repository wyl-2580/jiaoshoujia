import { post, get } from '@/utils/request'

export interface LoginData {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  refreshToken?: string
}

export interface UserInfo {
  user: {
    userId: number
    userName: string
    nickName: string
    avatar: string
    deptId: number
  }
  roles: string[]
  permissions: string[]
}

export function login(data: LoginData) {
  return post<LoginResult>('/api/auth/login', data)
}

export function logout() {
  return post('/api/auth/logout')
}

export function getInfo() {
  return get<UserInfo>('/api/auth/getInfo')
}

export function getRouters() {
  return get<RouteMenu[]>('/api/auth/getRouters')
}

export function refreshToken(refreshToken: string) {
  return post<LoginResult>('/api/auth/refresh', { refreshToken })
}

export interface RouteMenu {
  name: string
  path: string
  hidden: boolean
  redirect?: string
  component: string
  alwaysShow?: boolean
  meta: {
    title: string
    icon: string
    noCache: boolean
    link?: string
  }
  children?: RouteMenu[]
}
