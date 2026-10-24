import http from './request'
export const consult = (question) => http.post('/ai/consult?question=' + encodeURIComponent(question))
export const ingest = (source, content) => http.post('/ai/knowledge/ingest?source=' + encodeURIComponent(source) + '&content=' + encodeURIComponent(content))
