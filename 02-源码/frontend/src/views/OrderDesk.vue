<template>
  <div>
    <div class="pagehead">
      <div><h1>租赁订单台</h1><p>待办优先、更新时间倒序；审核预约、登记押金、办理交接与归还验收</p></div>
      <div class="head-actions">
        <el-button @click="load">刷新</el-button>
        <el-button type="primary" @click="exportCurrent">导出当前筛选</el-button>
      </div>
    </div>

    <div class="card">
      <div class="filters">
        <el-radio-group v-model="q.tab" @change="search">
          <el-radio-button value="TODO">待办</el-radio-button>
          <el-radio-button value="DOING">处理中</el-radio-button>
          <el-radio-button value="DONE">已完成</el-radio-button>
          <el-radio-button value="ABNORMAL">异常</el-radio-button>
          <el-radio-button value="">全部</el-radio-button>
        </el-radio-group>
        <el-select v-model="q.status" placeholder="状态" clearable>
          <el-option v-for="(v, k) in ORDER_STATUS" :key="k" :label="v.text" :value="k" />
        </el-select>
        <el-input v-model="q.keyword" placeholder="订单号 / 客户" clearable />
        <el-button type="primary" @click="search">查询</el-button>
      </div>
    </div>

    <div class="card">
      <div class="hint" style="margin-bottom:10px">当前待办 {{ todoCount }} 条，共 {{ total }} 条记录</div>
      <el-table :data="rows" v-loading="loading" @selection-change="(v) => (selected = v)">
        <el-table-column type="selection" width="42" />
        <el-table-column prop="orderNo" label="订单号" min-width="168" />
        <el-table-column prop="customerName" label="客户" min-width="90" />
        <el-table-column label="乐器" min-width="150">
          <template #default="{ row }">{{ instLabel(row.instrumentId) }}</template>
        </el-table-column>
        <el-table-column label="租期" min-width="176">
          <template #default="{ row }">{{ row.startDate }} ~ {{ row.endDate }}</template>
        </el-table-column>
        <el-table-column label="金额" min-width="110">
          <template #default="{ row }"><span class="num">¥{{ money(row.rentAmount) }}</span></template>
        </el-table-column>
        <el-table-column label="押金" width="92">
          <template #default="{ row }"><el-tag :type="depositMeta(row.depositStatus).type" size="small">{{ depositMeta(row.depositStatus).text }}</el-tag></template>
        </el-table-column>
        <el-table-column label="状态" width="112">
          <template #default="{ row }"><el-tag :type="orderMeta(row.status).type" size="small">{{ orderMeta(row.status).text }}</el-tag></template>
        </el-table-column>
        <el-table-column label="更新时间" min-width="152">
          <template #default="{ row }">{{ formatTime(row.updateTime) }}</template>
        </el-table-column>
        <el-table-column label="待办操作" min-width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING_AUDIT'" link type="primary" @click="openAudit(row)">审核</el-button>
            <el-button v-if="row.status === 'PENDING_DEPOSIT'" link type="primary" @click="openDeposit(row)">登记押金</el-button>
            <el-button v-if="row.status === 'PENDING_HANDOVER'" link type="primary" @click="openHandover(row)">出库交接</el-button>
            <el-button v-if="row.status === 'RENTING'" link type="primary" @click="openReturn(row)">归还验收</el-button>
            <el-button v-if="row.status === 'PENDING_RETURN'" link type="danger" @click="openDamage(row)">赔偿处理</el-button>
            <el-button link @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, prev, pager, next, sizes" :total="total"
        v-model:current-page="q.pageNum" v-model:page-size="q.pageSize" :page-sizes="[10,20,50]" @change="load" />
    </div>

    <el-drawer v-model="detailVisible" title="订单详情" size="600px">
      <template v-if="detail">
        <div class="notice">
          {{ detail.order.orderNo }} · {{ orderMeta(detail.order.status).text }} ·
          责任人 {{ detail.order.handlerName || '-' }} · 创建 {{ formatTime(detail.order.createTime) }} · 更新 {{ formatTime(detail.order.updateTime) }}
        </div>
        <el-descriptions :column="2" border size="small" class="mt">
          <el-descriptions-item label="客户">{{ detail.order.customerName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detail.order.idProof }}</el-descriptions-item>
          <el-descriptions-item label="乐器">{{ detail.instrument?.instrumentNo }} {{ detail.instrument?.name }}</el-descriptions-item>
          <el-descriptions-item label="租期">{{ detail.order.startDate }} ~ {{ detail.order.endDate }}</el-descriptions-item>
          <el-descriptions-item label="租金">¥{{ money(detail.order.rentAmount) }}</el-descriptions-item>
          <el-descriptions-item label="押金">¥{{ money(detail.order.depositAmount) }}</el-descriptions-item>
          <el-descriptions-item label="逾期费">¥{{ money(detail.order.overdueFee) }}</el-descriptions-item>
          <el-descriptions-item label="赔偿金">¥{{ money(detail.order.compensateAmount) }}</el-descriptions-item>
          <el-descriptions-item label="用途" :span="2">{{ detail.order.purpose }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.order.remark }}</el-descriptions-item>
        </el-descriptions>

        <div class="card-title mt">交接记录</div>
        <el-table :data="detail.handovers" size="small">
          <el-table-column label="类型" width="86">
            <template #default="{ row }">{{ row.handoverType === 'OUT' ? '出库' : '归还' }}</template>
          </el-table-column>
          <el-table-column prop="appearanceCheck" label="外观" min-width="120" />
          <el-table-column prop="accessoryList" label="配件" min-width="120" />
          <el-table-column label="时间" min-width="150">
            <template #default="{ row }">{{ formatTime(row.handoverTime) }}</template>
          </el-table-column>
        </el-table>

        <div class="card-title mt">归还验收</div>
        <el-table :data="detail.inspections" size="small">
          <el-table-column prop="result" label="结论" width="90" />
          <el-table-column prop="overdueDays" label="逾期天数" width="90" />
          <el-table-column prop="overdueFee" label="逾期费" width="90" />
          <el-table-column prop="damageDesc" label="损坏描述" min-width="140" />
          <el-table-column label="时间" min-width="150">
            <template #default="{ row }">{{ formatTime(row.inspectTime) }}</template>
          </el-table-column>
        </el-table>

        <div class="card-title mt">关联维修</div>
        <el-table :data="detail.repairs" size="small">
          <el-table-column prop="repairNo" label="工单号" min-width="150" />
          <el-table-column prop="faultDesc" label="故障" min-width="150" />
          <el-table-column label="状态" width="96">
            <template #default="{ row }">{{ repairMeta(row.status).text }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-drawer>

    <el-dialog v-model="auditVisible" title="审核预约" width="460px">
      <el-form label-width="86px">
        <el-form-item label="订单">{{ current?.orderNo }}</el-form-item>
        <el-form-item label="乐器">{{ current ? instLabel(current.instrumentId) : '' }}</el-form-item>
        <el-form-item label="结果">
          <el-radio-group v-model="auditForm.pass">
            <el-radio :value="true">通过</el-radio>
            <el-radio :value="false">退回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!auditForm.pass" label="退回原因">
          <el-input v-model="auditForm.reason" type="textarea" :rows="3" placeholder="不少于 5 个字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="depositVisible" title="线下押金登记" width="460px">
      <el-form label-width="96px">
        <el-form-item label="应收押金">¥{{ money(current?.depositAmount) }}</el-form-item>
        <el-form-item label="应收租金">¥{{ money(current?.rentAmount) }}</el-form-item>
        <el-form-item label="实收金额"><el-input v-model="depositForm.amount" /></el-form-item>
        <el-form-item label="收取方式">
          <el-select v-model="depositForm.method" style="width:100%">
            <el-option label="线下现金" value="线下现金" /><el-option label="线下刷卡" value="线下刷卡" />
            <el-option label="线下转账" value="线下转账" />
          </el-select>
        </el-form-item>
        <el-form-item label="收款凭证">
          <el-upload :show-file-list="false" :http-request="uploadProof" accept="image/*">
            <img v-if="depositForm.proof" :src="depositForm.proof" class="thumb" style="width:96px;height:64px" />
            <el-button v-else size="small">上传凭证</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="depositVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDeposit">确认登记</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="handoverVisible" title="出库交接" width="520px">
      <el-form label-width="96px">
        <el-form-item label="外观检查"><el-input v-model="handoverForm.appearance" /></el-form-item>
        <el-form-item label="功能检查"><el-input v-model="handoverForm.function" /></el-form-item>
        <el-form-item label="编号核对"><el-input v-model="handoverForm.instrumentNoCheck" /></el-form-item>
        <el-form-item label="配件清单"><el-input v-model="handoverForm.accessories" /></el-form-item>
        <el-form-item label="已有划痕"><el-input v-model="handoverForm.scratches" /></el-form-item>
        <el-form-item label="交接照片">
          <el-upload :show-file-list="false" :http-request="uploadHandover" accept="image/*">
            <img v-if="handoverForm.photos" :src="handoverForm.photos" class="thumb" style="width:96px;height:64px" />
            <el-button v-else size="small">上传照片</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handoverVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandover">完成交接</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="returnVisible" title="归还验收" width="520px">
      <el-form label-width="96px">
        <el-form-item label="归还日期">
          <el-date-picker v-model="returnForm.returnDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="功能检查">
          <el-select v-model="returnForm.functionCheck" style="width:100%">
            <el-option label="正常" value="正常" /><el-option label="异常" value="异常" />
          </el-select>
        </el-form-item>
        <el-form-item label="外观检查">
          <el-select v-model="returnForm.appearanceCheck" style="width:100%">
            <el-option label="正常" value="正常" /><el-option label="新增损坏" value="新增损坏" />
          </el-select>
        </el-form-item>
        <el-form-item label="配件检查">
          <el-select v-model="returnForm.accessoryCheck" style="width:100%">
            <el-option label="齐全" value="齐全" /><el-option label="缺失" value="缺失" />
          </el-select>
        </el-form-item>
        <el-form-item label="清洁情况">
          <el-select v-model="returnForm.cleanCheck" style="width:100%">
            <el-option label="已清洁" value="已清洁" /><el-option label="需清洁" value="需清洁" />
          </el-select>
        </el-form-item>
        <el-form-item label="验收结论">
          <el-select v-model="returnForm.result" style="width:100%">
            <el-option label="通过，转入可租" value="PASS" />
            <el-option label="不通过，转损坏赔偿" value="DAMAGE" />
            <el-option label="转维修工单" value="REPAIR" />
          </el-select>
        </el-form-item>
        <el-form-item label="损坏描述"><el-input v-model="returnForm.damageDesc" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReturn">提交验收</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="damageVisible" title="损坏赔偿处理" width="500px">
      <el-form label-width="96px">
        <el-form-item label="损坏点"><el-input v-model="damageForm.damagePoint" /></el-form-item>
        <el-form-item label="缺失配件"><el-input v-model="damageForm.missingAccessory" /></el-form-item>
        <el-form-item label="维修成本"><el-input v-model="damageForm.repairCost" /></el-form-item>
        <el-form-item label="责任结论">
          <el-select v-model="damageForm.liability" style="width:100%">
            <el-option label="客户责任" value="客户责任" /><el-option label="自然损耗" value="自然损耗" />
            <el-option label="门店责任" value="门店责任" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明"><el-input v-model="damageForm.conclusion" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="damageVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDamage">保存并登记赔偿</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { ORDER_STATUS, depositMeta, formatTime, money, orderMeta, repairMeta } from '../utils'

