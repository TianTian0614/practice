<template>
  <div style="max-width:760px;margin:0 auto;">
    <el-card shadow="never">
      <template #header><b>社区激励 · 积分中心</b></template>
      <div style="display:flex;align-items:center;gap:24px;">
        <div style="text-align:center;">
          <div style="font-size:28px;font-weight:bold;color:#e6a23c;">{{ info.points }}</div>
          <div style="color:#909399;font-size:13px;">当前积分</div>
        </div>
        <el-divider direction="vertical" />
        <div style="text-align:center;">
          <el-tag size="large" type="warning" effect="dark">Lv{{ info.level }} · {{ info.levelName }}</el-tag>
          <div style="color:#909399;font-size:13px;margin-top:4px;">等级越高，信誉越好</div>
        </div>
        <el-divider direction="vertical" />
        <div style="color:#909399;font-size:13px;line-height:1.8;">
          获取方式：发布互助帖 <b>+5</b> · 参与评论 <b>+2</b> · 回答被选为最佳 <b>+10</b>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" style="margin-top:12px;">
      <template #header><b>积分明细</b></template>
      <el-table :data="info.logs" empty-text="暂无积分记录">
        <el-table-column prop="reason" label="来源" width="140" />
        <el-table-column prop="delta" label="变动" width="90">
          <template #default="{ row }"><span style="color:#67c23a;">+{{ row.delta }}</span></template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" />
      </el-table>
    </el-card>

    <el-card shadow="never" style="margin-top:12px;">
      <template #header><b>助农积分榜</b></template>
      <el-table :data="rank" empty-text="暂无数据">
        <el-table-column label="排名" width="70">
          <template #default="{ $index }"><b>{{ $index + 1 }}</b></template>
        </el-table-column>
        <el-table-column prop="nickname" label="用户" />
        <el-table-column prop="region" label="地区" width="140" />
        <el-table-column prop="points" label="积分" width="90" />
        <el-table-column label="等级" width="130">
          <template #default="{ row }"><el-tag type="warning" effect="plain">Lv{{ row.level }} · {{ row.levelName }}</el-tag></template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { myPoints, pointRank } from '../api/point'

const info = reactive({ points: 0, level: 1, levelName: '新手农户', logs: [] })
const rank = ref([])

async function load() {
  try {
    const { data } = await myPoints()
    const d = data.data || {}
    info.points = d.points || 0
    info.level = d.level || 1
    info.levelName = d.levelName || '新手农户'
    info.logs = d.logs || []
  } catch (e) { ElMessage.error('加载失败：' + (e.response?.data?.message || '')) }
  try {
    const { data } = await pointRank(20)
    rank.value = data.data || []
  } catch (e) { /* ignore */ }
}

onMounted(load)
</script>
