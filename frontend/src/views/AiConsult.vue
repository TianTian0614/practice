<template>
  <el-row :gutter="16">
    <el-col :span="7">
      <el-card shadow="never" style="height:100%;">
        <template #header><b>咨询历史</b></template>
        <el-empty v-if="!history.length" description="暂无历史" :image-size="60" />
        <div v-for="(h,i) in history" :key="i" class="hist-item" @click="applyHistory(h)">
          <div class="hist-q">{{ h.question }}</div>
          <div class="hist-t">{{ h.time }}</div>
        </div>
        <el-button v-if="history.length" text type="danger" size="small" style="margin-top:8px;" @click="clearHistory">清空历史</el-button>
      </el-card>
    </el-col>
    <el-col :span="17">
      <el-card shadow="never">
        <template #header><b>AI 农技智能咨询（本地知识库 RAG）</b></template>
        <el-input v-model="question" type="textarea" :rows="3" placeholder="如：苹果树红蜘蛛如何防治？" />
        <el-button type="primary" style="margin-top:10px;" :loading="loading" @click="ask">咨询</el-button>
        <div v-if="answer" class="answer-box">
          <b>回答：</b>
          <p style="white-space:pre-wrap;">{{ displayAnswer }}</p>
          <small style="color:#666;">参考来源已基于本地知识库检索（deepseek-r1:1.5b 本地生成）</small>
        </div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { consult } from '../api/ai'

const question = ref('')
const answer = ref('')
const loading = ref(false)
const history = ref([])

const displayAnswer = computed(() => stripThink(answer.value))

function stripThink(t) {
  if (!t) return ''
  return t.replace(/<think>[\s\S]*?<\/think>/g, '').trim()
}
function loadHistory() {
  try { history.value = JSON.parse(localStorage.getItem('agri_ai_history') || '[]') } catch (e) { history.value = [] }
}
function saveHistory() {
  localStorage.setItem('agri_ai_history', JSON.stringify(history.value))
}
function applyHistory(h) { question.value = h.question; answer.value = h.answer }
function clearHistory() { history.value = []; localStorage.removeItem('agri_ai_history') }

async function ask() {
  if (!question.value.trim()) { ElMessage.warning('请输入问题'); return }
  loading.value = true
  try {
    const { data } = await consult(question.value)
    answer.value = data.data.answer
    history.value.unshift({ question: question.value, answer: answer.value, time: new Date().toLocaleString() })
    if (history.value.length > 50) history.value.pop()
    saveHistory()
  } catch (e) {
    ElMessage.error('咨询失败，请确认后端与 Ollama（deepseek-r1:1.5b）已启动')
  } finally {
    loading.value = false
  }
}
onMounted(loadHistory)
</script>

<style scoped>
.hist-item { padding: 8px; border-bottom: 1px solid #eee; cursor: pointer; }
.hist-item:hover { background: #f5f7f6; }
.hist-q { font-size: 13px; color: #222; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.hist-t { font-size: 11px; color: #999; }
.answer-box { margin-top: 14px; padding: 12px; background: #f1f8e9; border-radius: 8px; }
</style>
