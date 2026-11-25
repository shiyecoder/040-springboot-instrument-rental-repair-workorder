import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({ baseURL: '/api', timeout: 20000 })

http.interceptors.request.use((cfg) => {
  const token = localStorage.getItem('token')
  if (token) cfg.headers.Authorization = 'Bearer ' + token
  return cfg
})

http.interceptors.response.use(
  (res) => {
    if (res.config.responseType === 'blob') return res
    const body = res.data
    if (body && body.code === 200) return body.data
    if (body && body.code === 401) {
      localStorage.removeItem('token')
      location.href = '/login'
      return Promise.reject(new Error(body.msg))
    }
    ElMessage.error(body?.msg || '请求失败')
    return Promise.reject(new Error(body?.msg || '请求失败'))
  },
  (err) => {
    const status = err.response?.status
    if (status === 401) {
      localStorage.removeItem('token')
      location.href = '/login'
    } else {
      ElMessage.error(err.response?.data?.msg || err.message || '网络异常')
    }
    return Promise.reject(err)
  }
)

export default http

// 统一接口封装：页面不重复实现分页、上传、时间格式与状态映射
export const api = {
  login: (d) => http.post('/auth/login', d),
  me: () => http.get('/auth/me'),
  updateProfile: (d) => http.put('/auth/profile', d),
  changePassword: (d) => http.post('/auth/password', d),
  saveAvatar: (avatar) => http.post('/auth/avatar', { avatar }),
  upload: (file, type) => {
    const fd = new FormData()
    fd.append('file', file)
    fd.append('type', type || 'common')
    return http.post('/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
  },

  dashboard: (range) => http.get('/dashboard', { params: { range } }),

  instrumentPage: (p) => http.get('/instrument/page', { params: p }),
  instrumentDetail: (id) => http.get('/instrument/' + id),
  instrumentSave: (d) => (d.id ? http.put('/instrument/' + d.id, d) : http.post('/instrument', d)),
  instrumentDelete: (id) => http.delete('/instrument/' + id),
  instrumentOptions: (status) => http.get('/instrument/options', { params: { status } }),

  priceList: () => http.get('/price/list'),
  priceSave: (d) => (d.id ? http.put('/price/' + d.id, d) : http.post('/price', d)),

  maintainPage: (p) => http.get('/maintain/page', { params: p }),
  maintainCreate: (d) => http.post('/maintain', d),
  maintainExecute: (id, d) => http.post(`/maintain/${id}/execute`, d),

  rentalPage: (p) => http.get('/rental/page', { params: p }),
  rentalDetail: (id) => http.get('/rental/' + id),
  rentalCreate: (d) => http.post('/rental', d),
  rentalAudit: (id, d) => http.post(`/rental/${id}/audit`, d),
  rentalDeposit: (id, d) => http.post(`/rental/${id}/deposit`, d),
  rentalHandover: (id, d) => http.post(`/rental/${id}/handover`, d),
  rentalReturn: (id, d) => http.post(`/rental/${id}/return`, d),
  rentalRenew: (id, d) => http.post(`/rental/${id}/renew`, d),
  renewAudit: (id, d) => http.post(`/rental/renew/${id}/audit`, d),
  renewPage: () => http.get('/rental/renew/page'),
  rentalDamage: (id, d) => http.post(`/rental/${id}/damage`, d),
  rentalRepair: (id, d) => http.post(`/rental/${id}/repair`, d),
  rentalFeedback: (id, d) => http.post(`/rental/${id}/feedback`, d),
  calendar: (p) => http.get('/rental/calendar', { params: p }),
  ledger: () => http.get('/rental/ledger'),

  repairPage: (p) => http.get('/repair/page', { params: p }),
  repairDetail: (id) => http.get('/repair/' + id),
  repairCreate: (d) => http.post('/repair', d),
  repairAccept: (id) => http.post(`/repair/${id}/accept`),
  repairDiagnose: (id, d) => http.post(`/repair/${id}/diagnose`, d),
  repairPart: (id, d) => http.post(`/repair/${id}/part`, d),
  repairFinish: (id, d) => http.post(`/repair/${id}/finish`, d),
  repairClose: (id) => http.post(`/repair/${id}/close`),

  ruleList: () => http.get('/score/rule/list'),
  ruleCurrent: () => http.get('/score/rule/current'),
  rulePublish: (d) => http.post('/score/rule', d),
  scorePage: (p) => http.get('/score/page', { params: p }),
  scoreDetail: (id) => http.get('/score/' + id),
  scoreByInstrument: (id) => http.get('/score/by-instrument/' + id),
  evaluate: (id, ruleId) => http.post(`/score/evaluate/${id}`, null, { params: { ruleId } }),
  scoreAdjust: (id, d) => http.post(`/score/${id}/adjust`, d),
  scoreEffect: () => http.get('/score/effect'),

  userPage: (p) => http.get('/user/page', { params: p }),
  userSave: (d) => (d.id ? http.put('/user/' + d.id, d) : http.post('/user', d)),
  userReset: (id) => http.post(`/user/${id}/reset`),
  userDelete: (id) => http.delete('/user/' + id),
  roleList: () => http.get('/role/list'),
  roleMenus: (id) => http.get(`/role/${id}/menus`),
  roleAssign: (id, ids) => http.post(`/role/${id}/menus`, ids),
  menuTree: () => http.get('/menu/tree'),
  menuSave: (d) => http.post('/menu', d),
  dict: (t) => http.get('/dict/' + t),
  messages: (p) => http.get('/message/page', { params: p }),
  messageRead: (id) => http.post(`/message/${id}/read`),
  logs: (p) => http.get('/log/page', { params: p })
}
