import { get, post, put, del } from '@/utils/request'

export interface MenuQuery {
  menuName?: string
  status?: string
}

export interface MenuForm {
  menuId?: number
  parentId?: number
  menuName?: string
  orderNum?: number
  path?: string
  component?: string
  queryParam?: string
  isFrame?: string
  isCache?: string
  menuType?: string
  visible?: string
  status?: string
  perms?: string
  icon?: string
}

export function listMenu(query?: MenuQuery) {
  return get('/api/system/menu/list', query)
}

export function getMenu(menuId: number) {
  return get(`/api/system/menu/${menuId}`)
}

export function addMenu(data: MenuForm) {
  return post('/api/system/menu', data)
}

export function updateMenu(data: MenuForm) {
  return put('/api/system/menu', data)
}

export function deleteMenu(menuId: number) {
  return del(`/api/system/menu/${menuId}`)
}

export function treeselect() {
  return get('/api/system/menu/treeselect')
}

export function roleMenuTreeselect(roleId: number) {
  return get(`/api/system/menu/roleMenuTreeselect/${roleId}`)
}
