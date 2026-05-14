import { get, post, put, del } from '@/utils/request'

export interface DeptQuery {
  deptName?: string
  status?: string
}

export interface DeptForm {
  deptId?: number
  parentId?: number
  deptName?: string
  orderNum?: number
  leader?: string
  phone?: string
  email?: string
  status?: string
}

export function listDept(query?: DeptQuery) {
  return get('/api/system/dept/list', query)
}

export function getDept(deptId: number) {
  return get(`/api/system/dept/${deptId}`)
}

export function addDept(data: DeptForm) {
  return post('/api/system/dept', data)
}

export function updateDept(data: DeptForm) {
  return put('/api/system/dept', data)
}

export function deleteDept(deptId: number) {
  return del(`/api/system/dept/${deptId}`)
}

export function treeselect() {
  return get('/api/system/dept/treeselect')
}

export function roleDeptTreeselect(roleId: number) {
  return get(`/api/system/dept/roleDeptTreeselect/${roleId}`)
}
