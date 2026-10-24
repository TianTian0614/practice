<template>
  <div>
    <el-card class="box-card" shadow="never">
      <template #header><b>发布农事求助</b></template>
      <el-form :model="form" label-width="60px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="如：番茄叶霉病怎么治" />
        </el-form-item>
        <el-form-item label="详情">
          <el-input v-model="form.content" type="textarea" :rows="2" placeholder="详细描述你遇到的问题" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doPost">发布求助</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-empty v-if="!posts.length" description="暂无求助帖，快来发布第一个吧" />
    <el-card v-for="p in posts" :key="p.id" class="box-card" shadow="hover">
      <div style="display:flex;justify-content:space-between;align-items:center;">
        <b>{{ p.title }}</b>
        <el-tag :type="p.status==='RESOLVED'?'success':'warning'" size="small">
          {{ p.status==='RESOLVED'?'已解决':'求助中' }}
        </el-tag>
      </div>
      <p style="color:#555;margin:8px 0;">{{ p.content }}</p>

      <el-button text type="primary" size="small" @click="toggleComments(p.id)">
        {{ expandId===p.id ? '收起评论' : '查看评论' }}
      </el-button>

      <div v-if="expandId===p.id" style="margin-top:10px;background:#f7f9f8;padding:10px;border-radius:8px;">
        <div v-for="c in commentsMap[p.id] || []" :key="c.id"
             :style="{padding:'6px 8px',borderBottom:'1px solid #eee',
                      background: c.id===p.bestCommentId ? '#e8f5e9' : 'transparent',
                      borderRadius:'4px'}">
          <span>{{ c.content }}</span>
          <el-button v-if="p.status!=='RESOLVED'" text type="success" size="small"
                     style="float:right;" @click="doAdopt(p.id, c.id)">采纳</el-button>
          <el-tag v-else-if="c.id===p.bestCommentId" type="success" size="small" style="float:right;">最佳答案</el-tag>
        </div>
        <el-input v-model="commentDraft[p.id]" placeholder="写下你的回答…" size="small" style="margin-top:8px;"
                  @keyup.enter="submitComment(p.id)">
          <template #append><el-button @click="submitComment(p.id)">发送</el-button></template>
        </el-input>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listPosts, addPost, listComments, addComment, adopt } from '../api/content'

const form = reactive({ title: '', content: '' })
const posts = ref([])
const commentsMap = reactive({})
const commentDraft = reactive({})
const expandId = ref(null)

async function loadPosts() {
  const { data } = await listPosts()
  posts.value = data.data || []
}
async function toggleComments(pid) {
  if (expandId.value === pid) { expandId.value = null; return }
  expandId.value = pid
  if (!commentsMap[pid]) {
    const { data } = await listComments(pid)
    commentsMap[pid] = data.data || []
  }
}
async function submitComment(pid) {
  const text = (commentDraft[pid] || '').trim()
  if (!text) return
  await addComment(pid, text)
  commentDraft[pid] = ''
  const { data } = await listComments(pid)
  commentsMap[pid] = data.data || []
  ElMessage.success('评论已发布')
}
async function doAdopt(pid, cid) {
  await adopt(pid, cid)
  ElMessage.success('已采纳为最佳答案')
  await loadPosts()
}
async function doPost() {
  if (!form.title.trim()) { ElMessage.warning('请填写标题'); return }
  await addPost({ ...form })
  form.title = ''; form.content = ''
  ElMessage.success('求助已发布')
  await loadPosts()
}
onMounted(loadPosts)
</script>
