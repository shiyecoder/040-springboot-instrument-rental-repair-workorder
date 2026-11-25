<template>
  <div>
    <div class="pagehead">
      <div><h1>损坏赔偿</h1><p>新增损坏点、缺失配件、维修成本与责任结论，登记线下赔偿后关闭订单</p></div>
    </div>

    <div class="card">
      <div class="card-title">待赔偿订单（验收未通过）</div>
      <el-table :data="rows" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" min-width="170" />
        <el-table-column prop="customerName" label="客户" min-width="100" />
        <el-table-column label="乐器" min-width="170">
          <template #default="{ row }">{{ instLabel(row.instrumentId) }}</template>
        </el-table-column>
        <el-table-column label="押金" width="110">
          <template #default="{ row }"><span class="num">¥{{ money(row.depositAmount) }}</span></template>
        </el-table-column>
        <el-table-column label="已记赔偿" width="110">
          <template #default="{ row }"><span class="num">¥{{ money(row.compensateAmount) }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }"><el-button link type="danger" @click="open(row)">登记赔偿</el-button></template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <div class="card-title">历史赔偿记录</div>
      <el-table :data="claims" size="small">
        <el-table-column prop="orderNo" label="订单号" min-width="170" />
        <el-table-column prop="damagePoint" label="损坏点" min-width="180" />
        <el-table-column prop="liability" label="责任结论" width="110" />
        <el-table-column label="赔偿金额" width="110">
          <template #default="{ row }"><span class="num">¥{{ money(row.amount) }}</span></template>
        </el-table-column>
        <el-table-column prop="operator" label="经办人" width="100" />
        <el-table-column label="时间" min-width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" title="损坏赔偿处理" width="520px">
      <el-form label-width="96px">
        <el-form-item label="损坏点"><el-input v-model="form.damagePoint" /></el-form-item>
        <el-form-item label="缺失配件"><el-input v-model="form.missingAccessory" /></el-form-item>
        <el-form-item label="维修成本"><el-input v-model="form.repairCost" /></el-form-item>
        <el-form-item label="责任结论">
          <el-select v-model="form.liability" style="width:100%">
            <el-option label="客户责任" value="客户责任" /><el-option label="自然损耗" value="自然损耗" />
            <el-option label="门店责任" value="门店责任" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明"><el-input v-model="form.conclusion" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="收款凭证">
          <el-upload :show-file-list="false" :http-request="upload" accept="image/*">
            <img v-if="form.payProof" :src="form.payProof" class="thumb" />
            <el-button v-else size="small">上传凭证</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存并登记赔偿</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { formatTime, money } from '../utils'

const rows = ref([])
const claims = ref([])
const options = ref([])
const loading = ref(false)
const visible = ref(false)
const current = ref(null)
const form = reactive({ damagePoint: '', missingAccessory: '', repairCost: '0', liability: '客户责任', conclusion: '', payProof: '' })
const instLabel = (id) => options.value.find((o) => o.id === id)?.label || '乐器 #' + id

async function load() {
  loading.value = true
  try {
    const res = await api.rentalPage({ pageNum: 1, pageSize: 50, status: 'PENDING_RETURN' })
    rows.value = res.records
    // 历史赔偿从已完成订单详情汇总
    const done = await api.rentalPage({ pageNum: 1, pageSize: 30, status: 'COMPLETED' })
    const list = []
    for (const o of done.records.slice(0, 10)) {
      const d = await api.rentalDetail(o.id)
      for (const c of d.damages || []) list.push({ ...c, orderNo: o.orderNo })
    }
    claims.value = list
  } finally {
    loading.value = false
  }
}

function open(row) {
  current.value = row
  Object.assign(form, { damagePoint: '', missingAccessory: '', repairCost: '0', liability: '客户责任', conclusion: '', payProof: '' })
  visible.value = true
}

async function upload({ file }) {
  const res = await api.upload(file, 'pay')
  form.payProof = res.url
}

async function submit() {
  await api.rentalDamage(current.value.id, form)
  ElMessage.success('赔偿已登记，订单关闭')
  visible.value = false
  load()
}

onMounted(async () => {
  options.value = await api.instrumentOptions()
  load()
})
</script>

<style scoped>
.thumb { width: 96px; height: 64px; object-fit: cover; border-radius: 4px; }
</style>
