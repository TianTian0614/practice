<template>
  <div>
    <el-tabs v-model="tab">
      <!-- 预售大厅 -->
      <el-tab-pane label="预售大厅" name="hall">
        <el-card shadow="never">
          <template #header><b>发布农产品预售（先卖后收 · 以销定产）</b></template>
          <el-form :model="form" label-width="80px" inline>
            <el-form-item label="产品名"><el-input v-model="form.productName" placeholder="如：红心猕猴桃" /></el-form-item>
            <el-form-item label="价格"><el-input v-model.number="form.price" type="number" style="width:120px;" placeholder="元/单位" /></el-form-item>
            <el-form-item label="地区"><el-input v-model="form.region" placeholder="如：山西运城" /></el-form-item>
            <el-form-item label="品类"><el-input v-model="form.category" placeholder="如：水果" /></el-form-item>
            <el-form-item label="总名额"><el-input v-model.number="form.totalQuota" type="number" style="width:120px;" /></el-form-item>
            <el-form-item label="起订量"><el-input v-model.number="form.minOrder" type="number" style="width:120px;" /></el-form-item>
            <el-form-item label="发货时间"><el-date-picker v-model="form.harvestDate" type="date" value-format="YYYY-MM-DD" placeholder="预计发货" style="width:160px;" /></el-form-item>
            <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="1" placeholder="产品卖点" /></el-form-item>
            <el-form-item><el-button type="primary" @click="doPublish">发布预售</el-button></el-form-item>
          </el-form>
        </el-card>

        <h3 style="margin-top:14px;">预售列表</h3>
        <el-empty v-if="!listData.length" description="暂无预售" />
        <el-card v-for="p in listData" :key="p.id" class="box-card" shadow="hover" style="margin-bottom:10px;">
          <div style="display:flex;justify-content:space-between;flex-wrap:wrap;gap:8px;">
            <div>
              <b>{{ p.productName }}</b>
              <el-tag v-if="p.category" size="small" style="margin-left:6px;">{{ p.category }}</el-tag>
              <el-tag size="small" :type="psStatusType(p.status)">{{ psStatusText(p.status) }}</el-tag>
            </div>
            <div>
              <span style="color:#f56c6c;font-weight:600;">¥{{ p.price }}</span>
              <small style="color:#999;margin-left:6px;">/ {{ p.region || '地区未填' }}</small>
            </div>
          </div>
          <div style="margin:8px 0;">
            <el-progress :percentage="progress(p)" :stroke-width="10" />
            <small>已预约 {{ p.reservedCount || 0 }} / {{ p.totalQuota || 0 }}　起订量 {{ p.minOrder || 1 }}</small>
          </div>
          <div style="color:#909399;font-size:13px;">{{ p.description || '（无描述）' }}</div>
          <div v-if="p.harvestDate" style="color:#909399;font-size:12px;margin-top:4px;">预计发货：{{ p.harvestDate }}</div>
          <el-button v-if="p.status === 1" type="primary" size="small" style="margin-top:8px;" @click="openReserve(p)">我要预约</el-button>
        </el-card>
      </el-tab-pane>

      <!-- 我的预售（农户侧） -->
      <el-tab-pane label="我的预售" name="mine">
        <el-card shadow="never">
          <template #header><b>我发布的预售及预约管理</b></template>
          <el-empty v-if="!myPresales.length" description="你还没有发布预售" />
          <el-table v-else :data="myPresales" size="small" @expand-change="onExpand">
            <el-table-column type="expand">
              <template #default="{ row }">
                <el-table :data="reservations[row.id] || []" size="small">
                  <el-table-column prop="name" label="联系人" />
                  <el-table-column prop="phone" label="电话" />
                  <el-table-column prop="quantity" label="数量" width="70" />
                  <el-table-column label="状态" width="100">
                    <template #default="{ r }"><el-tag size="small" :type="resStatusType(r.status)">{{ resStatusText(r.status) }}</el-tag></template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template #default="{ r }">
                      <el-button v-if="r.status === 'RESERVED'" size="small" type="primary" @click="doShip(r)">发货</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-empty v-if="!(reservations[row.id] || []).length" description="暂无预约" :image-size="50" />
              </template>
            </el-table-column>
            <el-table-column prop="productName" label="产品" />
            <el-table-column prop="price" label="价格" width="90" />
            <el-table-column prop="region" label="地区" width="100" />
            <el-table-column prop="category" label="品类" width="90" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }"><el-tag size="small" :type="psStatusType(row.status)">{{ psStatusText(row.status) }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button v-if="row.status === 1" size="small" type="warning" @click="endPs(row)">结束</el-button>
                <el-button v-if="row.status === 1" size="small" type="danger" @click="cancelPs(row)">取消</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 我的预约（预约人侧） -->
      <el-tab-pane label="我的预约" name="myres">
        <el-card shadow="never">
          <template #header><b>我预约的预售</b></template>
          <el-empty v-if="!myRes.length" description="还没有预约记录" />
          <el-table v-else :data="myRes" size="small">
            <el-table-column label="产品">
              <template #default="{ row }">{{ psName(row.preSaleId) }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="name" label="联系人" width="100" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }"><el-tag size="small" :type="resStatusType(row.status)">{{ resStatusText(row.status) }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button v-if="row.status === 'SHIPPED'" size="small" type="success" @click="confirmRes(row)">确认收货</el-button>
                <el-button v-if="row.status === 'RESERVED'" size="small" type="danger" @click="cancelMyRes(row)">取消预约</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 预约弹窗 -->
    <el-dialog v-model="reserveDlg" title="预约预售" width="420px">
      <template v-if="reserveTarget">
        <p>产品：<b>{{ reserveTarget.productName }}</b>　价格：<span style="color:#f56c6c;">¥{{ reserveTarget.price }}</span></p>
        <el-form :model="reserveForm" label-width="70px">
          <el-form-item label="数量">
            <el-input-number v-model="reserveForm.quantity" :min="reserveTarget.minOrder || 1" />
          </el-form-item>
          <el-form-item label="联系人"><el-input v-model="reserveForm.name" placeholder="您的称呼" /></el-form-item>
          <el-form-item label="电话"><el-input v-model="reserveForm.phone" placeholder="便于农户联系您" /></el-form-item>
        </el-form>
        <p style="color:#999;font-size:12px;">预约成功后，农户将在“我的预售”看到您的信息并发货。请保持电话畅通。</p>
      </template>
      <template #footer>
        <el-button @click="reserveDlg = false">取消</el-button>
        <el-button type="primary" :loading="reserving" @click="submitReserve">提交预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { publish, list, reserve, myReservations, myPresales as apiMyPresales, reservationsOf,
         ship, confirm, cancelReservation, endPresale, cancelPresale } from '../api/presale'

