import http from './request'

export const me = () => http.get('/user/me')
export const getPublic = (id) => http.get('/user/public/' + id)
export const updateMe = (data) => http.put('/user/me', data)
