<template>
  <div>
    <el-card class="welcome" shadow="never" style="margin-bottom:16px;">
      <h2 style="margin:0 0 8px;">欢迎使用 agri-assist 助农平台</h2>
      <p style="margin:0;color:var(--ag-muted);">
        面向农户与采购方的一站式助农服务：供需对接、预售预约、农技资讯、互助社区、本地知识库 AI 咨询。
      </p>
    </el-card>

    <el-row :gutter="16">
      <el-col v-for="f in features" :key="f.to" :xs="24" :sm="12" :md="8" style="margin-bottom:16px;">
        <el-card shadow="hover" class="feat" @click="go(f.to)">
          <div class="feat-title">{{ f.title }}</div>
          <div class="feat-desc">{{ f.desc }}</div>
          <div class="feat-go">进入 →</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12" style="margin-bottom:16px;">
        <el-card>
          <template #header><b>最新供需</b></template>
          <div v-if="supplies.length" class="mini-list">
            <div v-for="s in supplies" :key="s.id" class="mini-item">
              <b>{{ s.title }}</b>
              <el-tag size="small" :type="s.type === 'SUPPLY' ? 'success' : 'warning'" style="margin:0 8px;">
                {{ s.type === 'SUPPLY' ? '供应' : '需求' }}
              </el-tag>
              <span class="muted">{{ s.region }} · ¥{{ s.price }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无供需信息" :image-size="56" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12" style="margin-bottom:16px;">
        <el-card>
          <template #header><b>最新农技资讯</b></template>
          <div v-if="articles.length" class="mini-list">
            <div v-for="a in articles" :key="a.id" class="mini-item">
              <b>{{ a.title }}</b>
            </div>
          </div>
          <el-empty v-else description="暂无资讯" :image-size="56" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { list as listSupply } from '../api/supply'
import { listArticles } from '../api/content'

const router = useRouter()
const supplies = ref([])
const articles = ref([])

const features = [
  { title: '供需对接', desc: '发布 / 智能匹配农产品供需', to: '/supply' },
  { title: '预售预约', desc: '农产品预售与名额预约', to: '/presale' },
  { title: '农技资讯', desc: '农业技术文章与政策', to: '/article' },
  { title: '互助社区', desc: '农户间农事经验互助', to: '/community' },
  { title: 'AI 智能咨询', desc: '基于本地知识库的问答', to: '/ai' }
]

function go(to) { router.push(to) }

onMounted(async () => {
  try {
    const { data } = await listSupply({})
    supplies.value = (data.data.content || []).slice(0, 5)
  } catch (e) { /* 忽略，展示空态 */ }
  try {
    const { data } = await listArticles()
    articles.value = (data.data || []).slice(0, 5)
  } catch (e) { /* 忽略，展示空态 */ }
})
</script>

<style scoped>
.feat { cursor: pointer; transition: transform .15s; }
.feat:hover { transform: translateY(-2px); }
.feat-title { font-size: 16px; font-weight: 600; color: var(--ag-primary); }
.feat-desc { color: var(--ag-muted); margin: 6px 0 12px; min-height: 40px; }
.feat-go { color: var(--ag-accent); font-weight: 600; }
.mini-list { display: flex; flex-direction: column; gap: 10px; }
.mini-item { display: flex; align-items: center; flex-wrap: wrap; gap: 4px; }
.muted { color: var(--ag-muted); font-size: 13px; }
</style>