const tab = ref('hall')
const form = reactive({ productName: '', price: 0, region: '', category: '', totalQuota: 0, minOrder: 1, harvestDate: '', description: '' })
const listData = ref([])
const myPresales = ref([])
const myRes = ref([])
const reservations = reactive({})
const psMap = reactive({})

const reserveDlg = ref(false)
const reserveTarget = ref(null)
const reserveForm = reactive({ quantity: 1, name: '', phone: '' })
const reserving = ref(false)

function progress(p) { const t = p.totalQuota || 0, r = p.reservedCount || 0; return t === 0 ? 0 : Math.min(100, Math.round(r / t * 100)) }
function psStatusType(s) { return s === 1 ? 'success' : s === 2 ? 'info' : 'danger' }
function psStatusText(s) { return s === 1 ? '进行中' : s === 2 ? '已结束' : '已取消' }
function resStatusType(s) { return s === 'RESERVED' ? 'warning' : s === 'SHIPPED' ? 'primary' : s === 'CONFIRMED' ? 'success' : 'info' }
function resStatusText(s) { return s === 'RESERVED' ? '已预约' : s === 'SHIPPED' ? '已发货' : s === 'CONFIRMED' ? '已确认' : '已取消' }
function psName(id) { return psMap[id] ? psMap[id].productName : ('预售#' + id) }

async function load() { const { data } = await list(); listData.value = data.data || []; listData.value.forEach(p => psMap[p.id] = p) }
async function loadMine() { const { data } = await apiMyPresales(); myPresales.value = data.data || []; myPresales.value.forEach(p => psMap[p.id] = p) }
async function loadMyRes() { const { data } = await myReservations(); myRes.value = data.data || [] }
async function loadReservations(preSaleId) {
  try { const { data } = await reservationsOf(preSaleId); reservations[preSaleId] = data.data || [] } catch (e) { reservations[preSaleId] = [] }
}
function onExpand(row, expanded) { if (expanded && !reservations[row.id]) loadReservations(row.id) }

async function doPublish() {
  if (!form.productName.trim()) { ElMessage.warning('请填写产品名'); return }
  await publish({ ...form })
  ElMessage.success('预售已发布')
  Object.assign(form, { productName: '', price: 0, region: '', category: '', totalQuota: 0, minOrder: 1, harvestDate: '', description: '' })
  await load(); await loadMine()
}

function openReserve(p) { reserveTarget.value = p; reserveForm.quantity = p.minOrder || 1; reserveForm.name = ''; reserveForm.phone = ''; reserveDlg.value = true }
async function submitReserve() {
  if (!reserveForm.name.trim() || !reserveForm.phone.trim()) { ElMessage.warning('请填写联系人与电话'); return }
  reserving.value = true
  try {
    await reserve(reserveTarget.value.id, reserveForm.quantity, reserveForm.name, reserveForm.phone)
    ElMessage.success('预约成功，请等待农户发货')
    reserveDlg.value = false; await load(); await loadMyRes()
  } catch (e) { ElMessage.error('预约失败：' + (e.response?.data?.message || '请重试')) }
  finally { reserving.value = false }
}

async function doShip(r) { await ship(r.id); ElMessage.success('已发货'); await loadReservations(r.preSaleId) }
async function confirmRes(r) { await confirm(r.id); ElMessage.success('已确认收货'); await loadMyRes() }
async function cancelMyRes(r) {
  try { await ElMessageBox.confirm('确定取消该预约吗？', '提示', { type: 'warning' }); await cancelReservation(r.id); ElMessage.success('已取消'); await loadMyRes() } catch (e) {}
}
async function endPs(p) { await endPresale(p.id); ElMessage.success('预售已结束'); await loadMine() }
async function cancelPs(p) { await cancelPresale(p.id); ElMessage.success('预售已取消'); await loadMine() }

onMounted(() => { load(); if (localStorage.getItem('token')) { loadMine(); loadMyRes() } })
</script>
