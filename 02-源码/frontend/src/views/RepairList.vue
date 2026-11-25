<template>
  <div>
    <div class="pagehead">
      <div><h1>维修工单</h1><p>接单 → 检测报价 → 零件领用 → 维修 → 试奏检验 → 完工验收；同屏展示状态评分依据</p></div>
      <div class="head-actions">
        <el-select v-model="q.status" placeholder="工单状态" clearable style="width:160px" @change="search">
          <el-option v-for="(v, k) in REPAIR_STATUS" :key="k" :label="v.text" :value="k" />
        </el-select>
        <el-input v-model="q.keyword" placeholder="工单号 / 故障" clearable style="width:180px" />
        <el-button type="primary" @click="search">查询</el-button>
      </div>
    </div>

    <div class="card">
      <el-table :data="rows" v-loading="loading">
        <el-table-column label="工单号" min-width="170"><template #default="{ row }">{{ row.repair.repairNo }}</template></el-table-column>
        <el-table-column label="乐器" min-width="170">
          <template #default="{ row }">{{ row.instrumentNo }} {{ row.instrumentName }}</template>
        </el-table-column>
        <el-table-column label="故障" min-width="200"><template #default="{ row }">{{ row.repair.faultDesc }}</template></el-table-column>
        <el-table-column label="预估/实际" width="140">
          <template #default="{ row }"><span class="num">¥{{ money(row.repair.estimatedCost) }} / {{ money(row.repair.actualCost) }}</span></template>
        </el-table-column>
        <el-table-column label="技师" width="100"><template #default="{ row }">{{ row.repair.technicianName || '-' }}</template></el-table-column>
        <el-table-column label="状态" width="104">
          <template #default="{ row }"><el-tag :type="repairMeta(row.repair.status).type" size="small">{{ repairMeta(row.repair.status).text }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }"><el-button link type="primary" @click="open(row.repair)">处理</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, prev, pager, next, sizes" :total="total"
        v-model:current-page="q.pageNum" v-model:page-size="q.pageSize" :page-sizes="[10,20,50]" @change="load" />
    </div>

    <el-drawer v-model="visible" title="维修生命周期" size="620px">
      <template v-if="detail">
        <div class="notice">
          {{ detail.repair.repairNo }} · {{ repairMeta(detail.repair.status).text }} ·
          {{ detail.instrument?.instrumentNo }} {{ detail.instrument?.name }}<br />
          来源 {{ detail.repair.source === 'RENTAL' ? '租期报修' : '门店送修' }} · 技师 {{ detail.repair.technicianName || '-' }}
        </div>

        <div class="card-title mt">处理记录</div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="故障">{{ detail.repair.faultDesc }}</el-descriptions-item>
          <el-descriptions-item label="检测结论">{{ detail.repair.diagnosis || '-' }}</el-descriptions-item>
          <el-descriptions-item label="预估费用">¥{{ money(detail.repair.estimatedCost) }}</el-descriptions-item>
          <el-descriptions-item label="实际费用">¥{{ money(detail.repair.actualCost) }}</el-descriptions-item>
          <el-descriptions-item label="实际处理">{{ detail.repair.handleDesc || '-' }}</el-descriptions-item>
          <el-descriptions-item label="试奏检验">{{ detail.repair.testResult || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="card-title mt">零件领用</div>
        <el-table :data="detail.parts" size="small">
          <el-table-column prop="partName" label="零件" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="单价" width="100"><template #default="{ row }">¥{{ money(row.unitPrice) }}</template></el-table-column>
          <el-table-column label="金额" width="100"><template #default="{ row }">¥{{ money(row.amount) }}</template></el-table-column>
        </el-table>

        <div class="card-title mt">乐器状态评分（创新点）</div>
        <div v-if="score">
          <div class="score-hero">
            <div class="ring" :style="{ background: ringBg }"><span>{{ score.totalScore }}</span></div>
            <div>
              <div style="font-size:14px;font-weight:600">{{ scoreMeta(score.level).text }}</div>
              <div class="hint">规则 {{ score.ruleVersion }} · {{ formatTime(score.createTime) }}</div>
              <div class="hint">{{ score.recommend }}</div>
            </div>
            <el-button size="small" @click="evaluate">重新计算</el-button>
          </div>
          <el-table :data="scoreRows" size="small" style="margin-top:10px">
            <el-table-column prop="name" label="因素" width="100" />
            <el-table-column prop="input" label="输入值" width="90" />
            <el-table-column prop="weight" label="权重" width="80" />
            <el-table-column prop="score" label="得分" width="80" />
            <el-table-column prop="weighted" label="加权" width="80" />
          </el-table>
          <el-alert class="mt" type="warning" :closable="false"
            :title="'主要影响因素：' + score.mainFactors + '；未满足条件：' + (score.unmet || '无') + '；可改进项：' + score.improve" />
          <div class="mt" style="display:flex;gap:8px;align-items:center">
            <el-select v-model="adjustResult" placeholder="人工修正" style="width:160px">
              <el-option label="保持原结果" value="" />
              <el-option v-for="(v, k) in SCORE_LEVEL" :key="k" :label="v.text" :value="k" />
            </el-select>
            <el-input v-model="adjustReason" placeholder="修正原因不少于 5 个字" style="flex:1" />
            <el-button @click="submitAdjust">保存修正</el-button>
          </div>
          <div v-if="score.manualResult" class="hint mt">
            已人工修正为 {{ scoreMeta(score.manualResult).text }}：{{ score.adjustReason }}（{{ score.adjustBy }} · {{ formatTime(score.adjustTime) }}）
          </div>
        </div>
        <div v-else class="hint">尚未计算评分，点击下方按钮生成</div>

        <div class="mt" style="display:flex;gap:8px;flex-wrap:wrap">
          <el-button v-if="can('PENDING')" type="primary" @click="accept">接单</el-button>
          <el-button v-if="can('DIAGNOSING')" type="primary" @click="diagnoseVisible = true">填写检测与报价</el-button>
          <el-button v-if="can('REPAIRING')" @click="partVisible = true">领用零件</el-button>
          <el-button v-if="can('REPAIRING')" type="primary" @click="finishVisible = true">完成维修</el-button>
          <el-button v-if="can('TESTING')" type="success" @click="closeOrder">完工验收</el-button>
          <el-button v-if="!score" @click="evaluate">计算状态评分</el-button>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="diagnoseVisible" title="检测与报价" width="460px">
      <el-form label-width="90px">
        <el-form-item label="检测结论"><el-input v-model="diagnoseForm.diagnosis" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="预估费用"><el-input v-model="diagnoseForm.estimatedCost" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="diagnoseVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDiagnose">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="partVisible" title="零件领用" width="440px">
      <el-form label-width="90px">
        <el-form-item label="零件名称"><el-input v-model="partForm.partName" /></el-form-item>
        <el-form-item label="数量"><el-input v-model="partForm.quantity" /></el-form-item>
        <el-form-item label="单价"><el-input v-model="partForm.unitPrice" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="partVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPart">登记领用</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="finishVisible" title="完成维修" width="480px">
      <el-form label-width="96px">
        <el-form-item label="实际处理"><el-input v-model="finishForm.handleDesc" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="试奏检验"><el-input v-model="finishForm.testResult" placeholder="如：各弦音准稳定，无杂音" /></el-form-item>
        <el-form-item label="实际费用"><el-input v-model="finishForm.actualCost" placeholder="留空则按零件合计" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finishVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFinish">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { REPAIR_STATUS, SCORE_LEVEL, formatTime, money, repairMeta, scoreMeta } from '../utils'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const visible = ref(false)
const detail = ref(null)
const score = ref(null)
const adjustResult = ref('')
const adjustReason = ref('')
const diagnoseVisible = ref(false)
const partVisible = ref(false)
const finishVisible = ref(false)
const q = reactive({ pageNum: 1, pageSize: 10, status: '', keyword: '' })
const diagnoseForm = reactive({ diagnosis: '', estimatedCost: 0 })
const partForm = reactive({ partName: '', quantity: 1, unitPrice: 0 })
const finishForm = reactive({ handleDesc: '', testResult: '', actualCost: '' })

const can = (st) => detail.value?.repair.status === st
const scoreRows = computed(() => {
  if (!score.value?.detailJson) return []
  try {
    return Object.values(JSON.parse(score.value.detailJson))
  } catch (e) {
    return []
  }
})
const ringBg = computed(() => {
  const v = Number(score.value?.totalScore || 0)
  const color = v >= 85 ? '#2B7955' : v >= 60 ? '#356A7A' : '#B02A37'
  return `conic-gradient(${color} 0 ${v}%, #E6EBEF ${v}% 100%)`
})

async function load() {
  loading.value = true
  try {
    const res = await api.repairPage(q)
    rows.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function search() {
  q.pageNum = 1
  load()
}

async function open(row) {
  detail.value = await api.repairDetail(row.id)
  score.value = await api.scoreByInstrument(detail.value.repair.instrumentId)
  adjustResult.value = score.value?.manualResult || ''
  adjustReason.value = ''
  visible.value = true
}

async function accept() {
  await api.repairAccept(detail.value.repair.id)
  ElMessage.success('已接单')
  await open(detail.value.repair)
  load()
}

async function submitDiagnose() {
  await api.repairDiagnose(detail.value.repair.id, diagnoseForm)
  ElMessage.success('检测已提交，进入维修')
  diagnoseVisible.value = false
  await open(detail.value.repair)
  load()
}

async function submitPart() {
  await api.repairPart(detail.value.repair.id, partForm)
  ElMessage.success('零件已登记')
  partVisible.value = false
  await open(detail.value.repair)
}

async function submitFinish() {
  await api.repairFinish(detail.value.repair.id, finishForm)
  ElMessage.success('维修完成，待验收')
  finishVisible.value = false
  await open(detail.value.repair)
  load()
}

async function closeOrder() {
  await api.repairClose(detail.value.repair.id)
  ElMessage.success('工单已关闭，乐器回到可租')
  await open(detail.value.repair)
  load()
}

async function evaluate() {
  const res = await api.evaluate(detail.value.repair.instrumentId)
  score.value = res.record
  ElMessage.success('已完成计算，留痕已保存')
}

async function submitAdjust() {
  if ((adjustReason.value || '').length < 5) return ElMessage.warning('修正原因不少于 5 个字')
  await api.scoreAdjust(score.value.id, { manualResult: adjustResult.value, reason: adjustReason.value })
  ElMessage.success('人工修正已记录，原始结果未覆盖')
  score.value = await api.scoreDetail(score.value.id)
}

onMounted(load)
</script>

<style scoped>
.pager { margin-top: 14px; justify-content: flex-end; }
.score-hero { display: flex; gap: 18px; align-items: center; }
.ring { width: 92px; height: 92px; border-radius: 50%; display: grid; place-items: center; }
.ring span { width: 70px; height: 70px; border-radius: 50%; background: #fff; display: grid; place-items: center;
  font-size: 22px; font-weight: 600; color: #19324A; }
</style>
