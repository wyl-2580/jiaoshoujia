import { get, post, put, del } from '@/utils/request'

export interface UserQuery {
  pageNum?: number
  pageSize?: number
  userName?: string
  phonenumber?: string
  status?: string
  deptId?: number
  beginTime?: string
  endTime?: string
}

export interface UserForm {
  userId?: number
  deptId?: number
  userName?: string
  nickName?: string
  password?: string
  phonenumber?: string
  email?: string
  sex?: string
  status?: string
  postIds?: number[]
  roleIds?: number[]
  remark?: string
}

export function listUser(query: UserQuery) {
  return get('/api/system/user/list', query)
}

export function getUser(userId: number) {
  return get(`/api/system/user/${userId}`)
}

export function addUser(data: UserForm) {
  return post('/api/system/user', data)
}

export function updateUser(data: UserForm) {
  return put('/api/system/user', data)
}

export function deleteUser(userIds: string) {
  return del(`/api/system/user/${userIds}`)
}

export function resetUserPwd(data: { userId: number; password: string }) {
  return put('/api/system/user/resetPwd', data)
}

export function changeUserStatus(data: { userId: number; status: string }) {
  return put('/api/system/user/changeStatus', data)
}
