import http from './request'

/** 溯源批次列表 */
export const listBatches = () => http.get('/trace/batches')

/** 按溯源码查询完整溯源链 */
export const queryTrace = (batchNo) => http.get('/trace/' + encodeURIComponent(batchNo))

/** 录入溯源环节 */
export const addTrace = (data) => http.post('/trace', data)
