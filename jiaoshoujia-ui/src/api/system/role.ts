import { get, post, put, del } from '@/utils/request'

export interface RoleQuery {
  pageNum?: number
  pageSize?: number
  roleName?: string
  roleKey?: string
  status?: string
  beginTime?: string
  endTime?: string
}

export interface RoleForm {
  roleId?: number
  roleName?: string
  roleKey?: string
  roleSort?: number
  status?: string
  menuIds?: number[]
  deptIds?: number[]
  remark?: string
}

export function listRole(query: RoleQuery) {
  return get('/api/system/role/list', query)
}

export function getRole(roleId: number) {
  return get(`/api/system/role/${roleId}`)
}

export function addRole(data: RoleForm) {
  return post('/api/system/role', data)
}

export function updateRole(data: RoleForm) {
  return put('/api/system/role', data)
}

export function deleteRole(roleIds: string) {
  return del(`/api/system/role/${roleIds}`)
}

export function changeRoleStatus(data: { roleId: number; status: string }) {
  return put('/api/system/role/changeStatus', data)
}

export function allocatedUserList(query: RoleQuery & { roleId: number }) {
  return get('/api/system/role/authUser/allocatedList', query)
}

export function unallocatedUserList(query: RoleQuery & { roleId: number }) {
  return get('/api/system/role/authUser/unallocatedList', query)
}