const rows = ref([])
const total = ref(0)
const todoCount = ref(0)
const loading = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const auditVisible = ref(false)
const depositVisible = ref(false)
const handoverVisible = ref(false)
const returnVisible = ref(false)
const damageVisible = ref(false)
const current = ref(null)
const options = ref([])
const selected = ref([])

const q = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: '', tab: 'TODO' })
const auditForm = reactive({ pass: true, reason: '' })
const depositForm = reactive({ amount: '', method: '线下现金', proof: '' })
const handoverForm = reactive({ appearance: '良好，无新增磕碰', function: '各部位正常，音准 A440', instrumentNoCheck: '已核对一致', accessories: '', scratches: '无', photos: '' })
const returnForm = reactive({ returnDate: '', functionCheck: '正常', appearanceCheck: '正常', accessoryCheck: '齐全', cleanCheck: '已清洁', result: 'PASS', damageDesc: '' })
const damageForm = reactive({ damagePoint: '', missingAccessory: '', repairCost: '0', liability: '客户责任', conclusion: '' })

const instLabel = (id) => options.value.find((o) => o.id === id)?.label || '乐器 #' + id

async function load() {
  loading.value = true
  try {
    const res = await api.rentalPage(q)
    rows.value = res.records
    total.value = res.total
    todoCount.value = res.todo
  } finally {
    loading.value = false
  }
}

