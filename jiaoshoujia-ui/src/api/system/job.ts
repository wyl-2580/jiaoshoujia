import { get, post, put, del } from '@/utils/request'

export interface JobQuery {
  pageNum?: number
  pageSize?: number
  jobName?: string
  jobGroup?: string
  status?: number | string
}

export interface JobForm {
  id?: number
  jobName?: string
  jobGroup?: string
  invokeTarget?: string
  cronExpression?: string
  misfirePolicy?: number | string
  concurrent?: number | string
  status?: number | string
  remark?: string
}

export function listJob(query: JobQuery) {
  return get('/api/system/job/list', query)
}

export function getJob(jobId: number) {
  return get(`/api/system/job/${jobId}`)
}

export function addJob(data: JobForm) {
  return post('/api/system/job', data)
}

export function updateJob(data: JobForm) {
  return put('/api/system/job', data)
}

export function deleteJob(jobIds: string) {
  return del(`/api/system/job/${jobIds}`)
}

export function changeJobStatus(data: { id: number; status: number | string }) {
  return put('/api/system/job/changeStatus', data)
}

export function runJob(data: { id: number; jobGroup: string }) {
  return put('/api/system/job/run', data)
}
