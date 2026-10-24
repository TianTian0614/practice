<template>
  <div class="screen" v-loading="loading">
    <div class="screen-header">
      <div class="title">🌾 agri-assist 助农平台数据驾驶舱</div>
      <el-button size="small" plain @click="load" :loading="loading">刷新数据</el-button>
    </div>

    <!-- 指标卡 -->
    <div class="stat-row">
      <div class="stat-card" v-for="c in cards" :key="c.label">
        <div class="stat-value">{{ c.value }}</div>
        <div class="stat-label">{{ c.label }}</div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-grid">
      <div class="panel">
        <div class="panel-title">供需品类分布</div>
        <div ref="pieRef" class="chart"></div>
      </div>
      <div class="panel">
        <div class="panel-title">供需地区分布 Top8</div>
        <div ref="barRef" class="chart"></div>
      </div>
      <div class="panel">
        <div class="panel-title">近 7 日 AI 咨询趋势</div>
        <div ref="consultRef" class="chart"></div>
      </div>
      <div class="panel">
        <div class="panel-title">农产品价格趋势（{{ priceProduct }}）</div>
        <div ref="priceRef" class="chart"></div>
      </div>
    </div>

    <!-- 最新供需动态 -->
    <div class="panel list-panel">
      <div class="panel-title">最新供需动态</div>
      <el-table :data="recent" size="small" style="width: 100%"
                :header-cell-style="{ background: 'rgba(255,255,255,0.06)', color: '#c9e8cf', border: 'none' }"
                :row-style="{ background: 'transparent' }"
                :cell-style="{ color: '#d7ecdc', borderBottom: '1px solid rgba(255,255,255,0.08)' }">
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 'SUPPLY' ? 'success' : 'warning'" size="small" effect="dark">
              {{ row.type === 'SUPPLY' ? '供应' : '需求' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="category" label="品类" width="100" />
        <el-table-column prop="region" label="地区" width="120" />
        <el-table-column prop="createTime" label="发布日期" width="120" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import * as echarts from 'echarts'
import { dashboard } from '../api/stats'
import { trend as priceTrendApi, listProducts } from '../api/price'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const totals = ref({})
const recent = ref([])
const priceProduct = ref('')

const pieRef = ref(null)
const barRef = ref(null)
const consultRef = ref(null)
const priceRef = ref(null)

const cards = computed(() => [
  { label: '在售供应', value: totals.value.supply ?? 0 },
  { label: '发布需求', value: totals.value.demand ?? 0 },
  { label: '预售项目', value: totals.value.presale ?? 0 },
  { label: '注册用户', value: totals.value.user ?? 0 },
  { label: '社区帖子', value: totals.value.post ?? 0 },
  { label: 'AI 咨询', value: totals.value.consult ?? 0 },
  { label: '预售预约', value: totals.value.reservation ?? 0 }
])

const charts = []

const COLORS = ['#67c23a', '#95d475', '#b3e19d', '#f6c344', '#e6a23c', '#409eff', '#79bbff', '#a0cfff']

function init(el, option) {
  const c = echarts.init(el)
  c.setOption(option)
  charts.push(c)
  return c
}

const axisStyle = {
  axisLabel: { color: '#9fc3a8' },
  axisLine: { lineStyle: { color: 'rgba(255,255,255,0.15)' } },
  splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } }
}

async function load() {
  loading.value = true
  try {
    const [{ data: res }, ,] = await Promise.all([dashboard(), loadPriceProduct(), Promise.resolve()])
    if (res.code === 200) {
      totals.value = res.data.totals || {}
      recent.value = res.data.recentSupply || []
      renderCharts(res.data)
    }
    await renderPriceChart()
  } catch (e) {
    ElMessage.error('大屏数据加载失败：' + (e.message || '请确认后端已启动'))
  } finally {
    loading.value = false
  }
}

async function loadPriceProduct() {
  try {
    const { data } = await listProducts()
    if (data.code === 200 && data.data && data.data.length) priceProduct.value = data.data[0]
  } catch (e) { /* ignore */ }
}

