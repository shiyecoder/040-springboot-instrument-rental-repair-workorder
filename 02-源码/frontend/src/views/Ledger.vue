<template>
  <div>
    <div class="pagehead">
      <div><h1>费用台账</h1><p>租金、押金、逾期费与赔偿金全部线下收取，系统只登记状态与凭证</p></div>
    </div>

    <div class="kpis" style="grid-template-columns:repeat(4,1fr)">
      <div class="kpi"><div class="lb">累计租金</div><div class="vl">¥{{ money(data.rent) }}</div></div>
      <div class="kpi"><div class="lb">在押押金</div><div class="vl">¥{{ money(data.deposit) }}</div></div>
      <div class="kpi"><div class="lb">逾期费</div><div class="vl" style="color:#B02A37">¥{{ money(data.overdue) }}</div></div>
      <div class="kpi"><div class="lb">赔偿金</div><div class="vl">¥{{ money(data.compensate) }}</div></div>
    </div>

    <div class="card">
      <div class="card-title">费用明细</div>
      <el-table :data="data.rows || []" size="small">
        <el-table-column prop="orderNo" label="订单号" min-width="170" />
        <el-table-column label="发生时间" min-width="160">
          <template #default="{ row }">{{ formatTime(row.time) }}</template>
        </el-table-column>
        <el-table-column prop="type" label="费用类型" width="100" />
        <el-table-column label="金额" width="120">
          <template #default="{ row }"><span class="num">¥{{ money(row.amount) }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'REFUNDED' ? 'success' : 'primary'" size="small">
              {{ row.status === 'REFUNDED' ? '已退还' : '已收取' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="说明" min-width="180">
          <template #default="{ row }">线下收取，凭证由门店留存</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { api } from '../api'
import { formatTime, money } from '../utils'

const data = reactive({ rent: 0, deposit: 0, overdue: 0, compensate: 0, rows: [] })

onMounted(async () => {
  Object.assign(data, await api.ledger())
})
</script>
