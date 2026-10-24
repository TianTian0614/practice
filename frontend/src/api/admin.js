import http from './request'
export const audit = (type, targetId, action, note = '') =>
  http.post('/admin/audit', null, { params: { type, targetId, action, note } })
export const logs = () => http.get('/admin/audit-logs')
export const supplyPending = () => http.get('/admin/supply-pending')
