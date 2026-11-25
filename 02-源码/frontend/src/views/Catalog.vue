<template>
  <div>
    <div class="pagehead">
      <div><h1>乐器租赁目录</h1><p>按类别、品牌、成色、存放点筛选；押金与租金到店线下登记</p></div>
      <div class="head-actions"><el-button type="primary" @click="$router.push('/calendar')">查看可租日历</el-button></div>
    </div>

    <div class="card">
      <div class="filters">
        <el-select v-model="q.category" placeholder="乐器类别" clearable>
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
        <el-input v-model="q.brand" placeholder="品牌" clearable style="width:160px" />
        <el-select v-model="q.condition" placeholder="成色" clearable>
          <el-option v-for="c in ['九成新','八成新','七成新','六成新']" :key="c" :label="c" :value="c" />
        </el-select>
        <el-input v-model="q.keyword" placeholder="编号 / 名称 / 型号" clearable />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
    </div>

    <div class="card">
      <el-table :data="rows" v-loading="loading" style="width:100%">
        <el-table-column label="图片" width="92">
          <template #default="{ row }">
            <img :src="row.image" class="thumb" alt="乐器图片" />
          </template>
        </el-table-column>
        <el-table-column prop="instrumentNo" label="乐器编号" min-width="130" />
        <el-table-column prop="name" label="名称" min-width="100" />
        <el-table-column prop="category" label="类别" min-width="80" />
        <el-table-column label="品牌型号" min-width="150">
          <template #default="{ row }">{{ row.brand }} {{ row.model }}</template>
        </el-table-column>
        <el-table-column prop="conditionLevel" label="成色" min-width="80" />
        <el-table-column prop="location" label="仓位" min-width="90" />
        <el-table-column label="状态" min-width="90">
          <template #default="{ row }"><el-tag :type="instMeta(row.status).type" size="small">{{ instMeta(row.status).text }}</el-tag></template>
        </el-table-column>
        <el-table-column label="状态分" min-width="80">
          <template #default="{ row }"><span class="num">{{ row.scoreValue ?? '-' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" min-width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="row.status !== 'AVAILABLE'" @click="openBook(row)">预约</el-button>
            <el-button link @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, prev, pager, next, sizes" :total="total"
        v-model:current-page="q.pageNum" v-model:page-size="q.pageSize" :page-sizes="[10,20,50]" @change="load" />
    </div>

    <el-drawer v-model="bookVisible" title="提交租赁预约" size="480px">
      <div v-if="current" class="notice" style="margin-bottom:14px">
        {{ current.instrumentNo }} · {{ current.name }} · {{ current.brand }} {{ current.model }}<br />
        日租金 ¥{{ money(price?.dayPrice) }} · 押金 ¥{{ money(price?.deposit) }} · 最短 {{ price?.minDays }} 天
      </div>
      <el-form :model="form" label-width="96px">
        <el-form-item label="租赁时段">
          <el-date-picker v-model="form.range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="起租日"
            end-placeholder="归还日" style="width:100%" />
        </el-form-item>
        <el-form-item label="用途">
          <el-select v-model="form.purpose" style="width:100%">
            <el-option v-for="p in ['考级练习','演出排练','短期体验','教学使用','录音制作']" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="取件方式">
          <el-radio-group v-model="form.deliveryType">
            <el-radio value="到店自提">到店自提</el-radio>
            <el-radio value="门店配送">门店配送</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="需要配件">
          <el-input v-model="form.needAccessory" placeholder="如：琴包、背带" />
        </el-form-item>
        <el-form-item label="身份证明">
          <el-select v-model="form.idProof" style="width:100%">
            <el-option label="身份证" value="身份证" /><el-option label="学生证" value="学生证" />
          </el-select>
        </el-form-item>
        <el-form-item label="证明照片">
          <el-upload :show-file-list="false" :http-request="uploadProof" accept="image/*">
            <img v-if="form.idProofImage" :src="form.idProofImage" class="thumb" style="width:96px;height:64px" />
            <el-button v-else size="small">上传证件照</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="预估租金">
          <b class="num">¥{{ money(estimate) }}</b>
          <span class="hint" style="margin-left:8px">{{ days }} 天（后端最终核定）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitBooking">提交预约</el-button>
      </template>
    </el-drawer>

    <el-drawer v-model="detailVisible" title="乐器详情" size="520px">
      <template v-if="detail">
        <img :src="detail.instrument.image" style="width:100%;border-radius:8px;margin-bottom:14px" />
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="编号">{{ detail.instrument.instrumentNo }}</el-descriptions-item>
          <el-descriptions-item label="名称">{{ detail.instrument.name }}</el-descriptions-item>
          <el-descriptions-item label="类别">{{ detail.instrument.category }}</el-descriptions-item>
          <el-descriptions-item label="品牌型号">{{ detail.instrument.brand }} {{ detail.instrument.model }}</el-descriptions-item>
          <el-descriptions-item label="级别">{{ detail.instrument.level }}</el-descriptions-item>
          <el-descriptions-item label="尺寸">{{ detail.instrument.sizeSpec }}</el-descriptions-item>
          <el-descriptions-item label="成色">{{ detail.instrument.conditionLevel }}</el-descriptions-item>
          <el-descriptions-item label="仓位">{{ detail.instrument.location }}</el-descriptions-item>
          <el-descriptions-item label="随附配件" :span="2">{{ detail.instrument.accessoryDesc }}</el-descriptions-item>
        </el-descriptions>
        <div class="card-title mt">配件清单</div>
        <el-table :data="detail.accessories" size="small">
          <el-table-column prop="accessoryName" label="配件" />
          <el-table-column prop="spec" label="规格" />
          <el-table-column prop="quantity" label="数量" width="70" />
        </el-table>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { calcDays, instMeta, money } from '../utils'

