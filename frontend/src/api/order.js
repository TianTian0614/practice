import http from './request'
export const createOrder = (data) => http.post('/order/create', data)
export const payOrder = (id) => http.post('/order/' + id + '/pay')
export const shipOrder = (id, expressNo) => http.post('/order/' + id + '/ship', { expressNo })
export const confirmOrder = (id) => http.post('/order/' + id + '/confirm')
export const cancelOrder = (id) => http.post('/order/' + id + '/cancel')
export const myOrders = () => http.get('/order/my')
