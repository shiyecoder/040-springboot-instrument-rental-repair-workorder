<template>
  <div>
    <div class="pagehead">
      <div><h1>个人中心</h1><p>修改资料、密码与头像，头像上传后右上角立即同步</p></div>
    </div>

    <div class="split">
      <div class="card">
        <div class="card-title">基本资料</div>
        <el-form label-width="90px">
          <el-form-item label="头像">
            <el-upload :show-file-list="false" :http-request="uploadAvatar" accept="image/*">
              <img :src="store.user?.avatar || '/uploads/seed/avatar/01.jpg'" class="ava-lg" />
            </el-upload>
            <span class="hint" style="margin-left:12px">点击头像上传新图片</span>
          </el-form-item>
          <el-form-item label="账号"><el-input :model-value="store.user?.username" disabled /></el-form-item>
          <el-form-item label="角色"><el-input :model-value="roleText(store.user?.roleCode)" disabled /></el-form-item>
          <el-form-item label="姓名"><el-input v-model="profile.realName" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="profile.phone" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="profile.email" /></el-form-item>
        </el-form>
        <el-button type="primary" @click="saveProfile">保存资料</el-button>
      </div>

      <div class="card">
        <div class="card-title">修改密码</div>
        <el-form label-width="90px">
          <el-form-item label="原密码"><el-input v-model="pwd.oldPassword" type="password" show-password /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="pwd.newPassword" type="password" show-password /></el-form-item>
          <el-form-item label="确认新密码"><el-input v-model="pwd.confirm" type="password" show-password /></el-form-item>
        </el-form>
        <el-button @click="savePassword">修改密码</el-button>
        <div class="hint mt">密码使用 BCrypt 保存，连续 5 次登录失败会锁定 10 分钟。</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { loadUser, setAvatar, store } from '../store'
import { roleText } from '../utils'

const profile = reactive({ realName: '', phone: '', email: '' })
const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })

async function uploadAvatar({ file }) {
  const res = await api.upload(file, 'avatar')
  await api.saveAvatar(res.url)
  setAvatar(res.url)
  ElMessage.success('头像已更新')
}

async function saveProfile() {
  await api.updateProfile(profile)
  await loadUser(true)
  ElMessage.success('资料已保存')
}

async function savePassword() {
  if (!pwd.newPassword || pwd.newPassword.length < 6) return ElMessage.warning('新密码至少 6 位')
  if (pwd.newPassword !== pwd.confirm) return ElMessage.warning('两次输入的新密码不一致')
  await api.changePassword({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
  ElMessage.success('密码已修改')
  Object.assign(pwd, { oldPassword: '', newPassword: '', confirm: '' })
}

onMounted(async () => {
  await loadUser(true)
  profile.realName = store.user?.realName || ''
  profile.phone = store.user?.phone || ''
  profile.email = store.user?.email || ''
})
</script>

<style scoped>
.split { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.ava-lg { width: 64px; height: 64px; border-radius: 50%; object-fit: cover; border: 1px solid #E2E7EC; cursor: pointer; }
</style>
