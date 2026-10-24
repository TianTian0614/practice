<template>
  <div style="max-width:560px;margin:0 auto;">
    <el-card shadow="never">
      <template #header><b>个人资料</b></template>
      <el-form :model="form" label-width="90px" v-loading="loading">
        <el-form-item label="用户名">
          <el-input :model-value="form.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="昵称" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="form.contact" placeholder="手机号，便于他人联系您" />
        </el-form-item>
        <el-form-item label="所在地区">
          <el-input v-model="form.region" placeholder="如：山西运城" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存修改</el-button>
        </el-form-item>
      </el-form>
      <el-alert type="info" :closable="false" style="margin-top:8px;">
        填写联系方式与地区后，你发布的供需信息将展示“发布人”并可被他人联系。
      </el-alert>
    </el-card>

    <el-card shadow="never" style="margin-top:12px;">
      <template #header><b>我的信誉与等级</b></template>
      <div style="display:flex;align-items:center;gap:16px;flex-wrap:wrap;">
        <div>
          <span style="color:#909399;font-size:13px;">认证徽章：</span>
          <el-tag v-for="b in profile.badges" :key="b" type="success" effect="dark" style="margin:0 6px 6px 0;">✓ {{ b }}</el-tag>
          <router-link v-if="!profile.badges.length" to="/cert">暂无，去申请认证</router-link>
        </div>
        <el-divider direction="vertical" />
        <div>
          <span style="color:#909399;font-size:13px;">积分：</span>
          <b style="color:#e6a23c;">{{ profile.points }}</b>
          <el-tag type="warning" effect="plain" style="margin-left:6px;">Lv{{ profile.level }} · {{ profile.levelName }}</el-tag>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" style="margin-top:12px;">
      <template #header><b>我的交易与关注</b></template>
      <div style="display:flex;gap:12px;flex-wrap:wrap;">
        <router-link to="/order"><el-button>我的订单</el-button></router-link>
        <router-link to="/favorite"><el-button>我的收藏</el-button></router-link>
        <router-link to="/subscribe"><el-button>我的订阅</el-button></router-link>
        <router-link to="/cert"><el-button>我的认证</el-button></router-link>
        <router-link to="/point"><el-button>我的积分</el-button></router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { me, updateMe } from '../api/user'

const loading = ref(false)
const saving = ref(false)
const form = reactive({ username: '', nickname: '', contact: '', region: '' })
const profile = reactive({ badges: [], points: 0, level: 1, levelName: '新手农户' })

async function load() {
  loading.value = true
  try {
    const { data } = await me()
    const u = data.data || {}
    form.username = u.username || ''
    form.nickname = u.nickname || ''
    form.contact = u.contact || ''
    form.region = u.region || ''
    profile.badges = u.badges || []
    profile.points = u.points || 0
    profile.level = u.level || 1
    profile.levelName = u.levelName || '新手农户'
  } catch (e) {
    ElMessage.error('加载失败：' + (e.response?.data?.message || '请先登录'))
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    await updateMe({ nickname: form.nickname, contact: form.contact, region: form.region })
    ElMessage.success('已保存')
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || '请重试'))
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
