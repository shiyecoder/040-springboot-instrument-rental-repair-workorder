<template>
  <div>
    <div class="pagehead">
      <div><h1>租期报修</h1><p>上报故障与照片，门店判定换机、返店维修或远程指导</p></div>
    </div>

    <div class="split">
      <div class="card">
        <div class="card-title">可报修的租赁单</div>
        <el-table :data="orders" size="small">
          <el-table-column prop="orderNo" label="订单号" min-width="160" />
          <el-table-column label="租期" min-width="170">
            <template #default="{ row }">{{ row.startDate }} ~ {{ row.endDate }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }"><el-tag :type="orderMeta(row.status).type" size="small">{{ orderMeta(row.status).text }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="110">
            <template #default="{ row }"><el-button link type="primary" @click="open(row)">报修</el-button></template>
          </el-table-column>
        </el-table>
      </div>

      <div class="card">
        <div class="card-title">我提交的故障</div>
        <el-table :data="repairs" size="small">
          <el-table-column prop="repairNo" label="工单号" min-width="150" />
          <el-table-column prop="faultDesc" label="故障" min-width="150" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="repairMeta(row.status).type" size="small">{{ repairMeta(row.status).text }}</el-tag></template>
          </el-table-column>
        </el-table>
        <div class="hint mt">维修进度由门店技师更新，完成后会在消息中心通知。</div>
      </div>
    </div>

    <el-dialog v-model="visible" title="提交报修" width="460px">
      <el-form label-width="90px">
        <el-form-item label="订单">{{ current?.orderNo }}</el-form-item>
        <el-form-item label="故障描述">
          <el-input v-model="form.faultDesc" type="textarea" :rows="3" placeholder="不少于 5 个字，描述现象与位置" />
        </el-form-item>
        <el-form-item label="故障照片">
          <el-upload :show-file-list="false" :http-request="uploadImg" accept="image/*">
            <img v-if="form.images" :src="form.images" class="thumb" style="width:96px;height:64px" />
            <el-button v-else size="small">上传照片</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { orderMeta, repairMeta } from '../utils'

const orders = ref([])
const repairs = ref([])
const visible = ref(false)
const current = ref(null)
const form = reactive({ faultDesc: '', images: '' })

async function load() {
  const res = await api.rentalPage({ pageNum: 1, pageSize: 50 })
  orders.value = res.records.filter((o) => ['RENTING', 'PENDING_RETURN'].includes(o.status))
  const r = await api.repairPage({ pageNum: 1, pageSize: 20 })
  const myNos = new Set(res.records.map((o) => o.orderNo))
  repairs.value = r.records.filter((x) => x.repair.source === 'RENTAL').map((x) => x.repair)
}

function open(row) {
  current.value = row
  form.faultDesc = ''
  form.images = ''
  visible.value = true
}

async function uploadImg({ file }) {
  const res = await api.upload(file, 'repair')
  form.images = res.url
}

async function submit() {
  if ((form.faultDesc || '').length < 5) return ElMessage.warning('故障描述不少于 5 个字')
  await api.rentalRepair(current.value.id, form)
  ElMessage.success('报修已提交')
  visible.value = false
  load()
}

onMounted(load)
</script>

<style scoped>
.split { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.thumb { width: 96px; height: 64px; object-fit: cover; border-radius: 4px; }
</style>
