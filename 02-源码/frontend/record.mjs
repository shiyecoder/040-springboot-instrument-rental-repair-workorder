import puppeteer from 'puppeteer-core'
import fs from 'fs'

const BASE = 'http://127.0.0.1:5174'
const DIR = '/Users/xiaoxin/showcase/build/NEW-040/demo'
const FR = `${DIR}/frames`
// 帧目录由外部预先清空（框架会拦截批量删除），这里只确保存在
fs.mkdirSync(FR, { recursive: true })

const sleep = ms => new Promise(r => setTimeout(r, ms))
const log = (...a) => console.log('[REC]', ...a)
let CREDS = { user: 'admin', pwd: 'admin123' }

const browser = await puppeteer.launch({
  executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  headless: 'new',
  args: ['--no-sandbox', '--hide-scrollbars', '--window-size=1920,1080', '--force-device-scale-factor=1'],
  defaultViewport: { width: 1920, height: 1080 }
})
const page = await browser.newPage()
const client = await page.target().createCDPSession()

let idx = 0
const stamps = []
const t0 = Date.now()
client.on('Page.screencastFrame', async ({ data, sessionId }) => {
  const ts = (Date.now() - t0) / 1000
  fs.writeFileSync(`${FR}/f${String(idx).padStart(5, '0')}.jpg`, Buffer.from(data, 'base64'))
  stamps.push(ts)
  idx++
  await client.send('Page.screencastFrameAck', { sessionId }).catch(() => {})
})
await client.send('Page.startScreencast', { format: 'jpeg', quality: 85, maxWidth: 1920, maxHeight: 1080, everyNthFrame: 1 })

/* ---------- 基础动作 ---------- */
async function stay(sec, note) {
  const steps = Math.max(1, Math.round(sec / 2.5))
  for (let i = 0; i < steps; i++) {
    await page.mouse.move(820 + i * 16, 460 + i * 10).catch(() => {})
    await sleep((sec * 1000) / steps)
  }
  if (note) log('stay', note)
}

async function clickText(text, wait = 1100) {
  const ok = await page.evaluate(t => {
    const nodes = [...document.querySelectorAll('button, .el-button, a, .el-radio-button__inner, .el-tabs__item')]
    const el = nodes.find(e => {
      const s = getComputedStyle(e)
      if (s.display === 'none' || s.visibility === 'hidden' || e.offsetParent === null) return false
      return (e.innerText || '').trim() === t
    })
    if (!el) return false
    el.scrollIntoView({ block: 'center' })
    el.click()
    return true
  }, text)
  if (!ok) log('!! 未找到按钮:', text)
  await sleep(wait)
  return ok
}

async function dialogVisible() {
  return await page.evaluate(() => [...document.querySelectorAll('.el-dialog, .el-drawer')]
    .some(d => d.offsetParent !== null))
}

async function closeDialog() {
  await page.evaluate(() => {
    const btns = [...document.querySelectorAll('.el-dialog__footer button, .el-drawer__footer button')]
    const cancel = btns.reverse().find(b => /取消|关闭|返回/.test((b.innerText || '').trim()))
    if (cancel) return cancel.click()
    const x = document.querySelector('.el-dialog__headerbtn, .el-drawer__close-btn')
    if (x) x.click()
  })
  await sleep(700)
}

/** 接口登录并写入登录态（UI 点击失败时的兜底，保证录屏不断链） */
async function apiLogin() {
  const ok = await page.evaluate(async (u, p) => {
    const r = await fetch('/api/auth/login', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: u, password: p })
    })
    const j = await r.json()
    if (!j || !j.data || !j.data.token) return false
    localStorage.setItem('token', j.data.token)
    localStorage.setItem('user', JSON.stringify(j.data.user))
    return true
  }, CREDS.user, CREDS.pwd)
  return ok
}

async function hasToken() {
  return await page.evaluate(() => !!localStorage.getItem('token'))
}

async function loginUi(user, pwd) {
  CREDS = { user, pwd }
  await page.evaluate(() => localStorage.clear())
  await page.goto(BASE + '/login', { waitUntil: 'networkidle2' })
  await sleep(2000)
  await page.evaluate((u, p) => {
    const ins = document.querySelectorAll('.box input')
    if (ins.length < 2) return
    const set = (el, v) => {
      const d = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set
      d.call(el, ''); el.dispatchEvent(new Event('input', { bubbles: true }))
      d.call(el, v); el.dispatchEvent(new Event('input', { bubbles: true }))
    }
    set(ins[0], u); set(ins[1], p)
  }, user, pwd)
  await stay(1.8, '填写登录信息 ' + user)
  await clickText('登 录', 2400)
  if (!(await hasToken())) {
    const ok = await apiLogin()
    log('UI 登录未生效，改用接口登录:', ok)
    await page.goto(BASE + '/dashboard', { waitUntil: 'networkidle2' })
    await sleep(1500)
  }
  log('login ->', page.url())
}

async function go(path, sec, note) {
  await page.goto(BASE + path, { waitUntil: 'networkidle2' })
  await sleep(800)
  if ((await page.evaluate(() => location.pathname)) === '/login') {
    await apiLogin()
    await page.goto(BASE + path, { waitUntil: 'networkidle2' })
    await sleep(1200)
    log('!! 掉登录态，已补登录:', path)
  }
  await stay(sec, note)
}

async function logout() {
  await clickText('退出', 1000)
  await clickText('确定', 1500)
  log('logout ->', page.url())
}

