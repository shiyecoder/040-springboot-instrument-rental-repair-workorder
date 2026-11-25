<template>
  <div>
    <div class="pagehead">
      <div><h1>评分规则与计算留痕</h1><p>权重合计必须 100%，修改后记录版本与生效时间；原始结果不可覆盖，人工修正需留原因</p></div>
      <div class="head-actions"><el-button @click="load">刷新</el-button></div>
    </div>

    <div class="split">
      <div class="card">
        <div class="card-title">规则配置（当前 {{ current?.versionNo || '-' }}）</div>
        <el-form label-width="120px">
          <el-form-item label="外观评分权重"><el-input v-model="rule.weightAppearance" style="width:100px" /></el-form-item>
          <el-form-item label="音准评分权重"><el-input v-model="rule.weightPitch" style="width:100px" /></el-form-item>
          <el-form-item label="部件状态权重"><el-input v-model="rule.weightPart" style="width:100px" /></el-form-item>
          <el-form-item label="累计租期权重"><el-input v-model="rule.weightRentDays" style="width:100px" /></el-form-item>
          <el-form-item label="维修次数权重"><el-input v-model="rule.weightRepairCount" style="width:100px" /></el-form-item>
          <el-form-item label="保养间隔权重"><el-input v-model="rule.weightMaintain" style="width:100px" /></el-form-item>
          <el-form-item label="权重合计"><b :style="{ color: weightSum === 100 ? '#2B7955' : '#B02A37' }">{{ weightSum }}%</b></el-form-item>
          <el-form-item label="出租阈值"><el-input v-model="rule.threshold" style="width:100px" /> <span class="hint">分，低于阈值禁止出租</span></el-form-item>
          <el-form-item label="生效说明"><el-input v-model="rule.remark" placeholder="不少于 5 个字" /></el-form-item>
        </el-form>
        <el-button type="primary" @click="publish">保存为新版本</el-button>

        <div class="card-title mt">版本历史</div>
        <el-table :data="versions" size="small">
          <el-table-column prop="versionNo" label="版本" width="80" />
          <el-table-column label="生效时间" min-width="150">
            <template #default="{ row }">{{ formatTime(row.effectTime) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }"><el-tag :type="row.enabled === 1 ? 'success' : 'info'" size="small">{{ row.enabled === 1 ? '启用' : '归档' }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="remark" label="说明" min-width="180" />
        </el-table>
      </div>

      <div class="card">
        <div class="card-title">效果看板</div>
        <div class="metric"><span>累计计算</span><b>{{ effect.total }} 次</b></div>
        <div class="metric"><span>推荐采纳率</span><b>{{ effect.adoptRate }}%</b></div>
        <div class="metric"><span>人工调整率</span><b>{{ effect.adjustRate }}%</b></div>
        <div class="metric" v-for="(v, k) in effect.dist || {}" :key="k">
          <span>{{ scoreMeta(k).text }}</span><b>{{ v }} 条</b>
        </div>
        <p class="hint mt">修改权重只影响新计算或主动重算的数据，历史留痕保留原规则版本。</p>
      </div>
    </div>

    <div class="card">
      <div class="card-title">计算留痕</div>
      <div class="filters" style="margin-bottom:10px">
        <el-input v-model="q.instrumentId" placeholder="乐器 ID" clearable style="width:140px" />
        <el-select v-model="q.level" placeholder="等级" clearable style="width:160px">
          <el-option v-for="(v, k) in SCORE_LEVEL" :key="k" :label="v.text" :value="k" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
      </div>
      <el-table :data="records" v-loading="loading">
        <el-table-column prop="instrumentId" label="乐器" width="80" />
        <el-table-column prop="ruleVersion" label="规则版本" width="90" />
        <el-table-column label="总分" width="80"><template #default="{ row }"><b class="num">{{ row.totalScore }}</b></template></el-table-column>
        <el-table-column label="等级" width="106">
          <template #default="{ row }"><el-tag :type="scoreMeta(row.level).type" size="small">{{ scoreMeta(row.level).text }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="mainFactors" label="主要影响因素" min-width="160" />
        <el-table-column prop="improve" label="可改进项" min-width="200" />
        <el-table-column label="人工修正" width="130">
          <template #default="{ row }">{{ row.manualResult ? scoreMeta(row.manualResult).text : '-' }}</template>
        </el-table-column>
        <el-table-column label="计算时间" min-width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="{ row }"><el-button link type="primary" @click="openDetail(row)">明细</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, prev, pager, next" :total="total"
        v-model:current-page="q.pageNum" v-model:page-size="q.pageSize" @change="loadRecords" />
    </div>

    <el-dialog v-model="detailVisible" title="评分明细与人工修正" width="620px">
      <template v-if="detailRow">
        <div class="notice">
          乐器 #{{ detailRow.instrumentId }} · 规则 {{ detailRow.ruleVersion }} · 总分 {{ detailRow.totalScore }} ·
          {{ scoreMeta(detailRow.level).text }} · {{ formatTime(detailRow.createTime) }}
        </div>
        <el-table :data="detailRows" size="small" class="mt">
          <el-table-column prop="name" label="因素" width="100" />
          <el-table-column prop="input" label="输入值" width="90" />
          <el-table-column prop="weight" label="权重" width="80" />
          <el-table-column prop="score" label="得分" width="80" />
          <el-table-column prop="weighted" label="加权得分" width="100" />
        </el-table>
        <el-alert class="mt" type="warning" :closable="false"
          :title="'未满足条件：' + (detailRow.unmet || '无') + '；可改进项：' + detailRow.improve" />
        <div class="mt" style="display:flex;gap:8px">
          <el-select v-model="adjustResult" placeholder="人工修正结果" style="width:160px">
            <el-option label="保持原结果" value="" />
            <el-option v-for="(v, k) in SCORE_LEVEL" :key="k" :label="v.text" :value="k" />
          </el-select>
          <el-input v-model="adjustReason" placeholder="修正原因不少于 5 个字" style="flex:1" />
          <el-button @click="submitAdjust">保存修正</el-button>
        </div>
        <div v-if="detailRow.manualResult" class="hint mt">
          已修正为 {{ scoreMeta(detailRow.manualResult).text }}：{{ detailRow.adjustReason }}（{{ detailRow.adjustBy }} · {{ formatTime(detailRow.adjustTime) }}）
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { SCORE_LEVEL, formatTime, scoreMeta } from '../utils'

