import { get, post, put, del, download } from '@/utils/request'

export interface RoleQuery {
  pageNum?: number
  pageSize?: number
  roleName?: string
  roleKey?: string
  status?: number | string
  beginTime?: string
  endTime?: string
}

export interface RoleForm {
  id?: number
  roleName?: string
  roleKey?: string
  roleSort?: number
  status?: number | string
  menuIds?: number[]
  deptIds?: number[]
  remark?: string
}

export function listRole(query: RoleQuery) {
  return get('/api/system/role/list', query)
}

export function getRole(id: number) {
  return get(`/api/system/role/${id}`)
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

export function changeRoleStatus(data: { id: number; status: number | string }) {
  return put('/api/system/role/changeStatus', data)
}

export function exportRole(query: RoleQuery) {
  return download('/api/system/role/export', query, '角色数据.xlsx')
}

export function optionselect() {
  return get('/api/system/role/optionselect')
}
