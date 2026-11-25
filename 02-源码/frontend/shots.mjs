import puppeteer from 'puppeteer-core'
import fs from 'fs'

const BASE = 'http://127.0.0.1:5174'
const OUT = '/Users/xiaoxin/showcase/build/NEW-040/verify/shots'
fs.mkdirSync(OUT, { recursive: true })
const CHROME = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'

const plan = [
  { user: 'admin', pass: 'admin123', pages: [
    ['02_dashboard', '/dashboard'], ['03_catalog', '/catalog'], ['04_calendar', '/calendar'],
    ['05_order_desk', '/order-desk'], ['06_handover', '/handover'], ['07_return_check', '/return-check'],
    ['08_damage', '/damage'], ['09_repair', '/repair'], ['10_instrument', '/instrument'],
    ['11_price', '/price'], ['12_maintain', '/maintain'], ['13_score_rule', '/score-rule'],
    ['14_user', '/user'], ['15_menu', '/menu'], ['16_message', '/message'],
    ['17_profile', '/profile'], ['18_ledger', '/ledger']
  ]},
  { user: 'customer01', pass: '123456', pages: [
    ['19_my_orders', '/my-orders'], ['20_my_repair', '/my-repair'], ['21_customer_catalog', '/catalog']
  ]},
  { user: 'tech01', pass: '123456', pages: [['22_tech_repair', '/repair']] }
]

const sleep = (ms) => new Promise(r => setTimeout(r, ms))
const browser = await puppeteer.launch({
  executablePath: CHROME, headless: 'new',
  args: ['--no-sandbox', '--window-size=1600,950', '--force-device-scale-factor=1'],
  defaultViewport: { width: 1600, height: 950 }
})
const page = await browser.newPage()
const errors = []
page.on('console', m => { if (m.type() === 'error') errors.push(m.text().slice(0, 160)) })
page.on('pageerror', e => errors.push('PAGEERROR: ' + e.message.slice(0, 160)))

await page.goto(BASE + '/login', { waitUntil: 'networkidle2' })
await sleep(1200)
await page.screenshot({ path: `${OUT}/01_login.png` })
fs.copyFileSync(`${OUT}/01_login.png`, `${OUT}/login-new.png`)

for (const role of plan) {
  await page.goto(BASE + '/login', { waitUntil: 'domcontentloaded' })
  const token = await page.evaluate(async (u, p) => {
    const r = await fetch('/api/auth/login', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: u, password: p })
    })
    const j = await r.json()
    if (j.code !== 200) throw new Error('login failed: ' + j.msg)
    return j.data.token
  }, role.user, role.pass)
  await page.evaluate((t) => localStorage.setItem('token', t), token)
  console.log('login ok', role.user)
  for (const [name, path] of role.pages) {
    await page.goto(BASE + path, { waitUntil: 'networkidle2' })
    await sleep(1500)
    const url = page.url()
    if (url.includes('/login')) console.log('!! redirected', name)
    await page.screenshot({ path: `${OUT}/${name}.png` })
  }
}
await browser.close()
console.log('CONSOLE_ERRORS', JSON.stringify([...new Set(errors)], null, 1))
