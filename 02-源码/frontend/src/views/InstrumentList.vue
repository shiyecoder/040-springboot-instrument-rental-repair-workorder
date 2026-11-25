<template>
  <div>
    <div class="pagehead">
      <div><h1>乐器台账</h1><p>一物一码管理编号、类别、品牌型号、级别、尺寸、购置价、成色、配件、仓位与状态</p></div>
      <div class="head-actions"><el-button type="primary" @click="openEdit()">新增乐器</el-button></div>
    </div>

    <div class="card">
      <div class="filters">
        <el-select v-model="q.category" placeholder="乐器类别" clearable>
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
        <el-input v-model="q.brand" placeholder="品牌" clearable style="width:140px" />
        <el-select v-model="q.condition" placeholder="成色" clearable>
          <el-option v-for="c in ['九成新','八成新','七成新']" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="q.status" placeholder="租赁状态" clearable>
          <el-option v-for="(v, k) in INSTRUMENT_STATUS" :key="k" :label="v.text" :value="k" />
        </el-select>
        <el-input v-model="q.keyword" placeholder="编号 / 名称 / 型号" clearable />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
    </div>

    <div class="card">
      <el-table :data="rows" v-loading="loading">
        <el-table-column label="图片" width="92">
          <template #default="{ row }"><img :src="row.image" class="thumb" /></template>
        </el-table-column>
        <el-table-column prop="instrumentNo" label="乐器编号" min-width="128" />
        <el-table-column prop="name" label="名称" min-width="96" />
        <el-table-column prop="category" label="类别" min-width="76" />
        <el-table-column label="品牌型号" min-width="146">
          <template #default="{ row }">{{ row.brand }} {{ row.model }}</template>
        </el-table-column>
        <el-table-column prop="level" label="级别" width="76" />
        <el-table-column label="购置价" width="100">
          <template #default="{ row }"><span class="num">¥{{ money(row.purchasePrice) }}</span></template>
        </el-table-column>
        <el-table-column prop="conditionLevel" label="成色" width="82" />
        <el-table-column prop="location" label="仓位" width="90" />
        <el-table-column label="状态" width="92">
          <template #default="{ row }"><el-tag :type="instMeta(row.status).type" size="small">{{ instMeta(row.status).text }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link @click="openDetail(row)">详情</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" background layout="total, prev, pager, next, sizes" :total="total"
        v-model:current-page="q.pageNum" v-model:page-size="q.pageSize" :page-sizes="[10,20,50]" @change="load" />
    </div>

    <el-drawer v-model="editVisible" :title="form.id ? '编辑乐器' : '新增乐器'" size="560px">
      <el-form :model="form" label-width="106px">
        <el-form-item label="乐器编号"><el-input v-model="form.instrumentNo" :disabled="!!form.id" placeholder="如 INS-2026-0101" /></el-form-item>
        <el-form-item label="乐器名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="乐器类别">
          <el-select v-model="form.category" style="width:100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌"><el-input v-model="form.brand" /></el-form-item>
        <el-form-item label="型号"><el-input v-model="form.model" /></el-form-item>
        <el-form-item label="级别">
          <el-select v-model="form.level" style="width:100%">
            <el-option v-for="c in ['入门','进阶','专业']" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="尺寸规格"><el-input v-model="form.sizeSpec" /></el-form-item>
        <el-form-item label="购置价"><el-input v-model="form.purchasePrice" /></el-form-item>
        <el-form-item label="购置日期">
          <el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="当前成色">
          <el-select v-model="form.conditionLevel" style="width:100%">
            <el-option v-for="c in ['九成新','八成新','七成新','六成新']" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="随附配件"><el-input v-model="form.accessoryDesc" /></el-form-item>
        <el-form-item label="存放仓位"><el-input v-model="form.location" placeholder="如 A-01-03" /></el-form-item>
        <el-form-item label="乐器状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option v-for="(v, k) in INSTRUMENT_STATUS" :key="k" :label="v.text" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="外观/音准/部件">
          <div style="display:flex;gap:8px">
            <el-input v-model="form.appearanceScore" placeholder="外观" />
            <el-input v-model="form.pitchScore" placeholder="音准" />
            <el-input v-model="form.partScore" placeholder="部件" />
          </div>
        </el-form-item>
        <el-form-item label="乐器图片">
          <el-upload :show-file-list="false" :http-request="upload" accept="image/*">
            <img v-if="form.image" :src="form.image" class="thumb" style="width:120px;height:80px" />
            <el-button v-else size="small">上传图片</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-drawer>

    <el-drawer v-model="detailVisible" title="乐器详情" size="560px">
      <template v-if="detail">
        <img :src="detail.instrument.image" style="width:100%;border-radius:8px;margin-bottom:12px" />
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="编号">{{ detail.instrument.instrumentNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ instMeta(detail.instrument.status).text }}</el-descriptions-item>
          <el-descriptions-item label="累计租期">{{ detail.instrument.totalRentDays }} 天</el-descriptions-item>
          <el-descriptions-item label="维修次数">{{ detail.instrument.repairCount }} 次</el-descriptions-item>
          <el-descriptions-item label="最近保养">{{ detail.instrument.lastMaintainDate }}</el-descriptions-item>
          <el-descriptions-item label="状态分">{{ detail.instrument.scoreValue }}</el-descriptions-item>
        </el-descriptions>
        <div class="card-title mt">配件</div>
        <el-table :data="detail.accessories" size="small">
          <el-table-column prop="accessoryName" label="配件" /><el-table-column prop="quantity" label="数量" width="80" />
        </el-table>
        <div class="card-title mt">关联单据</div>
        <el-table :data="detail.orders" size="small">
          <el-table-column prop="orderNo" label="订单号" min-width="160" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ orderMeta(row.status).text }}</template>
          </el-table-column>
        </el-table>
        <div class="card-title mt">保养计划</div>
        <el-table :data="detail.maintains" size="small">
          <el-table-column prop="planDate" label="计划日期" width="110" />
          <el-table-column prop="content" label="内容" />
          <el-table-column prop="status" label="状态" width="90" />
        </el-table>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import { INSTRUMENT_STATUS, instMeta, money, orderMeta } from '../utils'

const categories = ['吉他', '钢琴', '弦乐', '管乐', '打击乐', '民乐', '键盘']
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const editVisible = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const q = reactive({ pageNum: 1, pageSize: 10, keyword: '', category: '', brand: '', condition: '', status: '' })
const form = reactive({})

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
  Object.assign(q, { pageNum: 1, keyword: '', category: '', brand: '', condition: '', status: '' })
  load()
}

function openEdit(row) {
  Object.keys(form).forEach((k) => delete form[k])
  if (row) Object.assign(form, row)
  else Object.assign(form, { status: 'AVAILABLE', category: '吉他', level: '进阶', conditionLevel: '九成新', image: '' })
  editVisible.value = true
}

async function upload({ file }) {
  const res = await api.upload(file, 'instrument')
  form.image = res.url
}

async function save() {
  await api.instrumentSave(form)
  ElMessage.success('已保存')
  editVisible.value = false
  load()
}

async function openDetail(row) {
  detail.value = await api.instrumentDetail(row.id)
  detailVisible.value = true
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除 ${row.instrumentNo}？已被单据引用的乐器不允许删除。`, '删除确认', { type: 'warning' })
  await api.instrumentDelete(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.thumb { width: 72px; height: 54px; object-fit: cover; border-radius: 4px; background: #E9EEF2; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