const versions = ref([])
const current = ref(null)
const effect = reactive({ total: 0, adoptRate: 0, adjustRate: 0, dist: {} })
const records = ref([])
const total = ref(0)
const loading = ref(false)
const detailVisible = ref(false)
const detailRow = ref(null)
const adjustResult = ref('')
const adjustReason = ref('')
const q = reactive({ pageNum: 1, pageSize: 10, instrumentId: '', level: '' })
const rule = reactive({ weightAppearance: 20, weightPitch: 25, weightPart: 20, weightRentDays: 15, weightRepairCount: 10, weightMaintain: 10, threshold: 60, remark: '' })

const weightSum = computed(() => ['weightAppearance', 'weightPitch', 'weightPart', 'weightRentDays', 'weightRepairCount', 'weightMaintain']
  .reduce((s, k) => s + Number(rule[k] || 0), 0))

const detailRows = computed(() => {
  try {
    return Object.values(JSON.parse(detailRow.value?.detailJson || '{}'))
  } catch (e) {
    return []
  }
})

async function load() {
  versions.value = await api.ruleList()
  current.value = await api.ruleCurrent()
  Object.assign(rule, {
    weightAppearance: current.value.weightAppearance, weightPitch: current.value.weightPitch,
    weightPart: current.value.weightPart, weightRentDays: current.value.weightRentDays,
    weightRepairCount: current.value.weightRepairCount, weightMaintain: current.value.weightMaintain,
    threshold: current.value.threshold, remark: ''
  })
  Object.assign(effect, await api.scoreEffect())
  loadRecords()
}

function search() {
  q.pageNum = 1
  loadRecords()
}

async function loadRecords() {
  loading.value = true
  try {
    const res = await api.scorePage(q)
    records.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function publish() {
  if (weightSum.value !== 100) return ElMessage.warning('权重合计必须等于 100%')
  if ((rule.remark || '').length < 5) return ElMessage.warning('生效说明不少于 5 个字')
  await api.rulePublish({ ...rule })
  ElMessage.success('已发布新版本，只影响后续计算')
  load()
}

function openDetail(row) {
  detailRow.value = row
  adjustResult.value = row.manualResult || ''
  adjustReason.value = ''
  detailVisible.value = true
}

async function submitAdjust() {
  if ((adjustReason.value || '').length < 5) return ElMessage.warning('修正原因不少于 5 个字')
  await api.scoreAdjust(detailRow.value.id, { manualResult: adjustResult.value, reason: adjustReason.value })
  ElMessage.success('人工修正已记录，原始计算未被覆盖')
  detailVisible.value = false
  load()
}

onMounted(load)
</script>

<style scoped>
.split { display: grid; grid-template-columns: 1.35fr 0.65fr; gap: 12px; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
