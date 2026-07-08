import { get, post, put, del } from '@/utils/request'

export interface DeptQuery {
  deptName?: string
  status?: number | string
}

export interface DeptForm {
  id?: number
  parentId?: number
  deptName?: string
  orderNum?: number
  leader?: string
  phone?: string
  email?: string
  status?: number | string
}

export function listDept(query?: DeptQuery) {
  return get('/api/system/dept/list', query)
}

export function getDept(id: number) {
  return get(`/api/system/dept/${id}`)
}

export function addDept(data: DeptForm) {
  return post('/api/system/dept', data)
}

export function updateDept(data: DeptForm) {
  return put('/api/system/dept', data)
}

export function deleteDept(id: number) {
  return del(`/api/system/dept/${id}`)
}

export function deptTreeselect() {
  return get('/api/system/dept/treeselect')
}
