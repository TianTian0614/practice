<template>
  <div style="max-width:900px;margin:0 auto;">
    <el-tabs v-model="tab">
      <el-tab-pane label="我买的" name="buy" />
      <el-tab-pane label="我卖的" name="sell" />
    </el-tabs>
    <el-empty v-if="!list.length" :description="tab==='buy'?'暂无采购订单':'暂无销售订单'" />
    <el-card v-for="o in list" :key="o.id" shadow="never" style="margin-bottom:12px;">
      <div style="display:flex;justify-content:space-between;align-items:center;">
        <div>
          <b>{{ o.title }}</b>
          <el-tag size="small" :type="statusType(o.status)" style="margin-left:8px;">{{ statusText(o.status) }}</el-tag>
        </div>
        <div style="color:#999;font-size:12px;">订单#{{ o.id }}</div>
      </div>
      <div style="color:#606266;margin:8px 0;font-size:13px;">
        金额 ¥{{ o.amount }} ｜ 定金 ¥{{ o.deposit }} ｜ 数量 {{ o.quantity }}
        <span v-if="o.expressNo"> ｜ 运单 {{ o.expressNo }}</span>
      </div>
      <div style="text-align:right;">
        <template v-if="tab==='buy'">
          <el-button v-if="o.status===0" size="small" type="primary" @click="pay(o)">付定金</el-button>
          <el-button v-if="o.status===0||o.status===1" size="small" @click="cancel(o)">取消</el-button>
          <el-button v-if="o.status===2" size="small" type="success" @click="confirm(o)">确认收货</el-button>
        </template>
        <template v-else>
          <el-button v-if="o.status===1" size="small" type="primary" @click="ship(o)">发货</el-button>
          <el-button v-if="o.status===0||o.status===1" size="small" @click="cancel(o)">取消</el-button>
        </template>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { myOrders, payOrder, shipOrder, confirmOrder, cancelOrder } from '../api/order'
import { me } from '../api/user'

const tab = ref('buy')
const all = ref([])
const meId = ref(null)
const list = computed(() => all.value.filter(o => tab.value === 'buy' ? o.buyerId === meId.value : o.sellerId === meId.value))

async function load() {
  const { data: u } = await me(); meId.value = u.data.id
  const { data } = await myOrders(); all.value = data.data || []
}
function statusText(s) { return ['待付定金', '已付定金', '已发货', '已完成', '已取消', '已退款'][s] || s }
function statusType(s) { return s === 3 ? 'success' : (s === 4 || s === 5) ? 'info' : s === 2 ? 'warning' : 'primary' }
async function pay(o) { await payOrder(o.id); ElMessage.success('已付定金'); load() }
async function confirm(o) { await confirmOrder(o.id); ElMessage.success('已确认收货'); load() }
async function ship(o) {
  const { value } = await ElMessageBox.prompt('请输入运单号（可留空）', '发货', { inputValue: o.expressNo || '' })
  await shipOrder(o.id, value); ElMessage.success('已发货'); load()
}
async function cancel(o) {
  await ElMessageBox.confirm('确认取消该订单？', '提示')
  await cancelOrder(o.id); ElMessage.success('已取消'); load()
}
onMounted(load)
</script>
