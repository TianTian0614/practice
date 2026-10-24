<template>
  <div style="max-width:760px;margin:0 auto;">
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <b>消息中心</b>
          <el-button size="small" :disabled="!items.length" @click="readAll">全部标记已读</el-button>
        </div>
      </template>
      <el-empty v-if="!items.length" description="暂无消息" />
      <div v-for="n in items" :key="n.id" class="ntf" :class="{ unread: !n.isRead }">
        <div class="ntf-head">
          <el-tag size="small" :type="tagType(n.type)">{{ tagText(n.type) }}</el-tag>
          <span class="ntf-title">{{ n.title }}</span>
          <span class="ntf-time">{{ fmt(n.createTime) }}</span>
        </div>
        <div class="ntf-content">{{ n.content }}</div>
        <div class="ntf-actions">
          <el-button v-if="!n.isRead" size="small" text @click="readOne(n)">标记已读</el-button>
          <el-button size="small" text type="danger" @click="del(n)">删除</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { list, markRead, markAll, remove } from '../api/notification'

const items = ref([])
async function load() {
  const { data } = await list(0, 50)
  items.value = (data.data?.content || []).map(n => ({ ...n, isRead: !!n.isRead }))
}
function tagType(t) { return t === 'TRADE' ? 'success' : t === 'INTERACTION' ? 'warning' : 'info' }
function tagText(t) { return t === 'TRADE' ? '交易' : t === 'INTERACTION' ? '互动' : '系统' }
function fmt(t) { return (t || '').replace('T', ' ').slice(0, 16) }
async function readOne(n) { await markRead(n.id); n.isRead = 1; ElMessage.success('已读') }
async function readAll() { await markAll(); items.value.forEach(n => n.isRead = 1); ElMessage.success('已全部已读') }
async function del(n) { await remove(n.id); items.value = items.value.filter(x => x.id !== n.id) }
onMounted(load)
</script>

<style scoped>
.ntf { border: 1px solid #ebeef5; border-radius: 8px; padding: 12px 14px; margin-bottom: 12px; background: #fff; }
.ntf.unread { border-color: #c6e2ff; background: #f5faff; }
.ntf-head { display: flex; align-items: center; gap: 10px; }
.ntf-title { font-weight: 600; }
.ntf-time { margin-left: auto; color: #999; font-size: 12px; }
.ntf-content { color: #606266; margin: 8px 0; line-height: 1.6; }
.ntf-actions { display: flex; justify-content: flex-end; gap: 8px; }
</style>
