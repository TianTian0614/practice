import http from './request'
export const publish = (data) => http.post('/supply/publish', data)
export const list = (params) => http.get('/supply/list', { params })
export const match = (id) => http.get('/supply/match/' + id)
export const myList = (page = 0, size = 10) => http.get('/supply/my', { params: { page, size } })
export const updateSupply = (id, data) => http.put('/supply/' + id, data)
export const delSupply = (id) => http.delete('/supply/' + id)
export const offSupply = (id) => http.post('/supply/' + id + '/off')
export const onSupply = (id) => http.post('/supply/' + id + '/on')
