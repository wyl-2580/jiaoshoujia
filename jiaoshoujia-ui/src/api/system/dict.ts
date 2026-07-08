import { get, post, put, del, download } from '@/utils/request'

export interface DictTypeQuery {
  pageNum?: number
  pageSize?: number
  dictName?: string
  dictType?: string
  status?: number | string
  beginTime?: string
  endTime?: string
}

export interface DictTypeForm {
  id?: number
  dictName?: string
  dictType?: string
  status?: number | string
  remark?: string
}

export interface DictDataQuery {
  pageNum?: number
  pageSize?: number
  dictType?: string
  dictLabel?: string
  status?: number | string
}

export interface DictDataForm {
  id?: number
  dictSort?: number
  dictLabel?: string
  dictValue?: string
  dictType?: string
  cssClass?: string
  listClass?: string
  isDefault?: number | string
  status?: number | string
  remark?: string
}

export function listType(query: DictTypeQuery) {
  return get('/api/system/dict/type/list', query)
}

export function getType(id: number) {
  return get(`/api/system/dict/type/${id}`)
}

export function addType(data: DictTypeForm) {
  return post('/api/system/dict/type', data)
}

export function updateType(data: DictTypeForm) {
  return put('/api/system/dict/type', data)
}

export function deleteType(dictIds: string) {
  return del(`/api/system/dict/type/${dictIds}`)
}

export function listData(query: DictDataQuery) {
  return get('/api/system/dict/data/list', query)
}

export function getData(id: number) {
  return get(`/api/system/dict/data/${id}`)
}

export function addData(data: DictDataForm) {
  return post('/api/system/dict/data', data)
}

export function updateData(data: DictDataForm) {
  return put('/api/system/dict/data', data)
}

export function deleteData(dictCodes: string) {
  return del(`/api/system/dict/data/${dictCodes}`)
}

export function getDataByType(dictType: string) {
  return get(`/api/system/dict/data/type/${dictType}`)
}

export function exportType(query: DictTypeQuery) {
  return download('/api/system/dict/type/export', query, '字典类型.xlsx')
}