const categories = ['吉他', '钢琴', '弦乐', '管乐', '打击乐', '民乐', '键盘']
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const bookVisible = ref(false)
const detailVisible = ref(false)
const current = ref(null)
const detail = ref(null)
const price = ref(null)
const q = reactive({ pageNum: 1, pageSize: 10, keyword: '', category: '', brand: '', condition: '' })
const form = reactive({ range: [], purpose: '考级练习', deliveryType: '到店自提', needAccessory: '', idProof: '身份证', idProofImage: '' })

const days = computed(() => (form.range?.length === 2 ? calcDays(form.range[0], form.range[1]) : 0))
const estimate = computed(() => (price.value ? Number(price.value.dayPrice) * days.value : 0))

async function load() {
  loading.value = true
  try {
    const res = await api.instrumentPage(q)
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

function reset() {
  Object.assign(q, { pageNum: 1, keyword: '', category: '', brand: '', condition: '' })
  load()
}

async function openBook(row) {
  current.value = row
  const res = await api.calendar({ instrumentId: row.id, month: new Date().toISOString().slice(0, 7) })
  price.value = res.price
  form.range = []
  form.idProofImage = ''
  bookVisible.value = true
}

async function openDetail(row) {
  detail.value = await api.instrumentDetail(row.id)
  detailVisible.value = true
}

async function uploadProof({ file }) {
  const res = await api.upload(file, 'idproof')
  form.idProofImage = res.url
}

async function submitBooking() {
  if (form.range?.length !== 2) return ElMessage.warning('请选择租赁时段')
  saving.value = true
  try {
    await api.rentalCreate({
      instrumentId: current.value.id, startDate: form.range[0], endDate: form.range[1],
      purpose: form.purpose, deliveryType: form.deliveryType, needAccessory: form.needAccessory,
      idProof: form.idProof, idProofImage: form.idProofImage
    })
    ElMessage.success('预约已提交，等待门店审核')
    bookVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.thumb { width: 72px; height: 54px; object-fit: cover; border-radius: 4px; background: #E9EEF2; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
