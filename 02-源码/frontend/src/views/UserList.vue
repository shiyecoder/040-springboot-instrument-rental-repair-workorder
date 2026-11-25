<template>
  <div>
    <div class="pagehead">
      <div><h1>用户与角色</h1><p>四类角色：客户、门店营业员、维修技师、门店管理员；菜单权限与数据范围分别控制</p></div>
      <div class="head-actions"><el-button type="primary" @click="openEdit()">新增用户</el-button></div>
    </div>

    <div class="card">
      <div class="filters">
        <el-select v-model="q.roleCode" placeholder="角色" clearable>
          <el-option v-for="r in roles" :key="r.roleCode" :label="r.roleName" :value="r.roleCode" />
        </el-select>
        <el-input v-model="q.keyword" placeholder="姓名 / 账号 / 手机号" clearable />
        <el-button type="primary" @click="search">查询</el-button>
      </div>
    </div>

    <div class="card">
      <el-table :data="rows" v-loading="loading">
        <el-table-column prop="username" label="账号" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="110" />
        <el-table-column label="角色" width="130">
          <template #default="{ row }">{{ roleText(row.roleCode) }}</template>
        </el-table-column>
        <el-table-column prop="storeName" label="门店" width="100" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="最近登录" min-width="150">
          <template #default="{ row }">{{ formatTime(row.lastLogin) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link @click="reset(row)">重置密码</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, prev, pager, next" :total="total"
        v-model:current-page="q.pageNum" v-model:page-size="q.pageSize" @change="load" />
    </div>

    <div class="card">
      <div class="card-title">角色与菜单权限</div>
      <el-table :data="roles" size="small">
        <el-table-column prop="roleName" label="角色" width="140" />
        <el-table-column prop="dataScope" label="数据范围" width="110" />
        <el-table-column prop="userCount" label="用户数" width="90" />
        <el-table-column prop="remark" label="职责" min-width="240" />
        <el-table-column label="操作" width="130">
          <template #default="{ row }"><el-button link type="primary" @click="assign(row)">分配菜单</el-button></template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="editVisible" :title="form.id ? '编辑用户' : '新增用户'" width="460px">
      <el-form label-width="90px">
        <el-form-item label="账号"><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleCode" style="width:100%">
            <el-option v-for="r in roles" :key="r.roleCode" :label="r.roleName" :value="r.roleCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="门店"><el-input v-model="form.storeName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="初始密码"><el-input v-model="form.password" placeholder="留空默认 123456" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="menuVisible" :title="'为角色分配菜单 · ' + (assignRole?.roleName || '')" width="520px">
      <el-checkbox-group v-model="checkedMenus">
        <el-checkbox v-for="m in menus" :key="m.id" :value="m.id" style="width:46%">{{ m.name }}</el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="menuVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMenus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import { formatTime, roleText } from '../utils'

const rows = ref([])
const roles = ref([])
const menus = ref([])
const total = ref(0)
const loading = ref(false)
const editVisible = ref(false)
const menuVisible = ref(false)
const assignRole = ref(null)
const checkedMenus = ref([])
const q = reactive({ pageNum: 1, pageSize: 10, roleCode: '', keyword: '' })
const form = reactive({})

async function load() {
  loading.value = true
  try {
    const res = await api.userPage(q)
    rows.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function search() {
  q.pageNum = 1
  load()
}

function openEdit(row) {
  Object.keys(form).forEach((k) => delete form[k])
  if (row) Object.assign(form, row)
  else Object.assign(form, { status: 1, roleCode: 'CUSTOMER', storeName: '总店' })
  editVisible.value = true
}

async function save() {
  await api.userSave(form)
  ElMessage.success('已保存')
  editVisible.value = false
  load()
}

async function reset(row) {
  await ElMessageBox.confirm(`确认将 ${row.realName} 的密码重置为 123456？`, '重置确认', { type: 'warning' })
  await api.userReset(row.id)
  ElMessage.success('已重置')
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除用户 ${row.username}？`, '删除确认', { type: 'warning' })
  await api.userDelete(row.id)
  ElMessage.success('已删除')
  load()
}

async function assign(row) {
  assignRole.value = row
  menus.value = await api.menuTree()
  checkedMenus.value = await api.roleMenus(row.id)
  menuVisible.value = true
}

async function saveMenus() {
  await api.roleAssign(assignRole.value.id, checkedMenus.value)
  ElMessage.success('菜单权限已保存')
  menuVisible.value = false
}

onMounted(async () => {
  roles.value = await api.roleList()
  load()
})
</script>

<style scoped>
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
