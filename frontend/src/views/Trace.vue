<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <h2>🔍 农产品溯源</h2>
      <div class="header-actions">
        <el-input v-model="keyword" placeholder="输入溯源码 / 产品名搜索" clearable style="width: 240px" />
        <el-button type="primary" @click="showAddDialog = true">+ 录入溯源环节</el-button>
      </div>
    </div>

    <el-alert type="info" :closable="false" show-icon style="margin-bottom: 14px"
              title="每个批次拥有唯一溯源码，扫码即可查看从种植到交付的完整流转链，实现来源可查、去向可追。" />
    <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 14px"
              title="当前溯源批次为系统演示数据，用于展示溯源链路机制。真实运行需由生产、加工、物流、销售各环节主体录入并对接第三方认证。" />

    <div class="trace-layout">
      <!-- 左：批次列表 -->
      <div class="batch-list">
        <el-card v-for="b in filteredBatches" :key="b.batchNo" shadow="hover"
                 class="batch-card" :class="{ active: b.batchNo === currentBatch }"
                 @click="selectBatch(b.batchNo)">
          <div class="batch-name">
            {{ b.productName }}
            <el-tag v-if="b.isDemo === 1" size="small" type="info" effect="plain" class="demo-tag">演示</el-tag>
          </div>
          <div class="batch-no">溯源码：{{ b.batchNo }}</div>
          <div class="batch-meta">
            <el-tag size="small" type="success" effect="plain">{{ b.stepCount }} 个环节</el-tag>
            <span class="batch-date">{{ (b.lastTime || '').slice(0, 10) }}</span>
          </div>
        </el-card>
        <el-empty v-if="!filteredBatches.length" description="暂无溯源批次" :image-size="80" />
      </div>

      <!-- 右：溯源时间线 -->
      <div class="timeline-panel">
        <template v-if="records.length">
          <div class="timeline-head">
            <span class="product">{{ records[0].productName }}</span>
            <el-tag size="small" effect="dark" type="success">溯源码 {{ currentBatch }}</el-tag>
            <el-tag v-if="currentIsDemo === 1" size="small" type="info" effect="plain">演示数据</el-tag>
          </div>
          <el-timeline>
            <el-timeline-item v-for="(r, i) in records" :key="r.id"
                              :timestamp="fmt(r.occurredAt)"
                              :type="i === records.length - 1 ? 'success' : 'primary'"
                              :hollow="i !== records.length - 1"
                              size="large">
              <div class="step-name">{{ i + 1 }}. {{ r.step }}</div>
              <div class="step-detail" v-if="r.detail">{{ r.detail }}</div>
              <div class="step-meta">
                <span v-if="r.location">📍 {{ r.location }}</span>
                <span v-if="r.operator">👤 {{ r.operator }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
        </template>
        <el-empty v-else description="选择左侧批次查看完整溯源链" />
      </div>
    </div>

    <!-- 录入环节 -->
    <el-dialog v-model="showAddDialog" title="录入溯源环节" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="溯源批次号" required>
          <el-input v-model="form.batchNo" placeholder="如 APL-20261006-001" />
        </el-form-item>
        <el-form-item label="产品名称" required>
          <el-input v-model="form.productName" placeholder="如 红富士苹果" />
        </el-form-item>
        <el-form-item label="环节名称" required>
          <el-select v-model="form.step" allow-create filterable default-first-option style="width: 100%"
                     placeholder="选择或输入环节">
            <el-option v-for="s in steps" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="环节描述">
          <el-input v-model="form.detail" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="发生地点">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="经手主体">
          <el-input v-model="form.operator" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAdd">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { listBatches, queryTrace, addTrace } from '../api/trace'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const batches = ref([])
const records = ref([])
const currentBatch = ref('')
const keyword = ref('')
const showAddDialog = ref(false)
const steps = ['种植基地管理', '采摘分拣', '收割晾晒', '加工处理', '质量检测', '分级筛选', '冷链配送', '入库仓储', '到货上架', '签收交付']

const form = ref({ batchNo: '', productName: '', step: '', detail: '', location: '', operator: '' })

const filteredBatches = computed(() => {
  if (!keyword.value.trim()) return batches.value
  const k = keyword.value.trim().toLowerCase()
  return batches.value.filter(b =>
    b.batchNo.toLowerCase().includes(k) || (b.productName || '').toLowerCase().includes(k))
})

const currentIsDemo = computed(() => {
  const b = batches.value.find(x => x.batchNo === currentBatch.value)
  return b ? b.isDemo : 0
})

function fmt(t) { return (t || '').replace('T', ' ').slice(0, 16) }

async function loadBatches() {
  loading.value = true
  try {
    const { data } = await listBatches()
    if (data.code === 200) {
      batches.value = data.data || []
      if (batches.value.length && !currentBatch.value) {
        await selectBatch(batches.value[0].batchNo)
      }
    }
  } catch (e) {
    ElMessage.error('批次列表加载失败：' + (e.message || ''))
  } finally {
    loading.value = false
  }
}

async function selectBatch(batchNo) {
  currentBatch.value = batchNo
  try {
    const { data } = await queryTrace(batchNo)
    if (data.code === 200) records.value = data.data || []
  } catch (e) {
    ElMessage.error('溯源链查询失败：' + (e.message || ''))
  }
}

async function submitAdd() {
  const f = form.value
  if (!f.batchNo || !f.productName || !f.step) {
    ElMessage.warning('请填写批次号、产品名称和环节名称')
    return
  }
  submitting.value = true
  try {
    const { data } = await addTrace(f)
    if (data.code === 200) {
      ElMessage.success('溯源环节已录入')
      showAddDialog.value = false
      form.value = { batchNo: f.batchNo, productName: f.productName, step: '', detail: '', location: '', operator: '' }
      await loadBatches()
      if (currentBatch.value === f.batchNo) await selectBatch(f.batchNo)
    } else {
      ElMessage.error(data.message || '录入失败')
    }
  } catch (e) {
    ElMessage.error('录入失败：' + (e.message || ''))
  } finally {
    submitting.value = false
  }
}

onMounted(loadBatches)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; flex-wrap: wrap; gap: 10px; }
.page-header h2 { margin: 0; }
.header-actions { display: flex; gap: 10px; }

