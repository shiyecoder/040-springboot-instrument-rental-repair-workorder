<template>
  <div>
    <div class="pagehead">
      <div><h1>出库交接</h1><p>记录外观、功能、编号、配件、已有划痕与照片，双方确认后进入租用中</p></div>
    </div>

    <div class="card">
      <div class="card-title">待出库单据（押金已登记）</div>
      <el-table :data="rows" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" min-width="170" />
        <el-table-column prop="customerName" label="客户" min-width="100" />
        <el-table-column label="乐器" min-width="170">
          <template #default="{ row }">{{ instLabel(row.instrumentId) }}</template>
        </el-table-column>
        <el-table-column label="租期" min-width="180">
          <template #default="{ row }">{{ row.startDate }} ~ {{ row.endDate }}</template>
        </el-table-column>
        <el-table-column label="押金" width="110">
          <template #default="{ row }"><span class="num">¥{{ money(row.depositAmount) }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="130">
          <template #default="{ row }"><el-button link type="primary" @click="open(row)">办理交接</el-button></template>
        </el-table-column>
      </el-table>
    </div>

    <el-drawer v-model="visible" title="出库交接单" size="520px">
      <div v-if="current" class="notice" style="margin-bottom:14px">
        单号 HO-{{ current.orderNo }} · 客户 {{ current.customerName }}<br />
        乐器 {{ instLabel(current.instrumentId) }} · 责任人 {{ current.handlerName || '-' }}
      </div>
      <el-form label-width="96px">
        <el-form-item label="外观检查"><el-input v-model="form.appearance" /></el-form-item>
        <el-form-item label="功能检查"><el-input v-model="form.function" /></el-form-item>
        <el-form-item label="编号核对"><el-input v-model="form.instrumentNoCheck" /></el-form-item>
        <el-form-item label="配件清单"><el-input v-model="form.accessories" /></el-form-item>
        <el-form-item label="已有划痕"><el-input v-model="form.scratches" /></el-form-item>
        <el-form-item label="交接照片">
          <el-upload :show-file-list="false" :http-request="upload" accept="image/*">
            <img v-if="form.photos" :src="form.photos" class="thumb" />
            <el-button v-else size="small">上传照片</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">完成交接</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { money } from '../utils'

const rows = ref([])
const options = ref([])
const loading = ref(false)
const visible = ref(false)
const current = ref(null)
const form = reactive({ appearance: '良好，无新增磕碰', function: '各部位正常，音准 A440', instrumentNoCheck: '已核对一致', accessories: '', scratches: '无', photos: '' })
const instLabel = (id) => options.value.find((o) => o.id === id)?.label || '乐器 #' + id

async function load() {
  loading.value = true
  try {
    const res = await api.rentalPage({ pageNum: 1, pageSize: 50, status: 'PENDING_HANDOVER' })
    rows.value = res.records
  } finally {
    loading.value = false
  }
}

function open(row) {
  current.value = row
  form.accessories = row.needAccessory || '琴包、背带'
  form.photos = ''
  visible.value = true
}

async function upload({ file }) {
  const res = await api.upload(file, 'handover')
  form.photos = res.url
}

async function submit() {
  await api.rentalHandover(current.value.id, form)
  ElMessage.success('交接完成')
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
