<template>
  <div style="max-width:820px;margin:0 auto;">
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <b>我的订阅</b>
          <el-button size="small" type="primary" @click="showAdd=true">新增订阅</el-button>
        </div>
      </template>
      <el-empty v-if="!subs.length" description="订阅品类后，平台出现相关新供需会通知你" />
      <div v-for="s in subs" :key="s.id" class="sub">
        <span>品类：<b>{{ s.keyword || '全部' }}</b></span>
        <span v-if="s.region">地区：{{ s.region }}</span>
        <el-button size="small" text type="danger" style="margin-left:auto;" @click="del(s)">取消</el-button>
      </div>
    </el-card>
    <el-dialog v-model="showAdd" title="新增订阅" width="360px">
      <el-form label-width="60px">
        <el-form-item label="品类"><el-input v-model="form.keyword" placeholder="如：苹果" /></el-form-item>
        <el-form-item label="地区"><el-input v-model="form.region" placeholder="可选，如：山西" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd=false">取消</el-button>
        <el-button type="primary" @click="add">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { mySubs, subscribe, unsubscribe } from '../api/subscription'

const subs = ref([])
const showAdd = ref(false)
const form = ref({ keyword: '', region: '' })
async function load() { const { data } = await mySubs(); subs.value = data.data || [] }
async function add() {
  if (!form.value.keyword) { ElMessage.warning('请填写品类'); return }
  await subscribe(form.value.keyword, form.value.region)
  showAdd.value = false; form.value = { keyword: '', region: '' }
  ElMessage.success('已订阅'); load()
}
async function del(s) { await unsubscribe(s.id); subs.value = subs.value.filter(x => x.id !== s.id) }
onMounted(load)
</script>

<style scoped>
.sub { display: flex; align-items: center; gap: 16px; padding: 10px; border-bottom: 1px solid #f0f0f0; }
.sub:last-child { border-bottom: none; }
</style>
