<template>
  <div style="max-width:760px;margin:0 auto;">
    <el-card shadow="never">
      <template #header><b>我的认证徽章</b></template>
      <div v-if="badges.length" style="margin-bottom:8px;">
        <el-tag v-for="b in badges" :key="b" type="success" effect="dark" style="margin:0 6px 6px 0;">✓ {{ b }}</el-tag>
      </div>
      <el-alert v-else type="info" :closable="false">你还没有通过任何认证，申请认证后可在发布信息中展示信誉徽章，提升买家信任。</el-alert>
    </el-card>

    <el-card shadow="never" style="margin-top:12px;">
      <template #header><b>申请认证</b></template>
      <el-form :model="form" label-width="90px" v-loading="submitting">
        <el-form-item label="认证类型">
          <el-select v-model="form.type" placeholder="选择认证类型" style="width:240px;">
            <el-option v-for="t in CERT_TYPES" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="认证材料">
          <el-input v-model="form.material" type="textarea" :rows="3"
            :placeholder="materialTip" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :disabled="!form.type" @click="apply">提交申请</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top:12px;">
      <template #header><b>我的认证记录</b></template>
      <el-table :data="certs" empty-text="暂无记录">
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">{{ labelOf(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="material" label="材料" />
        <el-table-column prop="rejectReason" label="驳回原因" />
      </el-table>
    </el-card>

    <el-card v-if="isAdmin" shadow="never" style="margin-top:12px;">
      <template #header><b>管理员：待审核认证</b></template>
      <el-table :data="pending" empty-text="暂无待审核">
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">{{ labelOf(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="material" label="材料" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="review(row.id, true)">通过</el-button>
            <el-popconfirm title="填写驳回原因" @confirm="review(row.id, false, reason)">
              <template #reference><el-button size="small" type="danger">驳回</el-button></template>
              <template #actions="{ confirm }">
                <el-input v-model="reason" size="small" placeholder="原因" style="width:160px;" />
                <el-button size="small" type="danger" @click="confirm">确认驳回</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { me } from '../api/user'
import { applyCert, myCerts, pendingCerts, reviewCert } from '../api/cert'

const CERT_TYPES = [
  { value: 'REALNAME', label: '实名认证' },
  { value: 'ORIGIN', label: '产地认证' },
  { value: 'COOP', label: '合作社认证' },
  { value: 'EXPERT', label: '农技专家' },
  { value: 'QUALITY', label: '品质认证' }
]
const labelOf = (t) => (CERT_TYPES.find(x => x.value === t) || {}).label || t
const materialTip = computed(() => '如：实名认证请填写姓名/证件尾号；产地认证填写地块或合作社名称等')

const badges = ref([])
const certs = ref([])
const pending = ref([])
const submitting = ref(false)
const isAdmin = ref(false)
const reason = ref('')
const form = reactive({ type: '', material: '' })

async function loadMe() {
  try {
    const { data } = await me()
    const u = data.data || {}
    badges.value = u.badges || []
    isAdmin.value = u.role === 'ADMIN' || u.role === 'admin'
  } catch (e) { /* ignore */ }
}

async function load() {
  await loadMe()
  try {
    const { data } = await myCerts()
    certs.value = data.data || []
  } catch (e) { ElMessage.error('加载失败：' + (e.response?.data?.message || '')) }
  if (isAdmin.value) {
    try {
      const { data } = await pendingCerts()
      pending.value = data.data || []
    } catch (e) { /* ignore */ }
  }
}

const statusType = (s) => s === 'APPROVED' ? 'success' : s === 'REJECTED' ? 'danger' : 'warning'
const statusText = (s) => s === 'APPROVED' ? '已通过' : s === 'REJECTED' ? '已驳回' : '审核中'

async function apply() {
  submitting.value = true
  try {
    await applyCert(form.type, form.material)
    ElMessage.success('已提交，等待管理员审核')
    form.type = ''; form.material = ''
    await load()
  } catch (e) {
    ElMessage.error('提交失败：' + (e.response?.data?.message || ''))
  } finally { submitting.value = false }
}

async function review(id, approve, r) {
  try {
    await reviewCert(id, approve, r)
    ElMessage.success(approve ? '已通过' : '已驳回')
    reason.value = ''
    await load()
  } catch (e) { ElMessage.error('操作失败：' + (e.response?.data?.message || '')) }
}

onMounted(load)
</script>
