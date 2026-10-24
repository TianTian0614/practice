import http from './request'
export const applyCert = (type, material) => http.post('/cert/apply', { type, material })
export const myCerts = () => http.get('/cert/my')
export const pendingCerts = () => http.get('/cert/pending')
export const reviewCert = (certId, approve, rejectReason) =>
  http.post('/cert/review', null, {
    params: rejectReason ? { certId, approve, rejectReason } : { certId, approve }
  })