.trace-layout { display: grid; grid-template-columns: 320px 1fr; gap: 16px; align-items: start; }
.batch-list { display: flex; flex-direction: column; gap: 10px; max-height: 640px; overflow-y: auto; }
.batch-card { cursor: pointer; transition: all .2s; }
.batch-card:hover { transform: translateY(-2px); }
.batch-card.active { border-color: var(--el-color-success); box-shadow: 0 0 0 2px rgba(103, 194, 58, 0.2); }
.batch-name { font-weight: 700; font-size: 15px; display: flex; align-items: center; gap: 6px; }
.demo-tag { margin-left: 2px; transform: scale(0.85); transform-origin: left center; }
.batch-no { font-size: 12px; color: #888; margin: 4px 0; font-family: monospace; }
.batch-meta { display: flex; justify-content: space-between; align-items: center; }
.batch-date { font-size: 12px; color: #aaa; }

.timeline-panel { background: #fff; border-radius: 10px; padding: 18px 22px; min-height: 400px; border: 1px solid #e8f0e9; }
.timeline-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }
.timeline-head .product { font-size: 17px; font-weight: 700; }
.step-name { font-weight: 600; font-size: 15px; }
.step-detail { color: #666; font-size: 13px; margin-top: 4px; line-height: 1.6; }
.step-meta { display: flex; gap: 16px; margin-top: 6px; color: #999; font-size: 12px; }

@media (max-width: 900px) { .trace-layout { grid-template-columns: 1fr; } }
</style>
