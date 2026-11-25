<template>
  <div>
    <div class="pagehead">
      <div><h1>保养计划</h1><p>按周期生成保养任务，执行后回写最近保养日期，并避免与租赁、维修时段重叠</p></div>
      <div class="head-actions"><el-button type="primary" @click="openCreate">新增计划</el-button></div>
    </div>

    <div class="card">
      <div class="filters">
        <el-select v-model="q.status" placeholder="状态" clearable @change="load">
          <el-option label="计划中" value="PLAN" /><el-option label="待执行" value="DUE" /><el-option label="已完成" value="DONE" />
        </el-select>
        <el-button @click="load">刷新</el-button>
      </div>
    </div>

    <div class="card">
      <el-table :data="rows" v-loading="loading">
        <el-table-column label="乐器" min-width="200">
          <template #default="{ row }">{{ row.instrumentNo }} {{ row.instrumentName }}</template>
        </el-table-column>
        <el-table-column prop="location" label="仓位" width="110" />
        <el-table-column label="上次保养" width="120" prop="lastMaintain" />
        <el-table-column label="周期" width="90"><template #default="{ row }">{{ row.plan.cycleDays }} 天</template></el-table-column>
        <el-table-column label="计划日期" width="120"><template #default="{ row }">{{ row.plan.planDate }}</template></el-table-column>
        <el-table-column label="内容" min-width="180"><template #default="{ row }">{{ row.plan.content }}</template></el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.plan.status === 'DONE' ? 'success' : row.plan.status === 'DUE' ? 'warning' : 'info'" size="small">
              {{ { PLAN: '计划中', DUE: '待执行', DONE: '已完成' }[row.plan.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button v-if="row.plan.status !== 'DONE'" link type="primary" @click="execute(row.plan)">执行保养</el-button>
            <span v-else class="hint">{{ row.plan.finishDate }}</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, prev, pager, next" :total="total"
        v-model:current-page="q.pageNum" v-model:page-size="q.pageSize" @change="load" />
    </div>

    <el-dialog v-model="visible" title="新增保养计划" width="460px">
      <el-form label-width="96px">
        <el-form-item label="乐器">
          <el-select v-model="form.instrumentId" filterable style="width:100%">
            <el-option v-for="o in options" :key="o.id" :label="o.label" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划日期">
          <el-date-picker v-model="form.planDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="周期天数"><el-input v-model="form.cycleDays" /></el-form-item>
        <el-form-item label="保养内容"><el-input v-model="form.content" type="textarea" :rows="2" /></el-form-item>
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

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const visible = ref(false)
const options = ref([])
const q = reactive({ pageNum: 1, pageSize: 10, status: '' })
const form = reactive({ instrumentId: null, planDate: '', cycleDays: 90, content: '清洁与调音' })

async function load() {
  loading.value = true
  try {
    const res = await api.maintainPage(q)
    rows.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.instrumentId = null
  form.planDate = ''
  form.cycleDays = 90
  form.content = '清洁与调音'
  visible.value = true
}

async function save() {
  await api.maintainCreate(form)
  ElMessage.success('已创建')
  visible.value = false
  load()
}

async function execute(plan) {
  await api.maintainExecute(plan.id, {})
  ElMessage.success('保养已完成，乐器状态已更新')
  load()
}

onMounted(async () => {
  options.value = await api.instrumentOptions()
  load()
})
</script>

<style scoped>
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