function search() {
  q.pageNum = 1
  load()
}

async function openDetail(row) {
  detail.value = await api.rentalDetail(row.id)
  detailVisible.value = true
}

function openAudit(row) {
  current.value = row
  auditForm.pass = true
  auditForm.reason = ''
  auditVisible.value = true
}

async function submitAudit() {
  await api.rentalAudit(current.value.id, auditForm)
  ElMessage.success(auditForm.pass ? '已审核通过' : '已退回')
  auditVisible.value = false
  load()
}

function openDeposit(row) {
  current.value = row
  depositForm.amount = String(Number(row.depositAmount) + Number(row.rentAmount))
  depositForm.proof = ''
  depositForm.method = '线下现金'
  depositVisible.value = true
}

async function uploadProof({ file }) {
  const res = await api.upload(file, 'pay')
  depositForm.proof = res.url
}

async function submitDeposit() {
  await api.rentalDeposit(current.value.id, depositForm)
  ElMessage.success('押金已登记，可办理出库交接')
  depositVisible.value = false
  load()
}

function openHandover(row) {
  current.value = row
  handoverForm.accessories = row.needAccessory || '琴包、背带'
  handoverForm.photos = ''
  handoverVisible.value = true
}

async function uploadHandover({ file }) {
  const res = await api.upload(file, 'handover')
  handoverForm.photos = res.url
}

