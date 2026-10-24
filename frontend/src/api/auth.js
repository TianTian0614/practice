import http from './request'
export const login = (data) => http.post('/auth/login', data)
export const register = (data) => http.post('/auth/register', data)
