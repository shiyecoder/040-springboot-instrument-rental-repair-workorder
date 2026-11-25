<template>
  <div>
    <div class="pagehead">
      <div><h1>租赁价格方案</h1><p>按日 / 周 / 月设置租金、押金、最短最长租期、逾期费率与赠送配件</p></div>
      <div class="head-actions"><el-button type="primary" @click="openEdit()">新增方案</el-button></div>
    </div>

    <div class="card">
      <el-table :data="rows" v-loading="loading">
        <el-table-column prop="planName" label="方案名称" min-width="120" />
        <el-table-column prop="category" label="适用类别" width="110" />
        <el-table-column label="日/周/月租金" min-width="190">
          <template #default="{ row }">
            <span class="num">¥{{ money(row.dayPrice) }} / {{ money(row.weekPrice) }} / {{ money(row.monthPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="押金" width="110">
          <template #default="{ row }"><span class="num">¥{{ money(row.deposit) }}</span></template>
        </el-table-column>
        <el-table-column label="最短/最长" width="110">
          <template #default="{ row }">{{ row.minDays }} / {{ row.maxDays }} 天</template>
        </el-table-column>
        <el-table-column label="逾期费" width="120">
          <template #default="{ row }">日租金 × {{ row.overdueRate }}</template>
        </el-table-column>
        <el-table-column prop="giftAccessory" label="赠送配件" min-width="140" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }"><el-button link type="primary" @click="openEdit(row)">编辑</el-button></template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="form.id ? '编辑价格方案' : '新增价格方案'" width="500px">
      <el-form label-width="106px">
        <el-form-item label="方案名称"><el-input v-model="form.planName" /></el-form-item>
        <el-form-item label="适用类别">
          <el-select v-model="form.category" style="width:100%">
            <el-option v-for="c in ['吉他','钢琴','弦乐','管乐','打击乐','民乐','键盘']" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="日租金"><el-input v-model="form.dayPrice" /></el-form-item>
        <el-form-item label="周租金"><el-input v-model="form.weekPrice" /></el-form-item>
        <el-form-item label="月租金"><el-input v-model="form.monthPrice" /></el-form-item>
        <el-form-item label="押金"><el-input v-model="form.deposit" /></el-form-item>
        <el-form-item label="最短租期"><el-input v-model="form.minDays" /></el-form-item>
        <el-form-item label="最长租期"><el-input v-model="form.maxDays" /></el-form-item>
        <el-form-item label="逾期费率"><el-input v-model="form.overdueRate" /></el-form-item>
        <el-form-item label="赠送配件"><el-input v-model="form.giftAccessory" /></el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { money } from '../utils'

const rows = ref([])
const loading = ref(false)
const visible = ref(false)
const form = reactive({})

async function load() {
  loading.value = true
  try {
    rows.value = await api.priceList()
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  Object.keys(form).forEach((k) => delete form[k])
  if (row) Object.assign(form, row)
  else Object.assign(form, { status: 1, minDays: 3, maxDays: 90, overdueRate: 1.5 })
  visible.value = true
}

async function save() {
  await api.priceSave(form)
  ElMessage.success('已保存')
  visible.value = false
  load()
}

onMounted(load)
</script>
