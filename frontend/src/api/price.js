import http from './request'

/** 已有行情的农产品清单 */
export const listProducts = () => http.get('/price/products')

/** 价格趋势 { dates, series: [{market, data}] } */
export const trend = (product) => http.get('/price/trend', { params: { product } })

/** 最新比价表 */
export const latest = () => http.get('/price/latest')

/** 报价列表（?product= 可选） */
export const quotes = (product) => http.get('/price/quotes', { params: { product } })

/** 新增报价 */
export const addQuote = (data) => http.post('/price', data)
