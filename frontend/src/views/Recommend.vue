<template>
  <div style="max-width:900px;margin:0 auto;">
    <el-alert v-if="!personalized" type="info" :closable="false" style="margin-bottom:12px;">
      提示：登录后系统会根据你的「所在地区」与「订阅品类」为你精准推荐。当前为按「最新+热度」展示。
    </el-alert>

    <el-card shadow="never" style="margin-bottom:12px;">
      <template #header><b>🛒 最新供需</b></template>
      <el-empty v-if="!supply.length" description="暂无推荐" />
      <div v-for="s in supply" :key="s.id" class="rec-item" @click="go(s.link)">
        <div>
          <el-tag size="small" :type="s.type === 'SUPPLY' ? 'success' : 'danger'" effect="plain">
            {{ s.type === 'SUPPLY' ? '供应' : '需求' }}
          </el-tag>
          <b style="margin-left:6px;">{{ s.title }}</b>
          <span style="color:#909399;font-size:13px;margin-left:8px;">{{ s.category }} · {{ s.region }}</span>
          <span style="color:#c0c4cc;font-size:12px;margin-left:8px;">{{ fmtTime(s.createTime) }}</span>
        </div>
        <span style="color:#e6a23c;">匹配度 {{ s.score }}</span>
      </div>
    </el-card>

    <el-card shadow="never" style="margin-bottom:12px;">
      <template #header><b>📦 最新预售/预约</b></template>
      <el-empty v-if="!presales.length" description="暂无推荐" />
      <div v-for="p in presales" :key="p.id" class="rec-item" @click="go(p.link)">
        <div>
          <el-tag size="small" type="warning" effect="plain">{{ p.category }}</el-tag>
          <b style="margin-left:6px;">{{ p.title }}</b>
          <span style="color:#909399;font-size:13px;margin-left:8px;">{{ p.region }}</span>
          <span style="color:#c0c4cc;font-size:12px;margin-left:8px;">{{ fmtTime(p.createTime) }}</span>
        </div>
        <span style="color:#e6a23c;">匹配度 {{ p.score }}</span>
      </div>
    </el-card>

    <el-card shadow="never">
      <template #header><b>📚 最新农技资讯</b></template>
      <el-empty v-if="!articles.length" description="暂无推荐" />
      <div v-for="a in articles" :key="a.id" class="rec-item" @click="go(a.link)">
        <div>
          <el-tag size="small" type="info" effect="plain">{{ a.category }}</el-tag>
          <b style="margin-left:6px;">{{ a.title }}</b>
          <span style="color:#c0c4cc;font-size:12px;margin-left:8px;">{{ fmtTime(a.createTime) }}</span>
        </div>
        <span style="color:#e6a23c;">匹配度 {{ a.score }}</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { recommend } from '../api/recommend'

const router = useRouter()
const supply = ref([])
const presales = ref([])
const articles = ref([])
const personalized = ref(false)

function go(link) { if (link) router.push(link) }

// 把后端返回的发布时间（毫秒时间戳）格式化为「刚刚 / N天前 / 日期」
function fmtTime(epoch) {
  if (!epoch || epoch <= 0) return ''
  const now = Date.now()
  const diff = now - epoch
  if (diff < 0) return '刚刚'
  const day = 86_400_000
  if (diff < 60_000) return '刚刚'
  if (diff < 3_600_000) return Math.floor(diff / 60_000) + '分钟前'
  if (diff < day) return Math.floor(diff / 3_600_000) + '小时前'
  if (diff < 7 * day) return Math.floor(diff / day) + '天前'
  const d = new Date(epoch)
  const pad = (n) => (n < 10 ? '0' + n : '' + n)
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate())
}

onMounted(async () => {
  try {
    const { data } = await recommend(6)
    const d = data.data || {}
    supply.value = d.supply || []
    presales.value = d.presales || []
    articles.value = d.articles || []
    personalized.value = !!d.personalized
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.rec-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 6px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}
.rec-item:hover { background: #fafafa; }
.rec-item:last-child { border-bottom: none; }
</style>
