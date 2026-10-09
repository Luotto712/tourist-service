<template>
  <div v-loading="loading">
    <el-row :gutter="20">
      <el-col v-for="card in cards" :key="card.key" :span="6">
        <el-card class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value" :style="{ color: card.color }">{{ data[card.key] ?? 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <div ref="pieRef" class="chart" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div ref="barRef" class="chart" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { getOverview, getComplaintStatus, getEmergencyStatus } from '@/api/statistics'
import type { StatsMap } from '@/api/statistics'
import type { ECharts } from 'echarts'

const loading = ref(false)
const data = reactive<StatsMap>({})
const cards = [
  { key: 'totalComplaints', label: '投诉总数', color: '#409eff' },
  { key: 'pendingComplaints', label: '待审批投诉', color: '#e6a23c' },
  { key: 'processingComplaints', label: '处理中投诉', color: '#67c23a' },
  { key: 'closedComplaints', label: '已结案投诉', color: '#909399' }
]

const statusLabel: Record<string, string> = {
  PENDING: '待审批',
  APPROVED: '已通过',
  REJECTED: '未通过',
  PROCESSING: '处理中',
  RESOLVED: '处理完成',
  CONFIRMED: '已确认',
  CLOSED: '已结案'
}
const eStatusLabel: Record<string, string> = {
  PENDING: '待审批',
  APPROVED: '已发布',
  REJECTED: '已驳回'
}

const pieRef = ref(null)
const barRef = ref(null)
let pieChart: ECharts | null = null
let barChart: ECharts | null = null

onMounted(async () => {
  window.addEventListener('resize', onResize)
  await fetchData()
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  if (pieChart) pieChart.dispose()
  if (barChart) barChart.dispose()
})

function onResize() {
  if (pieChart) pieChart.resize()
  if (barChart) barChart.resize()
}

async function fetchData() {
  loading.value = true
  try {
    const [ov, cs, es] = await Promise.all([
      getOverview(),
      getComplaintStatus(),
      getEmergencyStatus()
    ])
    Object.assign(data, ov.data || {})
    await nextTick()
    const echarts = await import('echarts')

    pieChart = echarts.init(pieRef.value)
    pieChart.setOption({
      title: { text: '投诉状态分布', left: 'center' },
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [
        {
          type: 'pie',
          radius: ['40%', '68%'],
          data: Object.entries(cs.data || {}).map(([k, v]) => ({
            name: statusLabel[k] || k,
            value: v
          }))
        }
      ]
    })

    barChart = echarts.init(barRef.value)
    barChart.setOption({
      title: { text: '应急信息状态', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: Object.keys(es.data || {}).map(k => eStatusLabel[k] || k) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{ type: 'bar', barWidth: '40%', data: Object.values(es.data || {}) }]
    })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.stat-card {
  margin-bottom: 4px;
  text-align: center;
}
.stat-label {
  color: #909399;
  font-size: 14px;
}
.stat-value {
  font-size: 30px;
  font-weight: 700;
  margin-top: 8px;
}
.chart {
  height: 320px;
}
</style>
