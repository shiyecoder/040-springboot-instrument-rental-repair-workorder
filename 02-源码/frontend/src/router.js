import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('./views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('./layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('./views/Dashboard.vue'), meta: { title: '资产经营看板' } },
      { path: 'catalog', component: () => import('./views/Catalog.vue'), meta: { title: '乐器租赁目录' } },
      { path: 'calendar', component: () => import('./views/Calendar.vue'), meta: { title: '可租日历' } },
      { path: 'my-orders', component: () => import('./views/MyOrders.vue'), meta: { title: '我的租赁' } },
      { path: 'my-repair', component: () => import('./views/MyRepair.vue'), meta: { title: '租期报修' } },
      { path: 'ledger', component: () => import('./views/Ledger.vue'), meta: { title: '费用台账' } },
      { path: 'order-desk', component: () => import('./views/OrderDesk.vue'), meta: { title: '租赁订单台' } },
      { path: 'handover', component: () => import('./views/Handover.vue'), meta: { title: '出库交接' } },
      { path: 'return-check', component: () => import('./views/ReturnCheck.vue'), meta: { title: '归还验收' } },
      { path: 'damage', component: () => import('./views/Damage.vue'), meta: { title: '损坏赔偿' } },
      { path: 'repair', component: () => import('./views/RepairList.vue'), meta: { title: '维修工单' } },
      { path: 'instrument', component: () => import('./views/InstrumentList.vue'), meta: { title: '乐器台账' } },
      { path: 'price', component: () => import('./views/PriceList.vue'), meta: { title: '租赁价格' } },
      { path: 'maintain', component: () => import('./views/Maintain.vue'), meta: { title: '保养计划' } },
      { path: 'score-rule', component: () => import('./views/ScoreRule.vue'), meta: { title: '评分规则' } },
      { path: 'user', component: () => import('./views/UserList.vue'), meta: { title: '用户与角色' } },
      { path: 'menu', component: () => import('./views/MenuList.vue'), meta: { title: '菜单管理' } },
      { path: 'message', component: () => import('./views/Message.vue'), meta: { title: '消息中心' } },
      { path: 'profile', component: () => import('./views/Profile.vue'), meta: { title: '个人中心' } }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (!to.meta.public && !token) return '/login'
  if (to.path === '/login' && token) return '/dashboard'
  return true
})

export default router
