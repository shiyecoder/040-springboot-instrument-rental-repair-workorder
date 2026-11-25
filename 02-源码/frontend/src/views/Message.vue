<template>
  <div>
    <div class="pagehead">
      <div><h1>消息中心</h1><p>待审批、即将超时、已退回、待验收、处理完成等消息，含业务编号与详情跳转</p></div>
      <div class="head-actions"><el-button @click="load">刷新</el-button></div>
    </div>

    <div class="card">
      <el-table :data="rows" v-loading="loading">
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.isRead === 0 ? 'danger' : 'info'" size="small">{{ row.isRead === 0 ? '未读' : '已读' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="280" />
        <el-table-column prop="bizNo" label="业务编号" min-width="170" />
        <el-table-column label="时间" min-width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="row.isRead === 1" @click="read(row)">标记已读</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, prev, pager, next" :total="total"
        v-model:current-page="q.pageNum" v-model:page-size="q.pageSize" @change="load" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { formatTime } from '../utils'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const q = reactive({ pageNum: 1, pageSize: 10 })

async function load() {
  loading.value = true
  try {
    const res = await api.messages(q)
    rows.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function read(row) {
  await api.messageRead(row.id)
  row.isRead = 1
  ElMessage.success('已标记已读')
}

onMounted(load)
</script>

<style scoped>
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
