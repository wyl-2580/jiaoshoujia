import { get, post, put, del } from '@/utils/request'

export interface JobQuery {
  pageNum?: number
  pageSize?: number
  jobName?: string
  jobGroup?: string
  status?: string
}

export interface JobForm {
  jobId?: number
  jobName?: string
  jobGroup?: string
  invokeTarget?: string
  cronExpression?: string
  misfirePolicy?: string
  concurrent?: string
  status?: string
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

export function changeJobStatus(data: { jobId: number; status: string }) {
  return put('/api/system/job/changeStatus', data)
}

export function runJob(data: { jobId: number; jobGroup: string }) {
  return put('/api/system/job/run', data)
}
