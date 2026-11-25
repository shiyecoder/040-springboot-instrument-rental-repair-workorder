<template>
  <div>
    <div class="pagehead">
      <div><h1>菜单管理</h1><p>维护系统菜单与排序；权限按角色分配，菜单权限不等于接口权限，写接口仍需后端校验</p></div>
      <div class="head-actions"><el-button type="primary" @click="openEdit()">新增菜单</el-button></div>
    </div>

    <div class="card">
      <el-table :data="rows" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="菜单名称" min-width="160" />
        <el-table-column prop="path" label="路由地址" min-width="180" />
        <el-table-column prop="icon" label="图标" width="120" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }"><el-button link type="primary" @click="openEdit(row)">编辑</el-button></template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="form.id ? '编辑菜单' : '新增菜单'" width="440px">
      <el-form label-width="90px">
        <el-form-item label="菜单名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="路由地址"><el-input v-model="form.path" placeholder="/xxx" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" placeholder="Element Plus 图标名" /></el-form-item>
        <el-form-item label="排序"><el-input v-model="form.sort" /></el-form-item>
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
const loading = ref(false)
const visible = ref(false)
const form = reactive({})

async function load() {
  loading.value = true
  try {
    rows.value = await api.menuTree()
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  Object.keys(form).forEach((k) => delete form[k])
  if (row) Object.assign(form, row)
  else Object.assign(form, { sort: 99, icon: 'Document' })
  visible.value = true
}

async function save() {
  await api.menuSave(form)
  ElMessage.success('已保存')
  visible.value = false
  load()
}

onMounted(load)
</script>
