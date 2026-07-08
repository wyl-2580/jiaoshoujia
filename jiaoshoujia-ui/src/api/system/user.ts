import { get, post, put, del, download } from '@/utils/request'

export interface UserQuery {
  pageNum?: number
  pageSize?: number
  username?: string
  phone?: string
  status?: number | string
  deptId?: number
  beginTime?: string
  endTime?: string
}

export interface UserForm {
  id?: number
  deptId?: number
  username?: string
  nickname?: string
  password?: string
  phone?: string
  email?: string
  sex?: number | string
  status?: number | string
  postIds?: number[]
  roleIds?: number[]
  remark?: string
}

export function listUser(query: UserQuery) {
  return get('/api/system/user/list', query)
}

export function getUser(id: number) {
  return get(`/api/system/user/${id}`)
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

export function resetUserPwd(data: { id: number; password: string }) {
  return put('/api/system/user/resetPwd', data)
}

export function changeUserStatus(data: { id: number; status: number | string }) {
  return put('/api/system/user/changeStatus', data)
}

export function exportUser(query: UserQuery) {
  return download('/api/system/user/export', query, '用户数据.xlsx')
}
