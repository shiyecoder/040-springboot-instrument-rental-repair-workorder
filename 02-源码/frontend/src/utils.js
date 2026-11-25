// 全局格式化与状态映射：全系统统一调用，禁止页面各自实现
export function formatTime(v) {
  if (!v) return '-'
  const s = String(v).replace('T', ' ')
  return s.length >= 19 ? s.slice(0, 19) : s
}

export function formatDate(v) {
  if (!v) return '-'
  return formatTime(v).slice(0, 10)
}

export function money(v) {
  if (v === null || v === undefined || v === '') return '0.00'
  return Number(v).toFixed(2)
}

// 租赁订单状态机
export const ORDER_STATUS = {
  PENDING_AUDIT: { text: '待审核', type: 'warning' },
  PENDING_DEPOSIT: { text: '待押金', type: 'warning' },
  PENDING_HANDOVER: { text: '待交接', type: 'primary' },
  RENTING: { text: '租用中', type: 'primary' },
  PENDING_RETURN: { text: '待归还验收', type: 'danger' },
  COMPLETED: { text: '已完成', type: 'success' },
  REJECTED: { text: '已退回', type: 'info' },
  CANCELLED: { text: '已取消', type: 'info' }
}

export const INSTRUMENT_STATUS = {
  AVAILABLE: { text: '可租', type: 'success' },
  RESERVED: { text: '已预留', type: 'warning' },
  RENTING: { text: '租用中', type: 'primary' },
  REPAIRING: { text: '维修中', type: 'danger' },
  MAINTAINING: { text: '保养中', type: 'warning' },
  SCRAPPED: { text: '已报废', type: 'info' }
}

export const REPAIR_STATUS = {
  PENDING: { text: '待接单', type: 'danger' },
  DIAGNOSING: { text: '检测中', type: 'warning' },
  REPAIRING: { text: '维修中', type: 'primary' },
  TESTING: { text: '待试奏', type: 'warning' },
  COMPLETED: { text: '待验收', type: 'primary' },
  CLOSED: { text: '已完成', type: 'success' }
}

export const SCORE_LEVEL = {
  RECOMMEND: { text: '推荐出租', type: 'success' },
  OBSERVE: { text: '观察', type: 'primary' },
  MAINTAIN: { text: '待保养', type: 'warning' },
  FORBID: { text: '禁止出租', type: 'danger' }
}

export const DEPOSIT_STATUS = {
  UNPAID: { text: '待登记', type: 'warning' },
  PAID: { text: '已收', type: 'primary' },
  REFUNDED: { text: '已退还', type: 'success' }
}

export const ROLE_TEXT = {
  ADMIN: '门店管理员',
  CLERK: '门店营业员',
  TECHNICIAN: '维修技师',
  CUSTOMER: '客户'
}

export const RENEW_STATUS = {
  PENDING: { text: '待审核', type: 'warning' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已驳回', type: 'danger' }
}

export function meta(map, key) {
  return map[key] || { text: key || '-', type: 'info' }
}

export const orderMeta = (s) => meta(ORDER_STATUS, s)
export const instMeta = (s) => meta(INSTRUMENT_STATUS, s)
export const repairMeta = (s) => meta(REPAIR_STATUS, s)
export const scoreMeta = (s) => meta(SCORE_LEVEL, s)
export const depositMeta = (s) => meta(DEPOSIT_STATUS, s)
export const renewMeta = (s) => meta(RENEW_STATUS, s)
export const roleText = (c) => ROLE_TEXT[c] || c || '-'

// 租期天数（含起止两天，与后端算法一致）
export function calcDays(start, end) {
  if (!start || !end) return 0
  const a = new Date(String(start).replace(/-/g, '/'))
  const b = new Date(String(end).replace(/-/g, '/'))
  const d = Math.round((b - a) / 86400000) + 1
  return d > 0 ? d : 0
}

// 图表统一配色：与 design-system 一致，同义同色
export const CHART_COLORS = ['#19324A', '#356A7A', '#456C7E', '#8FA3B0', '#2B7955', '#B02A37', '#8A5B10']
