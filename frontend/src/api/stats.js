import http from './request'

/** 数据大屏统计（一次返回全部） */
export const dashboard = () => http.get('/stats/dashboard')
