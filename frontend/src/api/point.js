import http from './request'
export const myPoints = () => http.get('/point/my')
export const pointRank = (limit = 20) => http.get('/point/rank', { params: { limit } })
