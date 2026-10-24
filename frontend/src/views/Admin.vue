<template>
  <div v-if="!isAdmin">
    <el-result icon="warning" title="无访问权限" sub-title="管理后台仅管理员可见"></el-result>
  </div>
  <div v-else>
    <el-card shadow="never">
      <template #header><b>待审核供需信息</b></template>
      <el-empty v-if="!pending.length" description="暂无待审核内容" />
      <el-card v-for="s in pending" :key="s.id" class="box-card" shadow="hover">
        <div><b>{{ s.title }}</b> <el-tag size="small">{{ s.type }}</el-tag> {{ s.region }} ¥{{ s.price }}</div>
        <p style="color:#555;">{{ s.description }}</p>
        <el-button type="success" size="small" @click="doAudit('SUPPLY', s.id, 'APPROVE')">通过</el-button>
        <el-button type="danger" size="small" @click="doAudit('SUPPLY', s.id, 'REJECT')">驳回</el-button>
      </el-card>
    </el-card>

    <el-card shadow="never" style="margin-top:12px;">
      <template #header><b>审核日志</b></template>
      <el-table :data="logsList" size="small" stripe>
        <el-table-column prop="targetType" label="类型" width="110" />
        <el-table-column prop="targetId" label="目标ID" width="90" />
        <el-table-column prop="action" label="动作" width="90" />
        <el-table-column prop="note" label="备注" />
        <el-table-column prop="createTime" label="时间" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { me } from '../api/user'
import { logs as auditLogs, audit, supplyPending } from '../api/admin'

const isAdmin = ref(false)
const pending = ref([])
const logsList = ref([])

async function loadPending() {
  const { data } = await supplyPending()
  pending.value = data.data || []
}
async function loadLogs() { const { data } = await auditLogs(); logsList.value = data.data || [] }
async function doAudit(type, id, action) {
  await audit(type, id, action, '')
  ElMessage.success('已' + (action === 'APPROVE' ? '通过' : '驳回'))
  pending.value = pending.value.filter(s => s.id !== id)
  await loadLogs()
}
onMounted(async () => {
  try {
    const { data } = await me()
    const u = data.data
    isAdmin.value = !!(u && (u.role === 'ADMIN' || u.role === 'admin'))
  } catch (e) { isAdmin.value = false }
  if (isAdmin.value) { await loadPending(); await loadLogs() }
})
</script>
