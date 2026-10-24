<template>
  <div>
    <el-tabs v-model="tab">
      <!-- 发布与浏览 -->
      <el-tab-pane label="发布与浏览" name="browse">
        <el-card shadow="never">
          <template #header><b>发布供需信息</b></template>
          <el-form :model="form" label-width="70px" inline>
            <el-form-item label="类型">
              <el-select v-model="form.type" style="width:120px;">
                <el-option label="供应" value="SUPPLY" /><el-option label="需求" value="DEMAND" />
              </el-select>
            </el-form-item>
            <el-form-item label="品类"><el-input v-model="form.category" placeholder="如：苹果" /></el-form-item>
            <el-form-item label="标题"><el-input v-model="form.title" placeholder="标题" /></el-form-item>
            <el-form-item label="地区"><el-input v-model="form.region" placeholder="地区" /></el-form-item>
            <el-form-item label="价格"><el-input v-model.number="form.price" type="number" style="width:120px;" /></el-form-item>
            <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="1" /></el-form-item>
            <el-form-item><el-button type="primary" @click="doPublish">发布（待审核）</el-button></el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" style="margin-top:12px;">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:8px;">
              <b>供需列表</b>
              <div>
                <el-select v-model="query.type" placeholder="全部类型" clearable style="width:110px;" @change="onSearch">
                  <el-option label="供应" value="SUPPLY" /><el-option label="需求" value="DEMAND" />
                </el-select>
                <el-input v-model="query.category" placeholder="品类" style="width:120px;margin:0 8px;" @keyup.enter="onSearch" />
                <el-input v-model="query.region" placeholder="地区" style="width:120px;margin-right:8px;" @keyup.enter="onSearch" />
                <el-input v-model="keyword" placeholder="关键词" style="width:120px;margin-right:8px;" @keyup.enter="onSearch" />
                <el-button type="primary" @click="onSearch">搜索</el-button>
                <el-button @click="subscribeCategory">订阅该类目</el-button>
              </div>
            </div>
          </template>

          <el-empty v-if="!filtered.length" description="没有匹配的供需信息" />
          <el-table v-else :data="filtered" size="small" @row-click="openDetail" style="cursor:pointer;">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="type" label="类型" width="80" />
            <el-table-column prop="category" label="品类" width="100" />
            <el-table-column label="发布人" width="150">
              <template #default="{ row }">
                <span v-if="publishers[row.userId]">{{ publishers[row.userId].nickname }}（{{ publishers[row.userId].region }}）</span>
                <span v-else style="color:#999;">—</span>
              </template>
            </el-table-column>
            <el-table-column prop="region" label="地区" width="100" />
            <el-table-column prop="price" label="价格" width="90" />
            <el-table-column label="操作" width="170">
              <template #default="{ row }">
                <el-button text type="primary" size="small" @click.stop="doMatch(row.id)">智能匹配</el-button>
                <el-button text type="success" size="small" @click.stop="contact(row)">联系TA</el-button>
                <el-button text type="warning" size="small" @click.stop="toggleFavRow(row)">{{ favMap[row.id] ? '已收藏' : '收藏' }}</el-button>
                <el-button v-if="row.type==='SUPPLY'" text type="danger" size="small" @click.stop="openOrder(row)">下单</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div style="margin-top:10px;text-align:right;">
            <el-pagination layout="prev, pager, next" :total="total" :page-size="query.size"
                          :current-page="query.page + 1" @current-change="changePage" />
          </div>

          <div v-if="matchedId" style="margin-top:10px;">
            <small>匹配结果：</small>
            <el-tag v-for="m in matched[matchedId]" :key="m.id" style="margin:2px;">{{ m.title }} · {{ m.region }}</el-tag>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 我的发布 -->
      <el-tab-pane label="我的发布" name="mine">
        <el-card shadow="never">
          <template #header><b>我发布的供需（可进行编辑 / 下架 / 删除）</b></template>
          <el-empty v-if="!myData.length" description="你还没有发布任何供需信息" />
          <el-table v-else :data="myData" size="small">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="type" label="类型" width="80" />
            <el-table-column prop="category" label="品类" width="100" />
            <el-table-column prop="region" label="地区" width="100" />
            <el-table-column prop="price" label="价格" width="90" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }"><el-tag size="small" :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="260">
              <template #default="{ row }">
                <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
                <el-button v-if="row.status !== 3" text type="warning" size="small" @click="toggleOff(row)">下架</el-button>
                <el-button v-else text type="success" size="small" @click="toggleOn(row)">上架</el-button>
                <el-button text type="danger" size="small" @click="remove(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 详情抽屉 -->
    <el-drawer v-model="drawer" title="供需详情" size="32%">
      <template v-if="detail">
        <h3>{{ detail.title }}</h3>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="类型">{{ detail.type }}</el-descriptions-item>
          <el-descriptions-item label="品类">{{ detail.category }}</el-descriptions-item>
          <el-descriptions-item label="地区">{{ detail.region }}</el-descriptions-item>
          <el-descriptions-item label="价格">¥{{ detail.price }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ detail.description }}</el-descriptions-item>
          <el-descriptions-item label="发布人">
            <span v-if="publishers[detail.userId]">{{ publishers[detail.userId].nickname }}（{{ publishers[detail.userId].region }}）</span>
            <span v-else style="color:#999;">—</span>
          </el-descriptions-item>
        </el-descriptions>
        <el-button type="primary" style="margin-top:12px;" @click="doMatch(detail.id)">智能匹配</el-button>
        <el-button style="margin-top:12px;" @click="contact(detail)">联系发布人</el-button>
      </template>
    </el-drawer>

    <!-- 联系发布人 -->
    <el-dialog v-model="contactDlg" title="发布人联系方式" width="420px">
      <template v-if="contactUser">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="昵称">{{ contactUser.nickname }}</el-descriptions-item>
          <el-descriptions-item label="地区">{{ contactUser.region }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">
            {{ contactUser.contact ? contactUser.contact + '（已脱敏）' : '未填写' }}
          </el-descriptions-item>
        </el-descriptions>
        <el-button v-if="contactUser.contact" size="small" style="margin-top:8px;" @click="copyContact">复制联系方式</el-button>
        <p style="color:#999;margin-top:10px;font-size:12px;">平台仅提供脱敏联系方式，交易请双方自行沟通确认，切勿向陌生人转账。<br/>AI 农技建议仅供参考，用药施肥请遵循当地农技员指导。</p>

        <el-divider>对他的评价</el-divider>
        <div v-loading="ratingLoading">
          <el-empty v-if="!ratings.length" description="暂无评价" :image-size="60" />
          <div v-for="r in ratings" :key="r.id" class="rating-item">
            <el-rate :model-value="r.score" disabled size="small" />
            <span class="rating-comment">{{ r.comment || '（未填写评价内容）' }}</span>
            <span class="rating-time">{{ fmt(r.createTime) }}</span>
          </div>
          <el-button size="small" type="primary" style="margin-top:8px;" @click="openRate">我要评价</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑我的发布 -->
    <el-dialog v-model="editDlg" title="编辑供需信息" width="460px">
      <el-form :model="editForm" label-width="70px">
        <el-form-item label="类型">
          <el-select v-model="editForm.type" style="width:120px;">
            <el-option label="供应" value="SUPPLY" /><el-option label="需求" value="DEMAND" />
          </el-select>
        </el-form-item>
        <el-form-item label="品类"><el-input v-model="editForm.category" /></el-form-item>
        <el-form-item label="标题"><el-input v-model="editForm.title" /></el-form-item>
        <el-form-item label="地区"><el-input v-model="editForm.region" /></el-form-item>
        <el-form-item label="价格"><el-input v-model.number="editForm.price" type="number" style="width:120px;" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDlg=false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="saveEdit">保存（重新审核）</el-button>
      </template>
    </el-dialog>

    <!-- 发表评价 -->
    <el-dialog v-model="rateDlg" title="发表评价" width="420px">
      <el-form label-width="70px">
        <el-form-item label="评分"><el-rate v-model="rateForm.score" /></el-form-item>
        <el-form-item label="评价内容"><el-input v-model="rateForm.comment" type="textarea" :rows="3" placeholder="交易体验、守信情况等" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rateDlg=false">取消</el-button>
        <el-button type="primary" :loading="rateSaving" @click="submitRate">提交</el-button>
      </template>
    </el-dialog>

    <!-- 担保下单 -->
    <el-dialog v-model="orderDlg" title="担保下单（定金托管）" width="420px">
      <p style="color:#909399;font-size:13px;margin-top:0;">《{{ orderForm.title }}》— 平台提供担保交易：先付定金锁定货源，卖家发货后你确认收货，订单完成。</p>
      <el-form label-width="80px">
        <el-form-item label="数量"><el-input-number v-model="orderForm.quantity" :min="1" /></el-form-item>
        <el-form-item label="订单金额"><el-input v-model.number="orderForm.amount" type="number" style="width:140px;" /><span style="margin-left:6px;color:#909399;">元</span></el-form-item>
        <el-form-item label="定金"><el-input v-model.number="orderForm.deposit" type="number" style="width:140px;" /><span style="margin-left:6px;color:#909399;">元</span></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orderDlg=false">取消</el-button>
        <el-button type="primary" @click="submitOrder">创建订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { publish, list, match, myList, updateSupply, delSupply, offSupply, onSupply } from '../api/supply'
import { getPublic, me } from '../api/user'
import { listByUser, create as createRating, remove as removeRating } from '../api/rating'
import { toggleFav } from '../api/favorite'
import { subscribe } from '../api/subscription'
import { createOrder } from '../api/order'

const router = useRouter()
const tab = ref('browse')

const form = reactive({ type: 'SUPPLY', category: '', title: '', region: '', price: 0, description: '' })
const query = reactive({ type: '', category: '', region: '', page: 0, size: 10 })
const listData = ref([])
const total = ref(0)
const detail = ref(null)
const drawer = ref(false)
const matched = reactive({})
const matchedId = ref(null)
const keyword = ref('')
const publishers = reactive({})
const contactUser = ref(null)
const contactDlg = ref(false)
const contactTargetId = ref(null)
const ratings = ref([])
const ratingLoading = ref(false)

const myData = ref([])
const editDlg = ref(false)
const editSaving = ref(false)
const editForm = reactive({ id: null, type: 'SUPPLY', category: '', title: '', region: '', price: 0, description: '' })

const rateDlg = ref(false)
const rateSaving = ref(false)
const rateForm = reactive({ score: 5, comment: '' })

const myId = ref(null)
const favMap = reactive({})
const orderDlg = ref(false)
const orderForm = reactive({ id: null, title: '', quantity: 1, amount: 0, deposit: 0 })
const token = () => localStorage.getItem('token')

const filtered = computed(() => {
  if (!keyword.value.trim()) return listData.value
  const k = keyword.value.trim().toLowerCase()
  return listData.value.filter(s =>
    (s.title || '').toLowerCase().includes(k) || (s.description || '').toLowerCase().includes(k))
})

function statusType(s) { return s === 1 ? 'success' : s === 0 ? 'warning' : s === 2 ? 'danger' : 'info' }
function statusText(s) { return s === 1 ? '已上架' : s === 0 ? '待审核' : s === 2 ? '已驳回' : '已下架' }

async function loadPublishers(rows) {
  const ids = [...new Set(rows.map(r => r.userId).filter(Boolean))]
  for (const id of ids) {
    if (!publishers[id]) {
      try { const { data } = await getPublic(id); publishers[id] = data.data } catch (e) { /* 用户不存在则留空 */ }
    }
  }
}
async function load() {
  const { data } = await list({ ...query })
  listData.value = (data.data && data.data.content) || []
  total.value = (data.data && data.data.total) || 0
  await loadPublishers(listData.value)
}
function onSearch() { query.page = 0; load() }
function changePage(p) { query.page = p - 1; load() }
function openDetail(row) { detail.value = row; drawer.value = true }
async function doMatch(id) { const { data } = await match(id); matched[id] = data.data || []; matchedId.value = id }
function contact(row) {
  if (!localStorage.getItem('token')) { ElMessage.warning('登录后可查看发布人联系方式'); router.push('/login'); return }
  contactUser.value = publishers[row.userId] || null
  contactTargetId.value = row.id
  contactDlg.value = true
  loadRatings(row.userId)
}
async function loadRatings(userId) {
  ratingLoading.value = true
  try { const { data } = await listByUser(userId, 0, 20); ratings.value = data.data?.content || [] }
  catch (e) { ratings.value = [] } finally { ratingLoading.value = false }
}
function copyContact() {
  if (!contactUser.value || !contactUser.value.contact) return
  navigator.clipboard?.writeText(contactUser.value.contact)
  ElMessage.success('已复制联系方式')
}
function fmt(t) { return (t || '').replace('T', ' ').slice(0, 16) }
async function doPublish() {
  if (!form.title.trim()) { ElMessage.warning('请填写标题'); return }
  await publish({ ...form })
  ElMessage.success('已提交，等待管理员审核')
  form.title = ''; form.description = ''; form.category = ''; form.region = ''; form.price = 0
  onSearch()
}

// ===== 我的发布 =====
async function loadMine() {
  const { data } = await myList(0, 50)
  myData.value = data.data?.content || []
}
function openEdit(row) {
  Object.assign(editForm, { id: row.id, type: row.type, category: row.category, title: row.title, region: row.region, price: row.price, description: row.description })
  editDlg.value = true
}
async function saveEdit() {
  editSaving.value = true
  try {
    await updateSupply(editForm.id, { type: editForm.type, category: editForm.category, title: editForm.title, region: editForm.region, price: editForm.price, description: editForm.description })
    ElMessage.success('已保存，等待重新审核')
    editDlg.value = false; loadMine()
  } catch (e) { ElMessage.error('保存失败：' + (e.response?.data?.message || '请重试')) }
  finally { editSaving.value = false }
}
async function toggleOff(row) {
  await offSupply(row.id); ElMessage.success('已下架'); loadMine()
}
async function toggleOn(row) {
  await onSupply(row.id); ElMessage.success('已上架'); loadMine()
}
async function remove(row) {
  try {
    await ElMessageBox.confirm('确定删除该供需信息吗？', '提示', { type: 'warning' })
    await delSupply(row.id); ElMessage.success('已删除'); loadMine()
  } catch (e) { /* 取消 */ }
}

// ===== 评价 =====
function openRate() {
  if (!token()) { ElMessage.warning('登录后才能评价'); router.push('/login'); return }
  rateForm.score = 5; rateForm.comment = ''; rateDlg.value = true
}
async function submitRate() {
  if (!contactUser.value) return
  rateSaving.value = true
  try {
    await createRating({ toUserId: contactUser.value.id, targetId: contactTargetId.value, targetType: 'SUPPLY', score: rateForm.score, comment: rateForm.comment })
    ElMessage.success('评价成功')
    rateDlg.value = false
    await loadRatings(contactUser.value.id)
  } catch (e) { ElMessage.error('评价失败：' + (e.response?.data?.message || '请重试')) }
  finally { rateSaving.value = false }
}

// ===== 收藏 / 下单 / 订阅 =====
async function ensureMe() {
  if (myId.value != null) return
  try { const { data } = await me(); myId.value = data.data.id } catch (e) { /* 未登录 */ }
}
async function toggleFavRow(row) {
  if (!token()) { ElMessage.warning('登录后可收藏'); router.push('/login'); return }
  const { data } = await toggleFav('SUPPLY', row.id)
  favMap[row.id] = data.data.favored
  ElMessage.success(data.data.favored ? '已收藏' : '已取消收藏')
}
function openOrder(row) {
  if (!token()) { ElMessage.warning('登录后可下单'); router.push('/login'); return }
  if (row.type !== 'SUPPLY') { ElMessage.warning('仅可对“供应”类目下单'); return }
  if (row.userId === myId.value) { ElMessage.warning('不能对自己发布的供需下单'); return }
  orderForm.id = row.id; orderForm.title = row.title
  orderForm.quantity = 1; orderForm.amount = row.price || 0; orderForm.deposit = 0
  orderDlg.value = true
}
async function submitOrder() {
  await createOrder({ supplyId: orderForm.id, quantity: orderForm.quantity, amount: orderForm.amount, deposit: orderForm.deposit })
  ElMessage.success('订单已创建，请在“我的订单”支付定金')
  orderDlg.value = false
}
async function subscribeCategory() {
  if (!token()) { ElMessage.warning('登录后可订阅'); router.push('/login'); return }
  const kw = query.category.trim()
  if (!kw) { ElMessage.warning('请先在“品类”框输入要订阅的品类'); return }
  await subscribe(kw, query.region.trim())
  ElMessage.success('已订阅品类：' + kw + '，有新供需将通知你')
}

onMounted(() => { load(); if (token()) { loadMine(); ensureMe() } })
</script>

<style scoped>
.rating-item { padding: 6px 0; border-bottom: 1px dashed #ebeef5; }
.rating-comment { margin-left: 8px; color: #606266; }
.rating-time { float: right; color: #999; font-size: 12px; }
</style>
