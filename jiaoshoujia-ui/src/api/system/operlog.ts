import { get, del, download } from '@/utils/request'

export interface OperlogQuery {
  pageNum?: number
  pageSize?: number
  title?: string
  operName?: string
  businessType?: number | string
  status?: number | string
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

export function exportOperlog(query: OperlogQuery) {
  return download('/api/system/operlog/export', query, '操作日志.xlsx')
}
