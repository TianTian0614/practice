import http from './request'
export const listArticles = (category) => http.get('/articles', { params: { category } })
export const addArticle = (data) => http.post('/articles', data)
export const listPosts = () => http.get('/community/posts')
export const addPost = (data) => http.post('/community/posts', data)
export const listComments = (pid) => http.get('/community/posts/' + pid + '/comments')
export const addComment = (pid, content) => http.post('/community/posts/' + pid + '/comments?content=' + encodeURIComponent(content))
export const adopt = (pid, cid) => http.post('/community/posts/' + pid + '/adopt/' + cid)