await page.goto(BASE + '/login', { waitUntil: 'networkidle2' })
await sleep(1200)

/* ================= 一、门店管理员 ADMIN ================= */
log('=== 角色1 门店管理员 ===')
await loginUi('admin', 'admin123')
await stay(9, '经营看板')
await page.evaluate(() => window.scrollBy(0, 240))
await stay(4, '看板图表')
await page.evaluate(() => window.scrollTo(0, 0))
await stay(1)

await go('/catalog', 4, '乐器租赁目录')
await clickText('详情', 1700)
if (await dialogVisible()) { await stay(2.5, '乐器详情'); await closeDialog() } else await stay(1.5)

await go('/order-desk', 5, '租赁订单台')
await clickText('审核', 1600)
if (await dialogVisible()) { await stay(2.5, '订单审核'); await closeDialog() } else await stay(1.5)

await go('/handover', 3.5, '出库交接')
await clickText('办理交接', 1600)
if (await dialogVisible()) { await stay(2.5, '交接办理'); await closeDialog() } else await stay(1.5)

await go('/return-check', 3.5, '归还验收')
await clickText('归还验收', 1600)
if (await dialogVisible()) { await stay(2.5, '验收弹窗'); await closeDialog() } else await stay(1.5)

await go('/damage', 3.5, '损坏赔偿')
await clickText('登记赔偿', 1500)
if (await dialogVisible()) { await stay(2.5, '赔偿登记'); await closeDialog() } else await stay(1.5)

await go('/repair', 3.5, '维修工单')
await clickText('处理', 1600)
if (await dialogVisible()) { await stay(2.5, '工单处理'); await closeDialog() } else await stay(1.5)

await go('/maintain', 3.5, '保养计划')
await clickText('执行保养', 1500)
if (await dialogVisible()) { await stay(2.5, '执行保养'); await closeDialog() } else await stay(1.5)

// 评分规则（创新点）
await go('/score-rule', 4, '评分规则配置')
await clickText('明细', 1500)
if (await dialogVisible()) { await stay(4, '评分留痕'); await closeDialog() } else await stay(2)
await page.evaluate(() => window.scrollTo(0, 480))
await stay(3, '规则权重')
await page.evaluate(() => window.scrollTo(0, 0))

await go('/instrument', 3.5, '乐器台账')
await clickText('新增乐器', 1500)
if (await dialogVisible()) { await stay(2.5, '新增乐器'); await closeDialog() } else await stay(1.5)

await go('/price', 3, '租赁价格')
await clickText('新增方案', 1400)
if (await dialogVisible()) { await stay(2, '价格方案'); await closeDialog() } else await stay(1.2)

await go('/calendar', 4, '可租日历')
await go('/ledger', 4, '费用台账')
await go('/user', 3.5, '用户与角色')
await clickText('分配菜单', 1500)
if (await dialogVisible()) { await stay(2.5, '分配菜单'); await closeDialog() } else await stay(1.5)
await go('/message', 3, '消息中心')
await go('/profile', 3, '个人中心')
await logout()
await stay(1)

/* ================= 二、顾客 CUSTOMER ================= */
log('=== 角色2 顾客 ===')
await loginUi('customer01', '123456')
await go('/catalog', 4, '顾客 乐器目录')
await clickText('预约', 1500)
if (await dialogVisible()) { await stay(3, '租赁预约'); await closeDialog() } else await stay(1.5)
await go('/my-orders', 5, '我的租赁')
await clickText('详情', 1500)
if (await dialogVisible()) { await stay(2.5, '订单详情'); await closeDialog() } else await stay(1.5)
await go('/my-repair', 3.5, '我的报修')
await clickText('报修', 1500)
if (await dialogVisible()) { await stay(2.5, '提交报修'); await closeDialog() } else await stay(1.5)
await go('/calendar', 3.5, '顾客 可租日历')
await go('/profile', 3, '顾客 个人中心')
await logout()
await stay(1)

/* ================= 三、维修技师 TECHNICIAN ================= */
log('=== 角色3 维修技师 ===')
await loginUi('tech01', '123456')
await go('/repair', 4.5, '技师 维修工单')
await clickText('处理', 1600)
if (await dialogVisible()) { await stay(3, '技师处理工单'); await closeDialog() } else await stay(1.5)
await go('/maintain', 4.5, '技师 保养计划')
await clickText('执行保养', 1500)
if (await dialogVisible()) { await stay(2.5, '技师执行保养'); await closeDialog() } else await stay(1.5)
await go('/instrument', 3.5, '技师 乐器台账')
await logout()
await stay(1)

await client.send('Page.stopScreencast').catch(() => {})
await sleep(600)
await browser.close()

/* ---------- ffmpeg concat 清单 ---------- */
const total = (Date.now() - t0) / 1000
let list = ''
for (let i = 0; i < stamps.length; i++) {
  const next = i + 1 < stamps.length ? stamps[i + 1] : total
  const dur = Math.min(60, Math.max(0.04, next - stamps[i]))
  list += `file 'f${String(i).padStart(5, '0')}.jpg'\nduration ${dur.toFixed(3)}\n`
}
if (stamps.length) list += `file 'f${String(stamps.length - 1).padStart(5, '0')}.jpg'\n`
fs.writeFileSync(`${FR}/list.txt`, list)
fs.writeFileSync(`${DIR}/frames.json`, JSON.stringify({ frames: stamps.length, total }))
log('frames', stamps.length, 'seconds', total.toFixed(1))