function renderCharts(d) {
  // 品类饼图
  if (pieRef.value) {
    init(pieRef.value, {
      color: COLORS,
      tooltip: { trigger: 'item' },
      legend: { bottom: 0, textStyle: { color: '#9fc3a8' }, type: 'scroll' },
      series: [{
        type: 'pie', radius: ['38%', '65%'], center: ['50%', '44%'],
        itemStyle: { borderColor: '#0b1f13', borderWidth: 2 },
        label: { color: '#9fc3a8' },
        data: (d.supplyByCategory || []).map(x => ({ name: x.name, value: x.value }))
      }]
    })
  }
  // 地区柱图
  if (barRef.value) {
    const rg = d.supplyByRegion || []
    init(barRef.value, {
      tooltip: { trigger: 'axis' },
      grid: { left: 8, right: 16, top: 20, bottom: 0, containLabel: true },
      xAxis: { type: 'category', data: rg.map(x => x.name), ...axisStyle },
      yAxis: { type: 'value', ...axisStyle },
      series: [{
        type: 'bar', barWidth: 16, data: rg.map(x => x.value),
        itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#95d475' }, { offset: 1, color: '#2e7d32' }]), borderRadius: [4, 4, 0, 0] }
      }]
    })
  }
  // 咨询趋势折线
  if (consultRef.value) {
    const t = d.consultTrend || []
    init(consultRef.value, {
      tooltip: { trigger: 'axis' },
      grid: { left: 8, right: 16, top: 20, bottom: 0, containLabel: true },
      xAxis: { type: 'category', data: t.map(x => x.name), boundaryGap: false, ...axisStyle },
      yAxis: { type: 'value', minInterval: 1, ...axisStyle },
      series: [{
        type: 'line', smooth: true, symbolSize: 6, data: t.map(x => x.value),
        lineStyle: { color: '#f6c344', width: 2 },
        itemStyle: { color: '#f6c344' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(246,195,68,0.35)' }, { offset: 1, color: 'rgba(246,195,68,0)' }]) }
      }]
    })
  }
}

async function renderPriceChart() {
  if (!priceRef.value || !priceProduct.value) return
  try {
    const { data } = await priceTrendApi(priceProduct.value)
    if (data.code !== 200) return
    const t = data.data
    init(priceRef.value, {
      color: COLORS,
      tooltip: { trigger: 'axis' },
      legend: { bottom: 0, textStyle: { color: '#9fc3a8' } },
      grid: { left: 8, right: 16, top: 20, bottom: 30, containLabel: true },
      xAxis: { type: 'category', data: t.dates, boundaryGap: false, ...axisStyle },
      yAxis: { type: 'value', scale: true, ...axisStyle },
      series: (t.series || []).map(s => ({
        name: s.market, type: 'line', smooth: true, symbol: 'none', data: s.data
      }))
    })
  } catch (e) { /* 价格接口失败不影响大屏其余部分 */ }
}

function resizeAll() { charts.forEach(c => c.resize()) }

onMounted(() => {
  load()
  window.addEventListener('resize', resizeAll)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeAll)
  charts.forEach(c => c.dispose())
})
</script>

<style scoped>
.screen {
  background: linear-gradient(160deg, #0b1f13 0%, #0e2a18 60%, #0b1f13 100%);
  border-radius: 12px;
  padding: 20px;
  color: #d7ecdc;
  min-height: calc(100vh - 130px);
}
.screen-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.screen-header .title { font-size: 20px; font-weight: 700; letter-spacing: 2px; color: #e8f6ea; }

.stat-row { display: grid; grid-template-columns: repeat(7, 1fr); gap: 12px; margin-bottom: 16px; }
.stat-card {
  background: rgba(46, 125, 50, 0.12);
  border: 1px solid rgba(103, 194, 58, 0.25);
  border-radius: 10px; padding: 14px 8px; text-align: center;
}
.stat-value { font-size: 26px; font-weight: 700; color: #95d475; }
.stat-label { font-size: 12px; color: #9fc3a8; margin-top: 4px; }

.chart-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 14px; }
.panel {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(103, 194, 58, 0.18);
  border-radius: 10px; padding: 12px;
}
.panel-title { font-size: 14px; font-weight: 600; color: #c9e8cf; margin-bottom: 8px; padding-left: 8px; border-left: 3px solid #67c23a; }
.chart { height: 260px; }
.list-panel :deep(.el-table) { background: transparent; --el-table-tr-bg-color: transparent; --el-table-bg-color: transparent; }

@media (max-width: 900px) {
  .stat-row { grid-template-columns: repeat(4, 1fr); }
  .chart-grid { grid-template-columns: 1fr; }
}
</style>
