import http from './request'
export const list = (page = 0, size = 20) => http.get('/notification/list', { params: { page, size } })
export const unreadCount = () => http.get('/notification/unread-count')
export const markRead = (id) => http.post('/notification/' + id + '/read')
export const markAll = () => http.post('/notification/read-all')
export const remove = (id) => http.delete('/notification/' + id)
