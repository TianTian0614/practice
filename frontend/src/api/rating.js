import http from './request'
export const create = (data) => http.post('/rating', data)
export const listByUser = (toUserId, page = 0, size = 10) =>
  http.get('/rating/user/' + toUserId, { params: { page, size } })
export const update = (id, score, comment) => http.put('/rating/' + id, { score, comment })
export const remove = (id) => http.delete('/rating/' + id)
