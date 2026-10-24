<template>
  <div class="login-wrap">
    <el-card class="login-card" shadow="never">
      <h2 style="margin-top:0;">登录 / 注册</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="昵称（注册时必填）" prop="nickname">
          <el-input v-model="form.nickname" placeholder="注册时填写" />
        </el-form-item>
        <el-form-item label="联系方式" prop="contact">
          <el-input v-model="form.contact" placeholder="手机号，便于他人联系您" />
        </el-form-item>
        <el-form-item label="所在地区" prop="region">
          <el-input v-model="form.region" placeholder="如：山西运城" />
        </el-form-item>
        <el-button type="primary" :loading="loading" @click="doLogin" style="width:100%;">登录</el-button>
        <el-button :loading="loading" @click="doRegister" style="width:100%;margin-left:0;margin-top:12px;">
          注册并登录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { login, register } from '../api/auth'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '', nickname: '', contact: '', region: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function doLogin() {
  try { await formRef.value.validate() } catch { return }
  loading.value = true
  try {
    const { data } = await login({ username: form.username, password: form.password })
    localStorage.setItem('token', data.data.token)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    ElMessage.error('登录失败：' + (e.response?.data?.message || '用户名或密码错误'))
  } finally {
    loading.value = false
  }
}

async function doRegister() {
  if (!form.nickname.trim()) { ElMessage.warning('注册请先填写昵称'); return }
  try { await formRef.value.validate() } catch { return }
  loading.value = true
  try {
    await register({ username: form.username, password: form.password, nickname: form.nickname, contact: form.contact, region: form.region })
    const { data } = await login({ username: form.username, password: form.password })
    localStorage.setItem('token', data.data.token)
    ElMessage.success('注册成功，已自动登录')
    router.push('/')
  } catch (e) {
    ElMessage.error('注册失败：' + (e.response?.data?.message || '请检查输入'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap { display: flex; justify-content: center; padding-top: 60px; }
.login-card { width: 380px; max-width: 100%; }
</style>
