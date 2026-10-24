import http from './request'
export const subscribe = (keyword, region) => http.post('/subscribe', { keyword, region })
export const mySubs = () => http.get('/subscribe/my')
export const unsubscribe = (id) => http.delete('/subscribe/' + id)
