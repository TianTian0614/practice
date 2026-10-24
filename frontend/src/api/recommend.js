import http from './request'
export const recommend = (limit = 6) => http.get('/recommend', { params: { limit } })
