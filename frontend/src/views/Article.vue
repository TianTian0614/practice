<template>
  <div>
    <div class="card" v-for="a in list" :key="a.id">
      <h3>{{ a.title }}</h3>
      <p style="white-space:pre-wrap;">{{ a.content }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listArticles } from '../api/content'
const list = ref([])
onMounted(async () => { const { data } = await listArticles(); list.value = data.data })
</script>
