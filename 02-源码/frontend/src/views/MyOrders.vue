<template>
  <div>
    <div class="pagehead">
      <div><h1>我的租赁</h1><p>查看租期进度，提交续租、报修、归还确认与评价</p></div>
      <div class="head-actions"><el-button type="primary" @click="$router.push('/catalog')">去租乐器</el-button></div>
    </div>

    <div class="card">
      <div class="filters">
        <el-select v-model="q.status" placeholder="订单状态" clearable>
          <el-option v-for="(v, k) in ORDER_STATUS" :key="k" :label="v.text" :value="k" />
        </el-select>
        <el-input v-model="q.keyword" placeholder="订单号" clearable style="width:200px" />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
    </div>

    <div class="card">
      <el-table :data="rows" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" min-width="170" />
        <el-table-column label="乐器" min-width="170">
          <template #default="{ row }">
            <div class="row-gap">
              <img :src="row.image" class="thumb" />
              <span>{{ row.instrumentName || '乐器 #' + row.instrumentId }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="租期" min-width="180">
          <template #default="{ row }">{{ row.startDate }} ~ {{ row.endDate }}</template>
        </el-table-column>
        <el-table-column label="租金" min-width="100">
          <template #default="{ row }"><span class="num">¥{{ money(row.rentAmount) }}</span></template>
        </el-table-column>
        <el-table-column label="押金" min-width="90">
          <template #default="{ row }"><el-tag :type="depositMeta(row.depositStatus).type" size="small">{{ depositMeta(row.depositStatus).text }}</el-tag></template>
        </el-table-column>
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }"><el-tag :type="orderMeta(row.status).type" size="small">{{ orderMeta(row.status).text }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="{ row }">
            <el-button link @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'RENTING'" link type="primary" @click="openRenew(row)">续租</el-button>
            <el-button v-if="row.status === 'RENTING'" link type="warning" @click="openRepair(row)">报修</el-button>
            <el-button v-if="row.status === 'COMPLETED'" link type="success" @click="openFeedback(row)">评价</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, prev, pager, next, sizes" :total="total"
        v-model:current-page="q.pageNum" v-model:page-size="q.pageSize" :page-sizes="[10,20,50]" @change="load" />
    </div>

    <el-drawer v-model="detailVisible" title="订单详情" size="560px">
      <template v-if="detail">
        <div class="notice">
          编号 {{ detail.order.orderNo }} · 状态 {{ orderMeta(detail.order.status).text }}<br />
          责任人 {{ detail.order.handlerName || '-' }} · 创建 {{ formatTime(detail.order.createTime) }} · 更新 {{ formatTime(detail.order.updateTime) }}
        </div>
        <el-descriptions :column="2" border size="small" class="mt">
          <el-descriptions-item label="乐器">{{ detail.instrument?.instrumentNo }} {{ detail.instrument?.name }}</el-descriptions-item>
          <el-descriptions-item label="租期">{{ detail.order.startDate }} ~ {{ detail.order.endDate }}</el-descriptions-item>
          <el-descriptions-item label="租金">¥{{ money(detail.order.rentAmount) }}</el-descriptions-item>
          <el-descriptions-item label="押金">¥{{ money(detail.order.depositAmount) }}</el-descriptions-item>
          <el-descriptions-item label="逾期费">¥{{ money(detail.order.overdueFee) }}</el-descriptions-item>
          <el-descriptions-item label="赔偿金">¥{{ money(detail.order.compensateAmount) }}</el-descriptions-item>
          <el-descriptions-item label="用途">{{ detail.order.purpose }}</el-descriptions-item>
          <el-descriptions-item label="取件">{{ detail.order.deliveryType }}</el-descriptions-item>
        </el-descriptions>

        <div class="card-title mt">状态流转</div>
        <el-timeline>
          <el-timeline-item v-for="(h, i) in detail.handovers" :key="'h' + i" :timestamp="formatTime(h.handoverTime)">
            {{ h.handoverType === 'OUT' ? '出库交接' : '归还交接' }} · {{ h.operator }}
          </el-timeline-item>
          <el-timeline-item v-for="(r, i) in detail.renewals" :key="'r' + i" :timestamp="formatTime(r.applyTime)">
            续租申请 → {{ renewMeta(r.status).text }}{{ r.conflictReason ? '（' + r.conflictReason + '）' : '' }}
          </el-timeline-item>
          <el-timeline-item v-for="(rp, i) in detail.repairs" :key="'p' + i" :timestamp="formatTime(rp.createTime)">
            维修 {{ rp.repairNo }} · {{ repairMeta(rp.status).text }}
          </el-timeline-item>
          <el-timeline-item v-for="(d, i) in detail.damages" :key="'d' + i" :timestamp="formatTime(d.createTime)">
            损坏赔偿 ¥{{ money(d.amount) }}（{{ d.liability }}）
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-drawer>

    <el-dialog v-model="renewVisible" title="申请续租" width="440px">
      <el-form label-width="96px">
        <el-form-item label="当前归还日">{{ renewRow?.endDate }}</el-form-item>
        <el-form-item label="新归还日">
          <el-date-picker v-model="renewForm.newEndDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="续租说明">
          <el-input v-model="renewForm.reason" type="textarea" :rows="3" placeholder="不少于 5 个字，如：考级延期，需要多租两周" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRenew">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="repairVisible" title="租期报修" width="460px">
      <el-form label-width="96px">
        <el-form-item label="故障描述">
          <el-input v-model="repairForm.faultDesc" type="textarea" :rows="3" placeholder="不少于 5 个字，描述现象与位置" />
        </el-form-item>
        <el-form-item label="故障照片">
          <el-upload :show-file-list="false" :http-request="uploadFault" accept="image/*">
            <img v-if="repairForm.images" :src="repairForm.images" class="thumb" style="width:96px;height:64px" />
            <el-button v-else size="small">上传照片</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="repairVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRepair">提交报修</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="feedbackVisible" title="服务评价" width="420px">
      <el-form label-width="80px">
        <el-form-item label="评分"><el-rate v-model="feedbackForm.rating" /></el-form-item>
        <el-form-item label="评价">
          <el-input v-model="feedbackForm.content" type="textarea" :rows="3" placeholder="乐器状况、交接与响应速度" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFeedback">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { ORDER_STATUS, depositMeta, formatTime, money, orderMeta, renewMeta, repairMeta } from '../utils'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const renewVisible = ref(false)