async function submitHandover() {
  await api.rentalHandover(current.value.id, handoverForm)
  ElMessage.success('交接完成，乐器进入租用中')
  handoverVisible.value = false
  load()
}

function openReturn(row) {
  current.value = row
  returnForm.returnDate = new Date().toISOString().slice(0, 10)
  returnForm.result = 'PASS'
  returnForm.damageDesc = ''
  returnVisible.value = true
}

async function submitReturn() {
  const res = await api.rentalReturn(current.value.id, returnForm)
  ElMessage.success(`验收完成，逾期 ${res.overdueDays} 天，逾期费 ¥${money(res.overdueFee)}，应退押金 ¥${money(res.depositRefund)}`)
  returnVisible.value = false
  load()
}

function openDamage(row) {
  current.value = row
  damageForm.damagePoint = ''
  damageForm.missingAccessory = ''
  damageForm.repairCost = '0'
  damageForm.liability = '客户责任'
  damageForm.conclusion = ''
  damageVisible.value = true
}

async function submitDamage() {
  await api.rentalDamage(current.value.id, damageForm)
  ElMessage.success('赔偿已登记，订单已关闭')
  damageVisible.value = false
  load()
}

function exportCurrent() {
  const head = ['订单号', '客户', '乐器', '租期', '租金', '押金状态', '状态']
  const lines = rows.value.map((r) => [r.orderNo, r.customerName, instLabel(r.instrumentId), `${r.startDate}~${r.endDate}`, r.rentAmount, depositMeta(r.depositStatus).text, orderMeta(r.status).text].join(','))
  const blob = new Blob([`\ufeff${head.join(',')}\n${lines.join('\n')}`], { type: 'text/csv;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `租赁订单_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  ElMessage.success('已按当前筛选导出')
}

onMounted(async () => {
  options.value = await api.instrumentOptions()
  load()
})
</script>

<style scoped>
.pager { margin-top: 14px; justify-content: flex-end; }
.thumb { width: 96px; height: 64px; object-fit: cover; border-radius: 4px; }
</style>
