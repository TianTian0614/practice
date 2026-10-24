<template>
  <div style="max-width:900px;margin:0 auto;">
    <el-input v-model="q" placeholder="搜索供需 / 资讯 / 预售 / 社区" @keyup.enter="doSearch" style="margin-bottom:16px;">
      <template #append><el-button @click="doSearch">搜索</el-button></template>
    </el-input>
    <el-tabs v-model="tab">
      <el-tab-pane :label="'供需 (' + r.supplies.length + ')'" name="supplies" />
      <el-tab-pane :label="'资讯 (' + r.articles.length + ')'" name="articles" />
      <el-tab-pane :label="'预售 (' + r.presales.length + ')'" name="presales" />
      <el-tab-pane :label="'社区 (' + r.posts.length + ')'" name="posts" />
    </el-tabs>
    <el-empty v-if="!current.length" :description="'没有找到「' + q + '」相关结果'" />
    <el-card v-for="(item, i) in current" :key="i" shadow="never" style="margin-bottom:10px;">
      <b>{{ item.title || item.productName }}</b>
      <div v-if="summary(item)" style="color:#909399;font-size:12px;margin-top:4px;line-height:1.5;">
        {{ summary(item) }}
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { search } from '../api/search'

const route = useRoute()
const q = ref(route.query.q || '')
const tab = ref('supplies')
const r = reactive({ supplies: [], articles: [], presales: [], posts: [] })
const current = computed(() => r[tab.value] || [])
function summary(item) { return item.description || item.content || '' }

async function doSearch() {
  if (!q.value) return
  const { data } = await search(q.value)
  Object.assign(r, data.data)
  tab.value = r.supplies.length ? 'supplies'
    : r.articles.length ? 'articles'
    : r.presales.length ? 'presales' : 'posts'
}
watch(() => route.query.q, v => { q.value = v || ''; doSearch() })
onMounted(doSearch)
</script>
