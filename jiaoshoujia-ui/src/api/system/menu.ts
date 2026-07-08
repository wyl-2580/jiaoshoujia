import { get, post, put, del } from '@/utils/request'

export interface MenuQuery {
  menuName?: string
  status?: number | string
}

export interface MenuForm {
  id?: number
  parentId?: number
  menuName?: string
  orderNum?: number
  path?: string
  component?: string
  queryParam?: string
  isFrame?: number | string
  isCache?: number | string
  menuType?: string
  visible?: number | string
  status?: number | string
  perms?: string
  icon?: string
}

export function listMenu(query?: MenuQuery) {
  return get('/api/system/menu/list', query)
}

export function getMenu(id: number) {
  return get(`/api/system/menu/${id}`)
}

export function addMenu(data: MenuForm) {
  return post('/api/system/menu', data)
}

export function updateMenu(data: MenuForm) {
  return put('/api/system/menu', data)
}

export function deleteMenu(id: number) {
  return del(`/api/system/menu/${id}`)
}

export function treeselect() {
  return get('/api/system/menu/treeselect')
}
