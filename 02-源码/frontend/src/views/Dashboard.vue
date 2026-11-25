<template>
  <div class="dash">
    <div class="pagehead">
      <div>
        <h1>资产经营看板</h1>
        <p>统计区间 {{ rangeText }}，默认最近 30 天</p>
      </div>
      <div class="head-actions">
        <el-radio-group v-model="range" size="small" @change="load">
          <el-radio-button value="30">近 30 天</el-radio-button>
          <el-radio-button value="month">本月</el-radio-button>
          <el-radio-button value="quarter">本季度</el-radio-button>
          <el-radio-button value="year">本年</el-radio-button>
        </el-radio-group>
        <el-button size="small" @click="load">刷新</el-button>
      </div>
    </div>

    <div class="kpis">
      <div class="kpi"><div class="lb">可租率</div><div class="vl">{{ data.kpi?.availableRate ?? 0 }}%</div><div class="dt">可租 / 在册</div></div>
      <div class="kpi"><div class="lb">租赁次数</div><div class="vl">{{ data.kpi?.rentCount ?? 0 }}</div><div class="dt">区间内有效单</div></div>
      <div class="kpi"><div class="lb">逾期数</div><div class="vl" style="color:#B02A37">{{ data.kpi?.overdue ?? 0 }}</div><div class="dt">租用中已过归还日</div></div>
      <div class="kpi"><div class="lb">维修中</div><div class="vl">{{ data.kpi?.repairing ?? 0 }}</div><div class="dt">未关闭工单</div></div>
      <div class="kpi"><div class="lb">平均状态分</div><div class="vl">{{ data.kpi?.avgScore ?? 0 }}</div><div class="dt">创新点规则 v3</div></div>
      <div class="kpi"><div class="lb">收入登记</div><div class="vl">¥{{ money(data.kpi?.income) }}</div><div class="dt">线下收取合计</div></div>
    </div>

    <div class="charts">
      <div class="card chart-card"><div class="card-title">近 10 天租赁单量</div>
        <EChart :option="trendOption" :height="196" /></div>
      <div class="card chart-card"><div class="card-title">乐器状态分布</div>
        <EChart :option="statusOption" :height="196" /></div>
      <div class="card chart-card"><div class="card-title">类别租赁次数</div>
        <EChart :option="categoryOption" :height="196" /></div>
      <div class="card chart-card"><div class="card-title">责任人处理量</div>
        <EChart :option="staffOption" :height="196" /></div>
      <div class="card chart-card"><div class="card-title">高频故障分布</div>
        <EChart :option="faultOption" :height="196" /></div>
      <div class="card chart-card"><div class="card-title">状态评分结果分布</div>
        <EChart :option="scoreOption" :height="196" /></div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import EChart from '../components/EChart.vue'
import { api } from '../api'
import { CHART_COLORS, money } from '../utils'

const router = useRouter()
const range = ref('30')
const data = reactive({ kpi: {}, trend: [], statusDist: [], byCategory: [], byStaff: [], byFault: [], byScore: [] })

const rangeText = computed(() => ({ 30: '最近 30 天', month: '本月', quarter: '本季度', year: '本年' }[range.value]))

const axis = {
  grid: { left: 8, right: 12, top: 24, bottom: 4, containLabel: true },
  tooltip: { trigger: 'axis' }
}

const trendOption = computed(() => ({
  ...axis,
  xAxis: { type: 'category', data: data.trend.map((i) => i.name), axisLabel: { fontSize: 10 }, axisLine: { lineStyle: { color: '#E2E7EC' } } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#F0F3F6' } }, axisLabel: { fontSize: 10 } },
  series: [{ type: 'line', smooth: true, data: data.trend.map((i) => i.value), itemStyle: { color: CHART_COLORS[0] },
    areaStyle: { color: 'rgba(25,50,74,.08)' } }]
}))

const statusOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 11 } },
  color: CHART_COLORS,
  series: [{ type: 'pie', radius: ['42%', '66%'], center: ['50%', '42%'], label: { show: false },
    data: data.statusDist.map((i) => ({ name: i.name, value: i.value })) }]
}))

const categoryOption = computed(() => ({
  ...axis,
  xAxis: { type: 'category', data: data.byCategory.map((i) => i.name), axisLabel: { fontSize: 10, interval: 0 } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#F0F3F6' } }, axisLabel: { fontSize: 10 } },
  series: [{ type: 'bar', barWidth: 18, data: data.byCategory.map((i) => i.value), itemStyle: { color: CHART_COLORS[1], borderRadius: [3, 3, 0, 0] } }]
}))

const staffOption = computed(() => ({
  ...axis,
  xAxis: { type: 'value', splitLine: { lineStyle: { color: '#F0F3F6' } }, axisLabel: { fontSize: 10 } },
  yAxis: { type: 'category', data: data.byStaff.map((i) => i.name), axisLabel: { fontSize: 10 } },
  series: [{ type: 'bar', barWidth: 12, data: data.byStaff.map((i) => i.value), itemStyle: { color: CHART_COLORS[2], borderRadius: [0, 3, 3, 0] } }]
}))

const faultOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, itemWidth: 8, itemHeight: 8, textStyle: { fontSize: 11 } },
  color: CHART_COLORS,
  series: [{ type: 'pie', radius: '62%', center: ['50%', '42%'], label: { fontSize: 10, formatter: '{b}\n{c}' },
    data: data.byFault.map((i) => ({ name: i.name, value: i.value })) }]
}))

const scoreOption = computed(() => ({
  ...axis,
  xAxis: { type: 'category', data: data.byScore.map((i) => i.name), axisLabel: { fontSize: 10 } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#F0F3F6' } }, axisLabel: { fontSize: 10 } },
  series: [{ type: 'bar', barWidth: 24, data: data.byScore.map((i) => i.value),
    itemStyle: { color: (p) => ['#2B7955', '#356A7A', '#8A5B10', '#B02A37'][p.dataIndex] || CHART_COLORS[0], borderRadius: [3, 3, 0, 0] } }]
}))

async function load() {
  const res = await api.dashboard(range.value)
  Object.assign(data, res)
}

onMounted(load)
</script>

<style scoped>
.dash { display: flex; flex-direction: column; }
.charts { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.chart-card { padding: 12px 14px; }
.chart-card :deep(.card-title) { margin-bottom: 4px; font-size: 13px; }
.kpis { margin-bottom: 12px; }
</style>
