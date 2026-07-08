import { get, del, download } from '@/utils/request'

export interface LogininforQuery {
  pageNum?: number
  pageSize?: number
  userName?: string
  ipaddr?: string
  status?: number | string
}

export function listLogininfor(query: LogininforQuery) {
  return get('/api/system/logininfor/list', query)
}

export function deleteLogininfor(infoIds: string) {
  return del(`/api/system/logininfor/${infoIds}`)
}

export function cleanLogininfor() {
  return del('/api/system/logininfor/clean')
}

export function exportLogininfor(query: LogininforQuery) {
  return download('/api/system/logininfor/export', query, '登录日志.xlsx')
}
