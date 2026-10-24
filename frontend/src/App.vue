<template>
  <div class="app-shell">
    <header class="topbar">
      <span class="logo">agri-assist 助农平台</span>
      <nav>
        <router-link to="/">首页</router-link>
        <router-link to="/supply">供需</router-link>
        <router-link to="/presale">预售</router-link>
        <router-link to="/price">价格行情</router-link>
        <router-link to="/trace">可信溯源</router-link>
        <router-link to="/article">农技资讯</router-link>
        <router-link to="/community">互助社区</router-link>
        <router-link to="/ai">AI咨询</router-link>
        <router-link to="/recommend">推荐</router-link>
        <router-link to="/stats">数据大屏</router-link>
        <router-link v-if="user" to="/message" class="msg-link">消息
          <el-badge v-if="unread > 0" :value="unread" :max="99" class="msg-badge" />
        </router-link>
        <router-link v-if="user" to="/order">订单</router-link>
        <router-link v-if="user" to="/favorite">收藏</router-link>
        <router-link v-if="user" to="/cert">认证</router-link>
        <router-link v-if="user" to="/point">积分</router-link>
        <router-link v-if="user" to="/mine">我的</router-link>
        <router-link v-if="isAdmin" to="/admin" class="admin-link">管理后台</router-link>
      </nav>
      <div class="user" v-if="user">
        <span>{{ user.nickname || user.username }}</span>
        <el-button size="small" text bg @click="logout">退出</el-button>
      </div>
      <router-link v-else to="/login" class="login-link">登录</router-link>
      <div class="search-box">
        <el-input v-model="kw" size="small" placeholder="搜索…" @keyup.enter="goSearch" style="width:160px;">
          <template #append><el-button @click="goSearch">搜索</el-button></template>
        </el-input>
      </div>
    </header>
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { me } from './api/user'
import { unreadCount } from './api/notification'

const router = useRouter()
const user = ref(null)
const unread = ref(0)
const kw = ref('')
const isAdmin = computed(() => !!(user.value && (user.value.role === 'ADMIN' || user.value.role === 'admin')))

function goSearch() {
  const q = kw.value.trim()
  if (q) router.push('/search?q=' + encodeURIComponent(q))
}

async function loadUser() {
  if (!localStorage.getItem('token')) return
  try {
    const { data } = await me()
    user.value = data.data
    await loadUnread()
  } catch (e) { /* token 失效，忽略 */ }
}

async function loadUnread() {
  if (!localStorage.getItem('token')) return
  try {
    const { data } = await unreadCount()
    unread.value = data.data || 0
  } catch (e) { /* 忽略 */ }
}

onMounted(loadUser)
watch(() => router.currentRoute.value.path, () => {
  if (localStorage.getItem('token') && !user.value) loadUser()
  else if (localStorage.getItem('token')) loadUnread()
})

function logout() {
  localStorage.removeItem('token')
  user.value = null
  router.push('/login')
}
</script>

<style scoped>
.login-link { color: #fff; text-decoration: none; margin-left: auto; }
.user { margin-left: auto; display: flex; align-items: center; gap: 8px; }
.search-box { margin-left: 14px; }
</style>
