<template>
  <div>
    <div class="pagehead">
      <div><h1>可租日历</h1><p>综合已租、已预留、维修与保养时段，判断某台乐器是否可租</p></div>
    </div>

    <div class="card">
      <div class="filters">
        <el-select v-model="instrumentId" filterable placeholder="选择乐器" style="width:260px" @change="load">
          <el-option v-for="o in options" :key="o.id" :label="o.label" :value="o.id" />
        </el-select>
        <el-date-picker v-model="month" type="month" value-format="YYYY-MM" placeholder="月份" style="width:180px" @change="load" />
        <el-button @click="load">刷新</el-button>
        <span class="hint">绿色可租 · 蓝色已租 · 橙色保养 · 红色维修</span>
      </div>
    </div>

    <div class="split">
      <div class="card">
        <div class="card-title">{{ month }} 占用日历</div>
        <div class="cal">
          <div v-for="d in days" :key="d" class="d" :class="dayClass(d)">
            <b>{{ d }}</b>
            <div v-for="ev in eventsOf(d)" :key="ev.no + ev.type" class="ev">{{ typeText(ev.type) }} {{ ev.no }}</div>
          </div>
        </div>
      </div>
      <div class="card">
        <div class="card-title">价格与规则</div>
        <div v-if="price">
          <div class="metric"><span>日租金</span><b class="num">¥{{ money(price.dayPrice) }}</b></div>
          <div class="metric"><span>周租金</span><b class="num">¥{{ money(price.weekPrice) }}</b></div>
          <div class="metric"><span>月租金</span><b class="num">¥{{ money(price.monthPrice) }}</b></div>
          <div class="metric"><span>押金</span><b class="num">¥{{ money(price.deposit) }}</b></div>
          <div class="metric"><span>最短 / 最长</span><b>{{ price.minDays }} / {{ price.maxDays }} 天</b></div>
          <div class="metric"><span>逾期费</span><b>日租金 × {{ price.overdueRate }}</b></div>
          <div class="metric"><span>赠送配件</span><b>{{ price.giftAccessory }}</b></div>
        </div>
        <div class="card-title mt">占用明细</div>
        <el-table :data="events" size="small">
          <el-table-column label="类型" width="80">
            <template #default="{ row }">{{ typeText(row.type) }}</template>
          </el-table-column>
          <el-table-column prop="from" label="开始" width="104" />
          <el-table-column prop="to" label="结束" width="104" />
          <el-table-column prop="no" label="关联单据" min-width="140" />
        </el-table>
        <el-alert class="mt" type="info" :closable="false"
          title="押金未登记前不得出库；同一乐器的有效租赁、预留、保养与维修时段不得重叠。" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api'
import { money } from '../utils'

const options = ref([])
const instrumentId = ref(null)
const month = ref(new Date().toISOString().slice(0, 7))
const events = ref([])
const price = ref(null)
const days = ref(30)

const typeText = (t) => ({ RENTAL: '已租', REPAIR: '维修', MAINTAIN: '保养' }[t] || t)

function daysInMonth() {
  const [y, m] = month.value.split('-').map(Number)
  return new Date(y, m, 0).getDate()
}

function eventsOf(d) {
  const day = `${month.value}-${String(d).padStart(2, '0')}`
  return events.value.filter((e) => day >= e.from && day <= e.to)
}

function dayClass(d) {
  const list = eventsOf(d)
  if (list.some((e) => e.type === 'REPAIR')) return 'bad'
  if (list.some((e) => e.type === 'MAINTAIN')) return 'warn'
  if (list.length) return 'busy'
  return 'free'
}

async function load() {
  if (!instrumentId.value) return
  const res = await api.calendar({ instrumentId: instrumentId.value, month: month.value })
  events.value = res.events || []
  price.value = res.price
  days.value = daysInMonth()
}

onMounted(async () => {
  options.value = await api.instrumentOptions()
  if (options.value.length) {
    instrumentId.value = options.value[0].id
    load()
  }
})
</script>

<style scoped>
.cal { display: grid; grid-template-columns: repeat(7, 1fr); gap: 6px; }
.d { border: 1px solid #E2E7EC; border-radius: 5px; min-height: 76px; padding: 5px; font-size: 11px; background: #fff; }
.d.free { background: #F3FAF6; border-color: #CDE7D9; }
.d.busy { background: #EFF6F8; border-color: #BBD2DA; }
.d.warn { background: #FBF5E8; border-color: #E2CDA2; }
.d.bad { background: #FBEDEE; border-color: #E6C3C7; }
.d b { color: #63707E; font-weight: 500; }
.ev { margin-top: 3px; font-size: 10px; color: #33475B; background: rgba(255,255,255,.7); border-radius: 3px; padding: 1px 3px; }
.split { display: grid; grid-template-columns: 1fr 380px; gap: 12px; }
</style>
