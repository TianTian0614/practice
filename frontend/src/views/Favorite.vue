<template>
  <div style="max-width:820px;margin:0 auto;">
    <el-card shadow="never">
      <template #header><b>我的收藏</b></template>
      <el-empty v-if="!items.length" description="还没有收藏任何内容" />
      <div v-for="f in items" :key="f.id" class="fav" @click="open(f)">
        <el-tag size="small">{{ typeText(f.targetType) }}</el-tag>
        <span class="ft">{{ f.title || ('编号' + f.targetId) }}</span>
        <el-button size="small" text type="danger" @click.stop="remove(f)">取消收藏</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { myFavorites, toggleFav } from '../api/favorite'

const router = useRouter()
const items = ref([])
async function load() { const { data } = await myFavorites(); items.value = data.data || [] }
function typeText(t) { return ({ SUPPLY: '供需', ARTICLE: '资讯', PRESALE: '预售', COMMUNITY: '社区' })[t] || t }
function open(f) { if (f.link) router.push(f.link) }
async function remove(f) { await toggleFav(f.targetType, f.targetId); items.value = items.value.filter(x => x.id !== f.id); ElMessage.success('已取消收藏') }
onMounted(load)
</script>

<style scoped>
.fav { display: flex; align-items: center; gap: 10px; padding: 10px; border-bottom: 1px solid #f0f0f0; cursor: pointer; }
.fav:last-child { border-bottom: none; }
.ft { flex: 1; font-weight: 500; }
</style>
