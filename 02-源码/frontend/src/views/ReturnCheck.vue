<template>
  <div>
    <div class="pagehead">
      <div><h1>归还验收</h1><p>核对功能、外观、配件与清洁，后端计算逾期费，通过后转可租或待保养</p></div>
    </div>

    <div class="card">
      <div class="card-title">租用中 / 待验收单据</div>
      <el-table :data="rows" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" min-width="170" />
        <el-table-column prop="customerName" label="客户" min-width="100" />
        <el-table-column label="乐器" min-width="170">
          <template #default="{ row }">{{ instLabel(row.instrumentId) }}</template>
        </el-table-column>
        <el-table-column label="应归还日" width="120" prop="endDate" />
        <el-table-column label="逾期" width="90">
          <template #default="{ row }">
            <el-tag v-if="overdueDays(row) > 0" type="danger" size="small">逾期 {{ overdueDays(row) }} 天</el-tag>
            <span v-else class="hint">正常</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="112">
          <template #default="{ row }"><el-tag :type="orderMeta(row.status).type" size="small">{{ orderMeta(row.status).text }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }"><el-button link type="primary" @click="open(row)">归还验收</el-button></template>
        </el-table-column>
      </el-table>
    </div>

    <el-drawer v-model="visible" title="归还验收" size="520px">
      <div v-if="current" class="notice" style="margin-bottom:14px">
        {{ current.orderNo }} · 客户 {{ current.customerName }} · 应归还 {{ current.endDate }}<br />
        押金 ¥{{ money(current.depositAmount) }} · 逾期费后端口径：逾期天数 × 日租金 × 费率
      </div>
      <el-form label-width="96px">
        <el-form-item label="归还日期">
          <el-date-picker v-model="form.returnDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="功能检查">
          <el-select v-model="form.functionCheck" style="width:100%">
            <el-option label="正常" value="正常" /><el-option label="异常" value="异常" />
          </el-select>
        </el-form-item>
        <el-form-item label="外观检查">
          <el-select v-model="form.appearanceCheck" style="width:100%">
            <el-option label="正常" value="正常" /><el-option label="新增损坏" value="新增损坏" />
          </el-select>
        </el-form-item>
        <el-form-item label="配件检查">
          <el-select v-model="form.accessoryCheck" style="width:100%">
            <el-option label="齐全" value="齐全" /><el-option label="缺失" value="缺失" />
          </el-select>
        </el-form-item>
        <el-form-item label="清洁情况">
          <el-select v-model="form.cleanCheck" style="width:100%">
            <el-option label="已清洁" value="已清洁" /><el-option label="需清洁" value="需清洁" />
          </el-select>
        </el-form-item>
        <el-form-item label="验收结论">
          <el-select v-model="form.result" style="width:100%">
            <el-option label="通过，转入可租" value="PASS" />
            <el-option label="不通过，转损坏赔偿" value="DAMAGE" />
            <el-option label="转维修工单" value="REPAIR" />
          </el-select>
        </el-form-item>
        <el-form-item label="损坏描述"><el-input v-model="form.damageDesc" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">提交验收</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { money, orderMeta } from '../utils'

const rows = ref([])
const options = ref([])
const loading = ref(false)
const visible = ref(false)
const current = ref(null)
const form = reactive({ returnDate: '', functionCheck: '正常', appearanceCheck: '正常', accessoryCheck: '齐全', cleanCheck: '已清洁', result: 'PASS', damageDesc: '' })
const instLabel = (id) => options.value.find((o) => o.id === id)?.label || '乐器 #' + id

function overdueDays(row) {
  const today = new Date().toISOString().slice(0, 10)
  if (today <= row.endDate) return 0
  return Math.round((new Date(today) - new Date(row.endDate)) / 86400000)
}

async function load() {
  loading.value = true
  try {
    const a = await api.rentalPage({ pageNum: 1, pageSize: 50, status: 'RENTING' })
    const b = await api.rentalPage({ pageNum: 1, pageSize: 50, status: 'PENDING_RETURN' })
    rows.value = [...a.records, ...b.records]
  } finally {
    loading.value = false
  }
}

function open(row) {
  current.value = row
  form.returnDate = new Date().toISOString().slice(0, 10)
  form.result = 'PASS'
  form.damageDesc = ''
  visible.value = true
}

async function submit() {
  const res = await api.rentalReturn(current.value.id, form)
  ElMessage.success(`验收完成：逾期 ${res.overdueDays} 天，逾期费 ¥${money(res.overdueFee)}，应退押金 ¥${money(res.depositRefund)}`)
  visible.value = false
  load()
}

onMounted(async () => {
  options.value = await api.instrumentOptions()
  load()
})
</script>
