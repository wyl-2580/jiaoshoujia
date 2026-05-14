import { get, del } from '@/utils/request'

export interface OperlogQuery {
  pageNum?: number
  pageSize?: number
  title?: string
  operName?: string
  businessType?: number
  status?: number
  beginTime?: string
  endTime?: string
}

export function listOperlog(query: OperlogQuery) {
  return get('/api/system/operlog/list', query)
}

export function deleteOperlog(operIds: string) {
  return del(`/api/system/operlog/${operIds}`)
}

export function cleanOperlog() {
  return del('/api/system/operlog/clean')
}