const repairVisible = ref(false)
const feedbackVisible = ref(false)
const renewRow = ref(null)
const repairRow = ref(null)
const feedbackRow = ref(null)
const renewForm = reactive({ newEndDate: '', reason: '' })
const repairForm = reactive({ faultDesc: '', images: '' })
const feedbackForm = reactive({ rating: 5, content: '' })
const q = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: '' })

async function load() {
  loading.value = true
  try {
    const res = await api.rentalPage(q)
    // 列表补充乐器名称，避免只显示 ID
    const opts = await api.instrumentOptions()
    const map = new Map(opts.map((o) => [o.id, o.label]))
    rows.value = res.records.map((r) => ({ ...r, instrumentName: map.get(r.instrumentId) || '' }))
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function search() {
  q.pageNum = 1
  load()
}

function reset() {
  Object.assign(q, { pageNum: 1, keyword: '', status: '' })
  load()
}

async function openDetail(row) {
  detail.value = await api.rentalDetail(row.id)
  detailVisible.value = true
}

function openRenew(row) {
  renewRow.value = row
  renewForm.newEndDate = ''
  renewForm.reason = ''
  renewVisible.value = true
}

async function submitRenew() {
  if (!renewForm.newEndDate) return ElMessage.warning('请选择新归还日')
  const res = await api.rentalRenew(renewRow.value.id, renewForm)
  ElMessage.success('续租申请已提交，等待门店审核')
  renewVisible.value = false
  if (res) load()
}

function openRepair(row) {
  repairRow.value = row
  repairForm.faultDesc = ''
  repairForm.images = ''
  repairVisible.value = true
}

async function uploadFault({ file }) {
  const res = await api.upload(file, 'repair')
  repairForm.images = res.url
}

async function submitRepair() {
  if ((repairForm.faultDesc || '').length < 5) return ElMessage.warning('故障描述不少于 5 个字')
  await api.rentalRepair(repairRow.value.id, repairForm)
  ElMessage.success('报修已提交，门店会尽快联系您')
  repairVisible.value = false
}

function openFeedback(row) {
  feedbackRow.value = row
  feedbackForm.rating = 5
  feedbackForm.content = ''
  feedbackVisible.value = true
}

async function submitFeedback() {
  await api.rentalFeedback(feedbackRow.value.id, feedbackForm)
  ElMessage.success('感谢您的评价')
  feedbackVisible.value = false
}

onMounted(load)
</script>

<style scoped>
.thumb { width: 44px; height: 34px; object-fit: cover; border-radius: 3px; background: #E9EEF2; }
.row-gap { display: flex; align-items: center; gap: 8px; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
