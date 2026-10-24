import http from './request'
export const search = (q) => http.get('/search', { params: { q } })
