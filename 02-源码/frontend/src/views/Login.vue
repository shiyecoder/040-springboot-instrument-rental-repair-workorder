<template>
  <div class="login">
    <div class="left">
      <div class="brandline"><span class="dot">♫</span> 和声琴行 · 乐器租赁与维修工单平台</div>
      <h1>一物一码<br />租得清楚，修得明白</h1>
      <p class="desc">
        乐器台账 · 可租日历 · 预约审核 · 押金登记<br />
        出库交接 · 续租冲突校验 · 归还验收 · 损坏赔偿<br />
        维修工单 · 状态评分与维护周期
      </p>
      <div class="nums">
        <div><b>22</b><span>在册乐器</span></div>
        <div><b>34</b><span>租赁工单</span></div>
        <div><b>68.2%</b><span>可租率</span></div>
      </div>
      <div class="foot">系统只登记线下收款状态与台账，不接入线上支付</div>
    </div>
    <div class="right">
      <div class="box">
        <h2>登录</h2>
        <p class="hint">按角色进入对应工作台</p>
        <el-form :model="form" ref="formRef" @submit.prevent>
          <el-form-item prop="username">
            <el-input v-model="form.username" size="large" placeholder="账号" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" size="large" type="password" show-password placeholder="密码"
                      :prefix-icon="Lock" @keyup.enter="submit" />
          </el-form-item>
          <el-button type="primary" size="large" class="submit" :loading="loading" @click="submit">登 录</el-button>
        </el-form>
        <div class="tip">
          <div><b>演示账号</b>（客户与员工密码 123456，管理员 admin123）</div>
          <div>admin 门店管理员 · clerk01 门店营业员 · clerk02 分店营业员</div>
          <div>tech01 维修技师 · customer01 客户</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { api } from '../api'
import { loadUser } from '../store'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })

async function submit() {
  if (!form.username || !form.password) return ElMessage.warning('请输入账号和密码')
  loading.value = true
  try {
    const data = await api.login(form)
    localStorage.setItem('token', data.token)
    await loadUser(true)
    ElMessage.success('登录成功，欢迎回来')
    router.push('/dashboard')
  } catch (e) {
    // 错误提示由拦截器统一展示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login { display: grid; grid-template-columns: 1.08fr 0.92fr; height: 100vh; background: #fff; }
.left { position: relative; color: #fff; padding: 64px 56px; display: flex; flex-direction: column; justify-content: center;
  background: linear-gradient(120deg, rgba(25,50,74,.94), rgba(53,106,122,.82)), url('/login-bg.jpg') center/cover no-repeat; }
.brandline { position: absolute; top: 40px; left: 56px; display: flex; align-items: center; gap: 10px; font-size: 14px; letter-spacing: 1px; color: #D7E2EA; }
.dot { width: 28px; height: 28px; border-radius: 6px; background: #356A7A; display: grid; place-items: center; font-weight: 600; }
h1 { font-size: 34px; line-height: 1.42; font-weight: 600; margin: 0; letter-spacing: 1px; }
.desc { margin-top: 20px; color: #CFDDE6; line-height: 2; font-size: 13.5px; }
.nums { display: flex; gap: 44px; margin-top: 42px; }
.nums b { display: block; font-size: 26px; font-weight: 600; font-variant-numeric: tabular-nums; }
.nums span { font-size: 12px; color: #BCCDD8; }
.foot { position: absolute; bottom: 32px; left: 56px; font-size: 12px; color: #9FB2BE; }
.right { display: flex; align-items: center; justify-content: center; background: #F4F6F8; }
.box { width: 380px; background: #fff; border: 1px solid #E2E7EC; border-radius: 10px; padding: 34px 32px;
  box-shadow: 0 8px 24px rgba(25,50,74,.06); }
h2 { font-size: 24px; margin: 0 0 6px; color: #19324A; }
.hint { color: #63707E; font-size: 13px; margin: 0 0 24px; }
.submit { width: 100%; height: 46px; margin-top: 6px; font-size: 15px; letter-spacing: 4px; background: #19324A; border-color: #19324A; }
.tip { margin-top: 20px; background: #EFF3F6; border-radius: 8px; padding: 12px 14px; font-size: 12px; color: #5F707C; line-height: 1.9; }
.tip b { color: #19324A; }
:deep(.el-form-item) { margin-bottom: 18px; }
</style>
