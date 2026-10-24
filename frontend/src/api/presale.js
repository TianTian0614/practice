import http from './request'
export const publish = (data) => http.post('/presales/publish', data)
export const list = () => http.get('/presales')
export const reserve = (id, qty, name, phone) =>
  http.post('/presales/' + id + '/reserve', null, { params: { quantity: qty, name, phone } })
export const myReservations = () => http.get('/presales/my-reservations')
export const myPresales = () => http.get('/presales/my')
export const reservationsOf = (id) => http.get('/presales/' + id + '/reservations')
export const ship = (rid) => http.post('/presales/reservations/' + rid + '/ship')
export const confirm = (rid) => http.post('/presales/reservations/' + rid + '/confirm')
export const cancelReservation = (rid) => http.post('/presales/reservations/' + rid + '/cancel')
export const endPresale = (id) => http.post('/presales/' + id + '/end')
export const cancelPresale = (id) => http.post('/presales/' + id + '/cancel')
