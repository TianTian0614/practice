<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <h2>💰 农产品价格行情 / 比价</h2>
      <div class="header-actions">
        <el-select v-model="product" style="width: 200px" placeholder="选择农产品" @change="loadTrend">
          <el-option v-for="p in products" :key="p" :label="p" :value="p" />
        </el-select>
        <el-button type="primary" @click="showAddDialog = true">+ 新增报价</el-button>
      </div>
    </div>

    <!-- 价格趋势 -->
    <div class="panel">
      <div class="panel-title">「{{ product || '—' }}」近 14 日各渠道价格趋势（{{ unitHint }}）</div>
      <div ref="chartRef" class="chart"></div>
    </div>

    <!-- 最新比价表 -->
    <div class="panel">
      <div class="panel-title">最新比价一览（各渠道最新报价，绿色为最低价渠道）</div>
      <div class="compare-grid">
        <div v-for="row in latestData" :key="row.product" class="compare-card">
          <div class="compare-head">
            <span class="compare-product">{{ row.product }}</span>
            <span class="compare-unit">{{ row.unit }}</span>
          </div>
          <div v-for="m in row.markets" :key="m.market" class="compare-row" :class="{ lowest: m.lowest }">
            <span class="market-name">
              <el-tag v-if="m.lowest" type="success" size="small" effect="dark">最低</el-tag>
              {{ m.market }}
            </span>
            <span class="market-price">¥{{ m.price }}</span>
            <span class="market-date">{{ m.quoteDate }}</span>
          </div>
        </div>
      </div>
      <el-empty v-if="!latestData.length" description="暂无报价数据" :image-size="80" />
    </div>

    <!-- 新增报价 -->
    <el-dialog v-model="showAddDialog" title="新增价格报价" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="产品名称" required>
          <el-select v-model="form.productName" allow-create filterable default-first-option style="width: 100%"
                     placeholder="选择已有产品或输入新品">
            <el-option v-for="p in products" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="市场/渠道" required>
          <el-select v-model="form.market" allow-create filterable default-first-option style="width: 100%"
                     placeholder="选择或输入渠道">
            <el-option v-for="m in markets" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="0.5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计价单位" required>
          <el-select v-model="form.unit" allow-create filterable default-first-option style="width: 100%">
            <el-option label="元/斤" value="元/斤" />
            <el-option label="元/箱" value="元/箱" />
            <el-option label="元/袋" value="元/袋" />
            <el-option label="元/公斤" value="元/公斤" />
          </el-select>
        </el-form-item>
        <el-form-item label="报价日期" required>
          <el-date-picker v-model="form.quoteDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
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
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { listProducts, trend, latest, addQuote } from '../api/price'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const products = ref([])
const product = ref('')
const latestData = ref([])
const showAddDialog = ref(false)
const markets = ['产地直销', '本地批发市场', '商超零售', '电商平台']
const chartRef = ref(null)
const trendData = ref({ dates: [], series: [] })

const today = new Date().toISOString().slice(0, 10)
const form = ref({ productName: '', market: '', price: 5, unit: '元/斤', quoteDate: today })

const unitHint = computed(() => {
  const first = latestData.value.find(r => r.product === product.value)
  return first ? first.unit : ''
})

const COLORS = ['#2e7d32', '#f6c344', '#409eff', '#e6a23c', '#909399']
const axisStyle = {
  axisLabel: { color: '#888' },
  axisLine: { lineStyle: { color: '#ddd' } },
  splitLine: { lineStyle: { color: '#f0f0f0' } }
}

let chart = null

async function loadAll() {
  loading.value = true
  try {
    const [p, l] = await Promise.all([listProducts(), latest()])
    if (p.data.code === 200) products.value = p.data.data || []
    if (l.data.code === 200) latestData.value = l.data.data || []
    if (!product.value && products.value.length) product.value = products.value[0]
    await loadTrend()
  } catch (e) {
    ElMessage.error('行情加载失败：' + (e.message || '请确认后端已启动'))
  } finally {
    loading.value = false
  }
}

async function loadTrend() {
  if (!product.value) return
  try {
    const { data } = await trend(product.value)
    if (data.code === 200) {
      trendData.value = data.data || { dates: [], series: [] }
      await nextTick()
      renderChart()
    }
  } catch (e) {
    ElMessage.error('趋势加载失败：' + (e.message || ''))
  }
}

function renderChart() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  const t = trendData.value
  chart.setOption({
    color: COLORS,
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0 },
    grid: { left: 8, right: 16, top: 20, bottom: 40, containLabel: true },
    xAxis: { type: 'category', data: t.dates, boundaryGap: false, ...axisStyle },
    yAxis: { type: 'value', scale: true, ...axisStyle },
    series: (t.series || []).map(s => ({
      name: s.market, type: 'line', smooth: true, symbol: 'circle', symbolSize: 5, data: s.data
    }))
  }, true)
}

async function submitAdd() {
  const f = form.value
  if (!f.productName || !f.market || !f.price || !f.quoteDate) {
    ElMessage.warning('请完整填写报价信息')
    return
  }
  submitting.value = true
  try {
    const { data } = await addQuote(f)
    if (data.code === 200) {
      ElMessage.success('报价已提交')
      showAddDialog.value = false
      await loadAll()
      if (!products.value.includes(f.productName)) products.value.push(f.productName)
      product.value = f.productName
      await loadTrend()
    } else {
      ElMessage.error(data.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('提交失败：' + (e.message || ''))
  } finally {
    submitting.value = false
  }
}

function resize() { if (chart) chart.resize() }

onMounted(() => {
  loadAll()
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  if (chart) chart.dispose()
})
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; flex-wrap: wrap; gap: 10px; }
.page-header h2 { margin: 0; }
.header-actions { display: flex; gap: 10px; }

.panel { background: #fff; border-radius: 10px; padding: 16px; border: 1px solid #e8f0e9; margin-bottom: 16px; }
.panel-title { font-weight: 600; margin-bottom: 12px; padding-left: 10px; border-left: 3px solid var(--el-color-primary); }
.chart { height: 320px; }

.compare-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 14px; }
.compare-card { border: 1px solid #e8f0e9; border-radius: 10px; padding: 12px 14px; background: #fafcfa; }
.compare-head { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 8px; }
.compare-product { font-weight: 700; font-size: 15px; }
.compare-unit { font-size: 12px; color: #999; }
.compare-row { display: flex; align-items: center; justify-content: space-between; padding: 7px 8px; border-radius: 6px; font-size: 13px; }
.compare-row.lowest { background: rgba(103, 194, 58, 0.12); }
.market-name { display: flex; align-items: center; gap: 6px; color: #555; }
.market-price { font-weight: 700; color: #333; }
.compare-row.lowest .market-price { color: var(--el-color-success); }
.market-date { font-size: 12px; color: #aaa; }
</style>
