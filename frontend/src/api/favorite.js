import http from './request'
export const toggleFav = (targetType, targetId) => http.post('/favorite/toggle', { targetType, targetId })
export const checkFav = (targetType, targetId) => http.get('/favorite/check', { params: { targetType, targetId } })
export const myFavorites = (targetType) =>
  http.get('/favorite/list', { params: targetType ? { targetType } : {} })
