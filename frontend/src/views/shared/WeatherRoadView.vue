<template>
  <el-row :gutter="20">
    <el-col :span="12">
      <el-card>
        <template #header><span>景区天气</span></template>
        <div v-loading="loading">
          <el-descriptions :column="1" border v-if="weather">
            <el-descriptions-item label="区域">{{ weather.area }}</el-descriptions-item>
            <el-descriptions-item label="日期">{{ weather.date }}</el-descriptions-item>
            <el-descriptions-item label="天气">{{ weather.weather }}</el-descriptions-item>
            <el-descriptions-item label="温度">{{ weather.temp }}°C</el-descriptions-item>
            <el-descriptions-item label="风力">{{ weather.wind }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="暂无天气数据" />
        </div>
      </el-card>
    </el-col>
    <el-col :span="12">
      <el-card>
        <template #header><span>景区路况</span></template>
        <div v-loading="loading">
          <el-table :data="roads" border>
            <el-table-column prop="road" label="道路" />
            <el-table-column prop="condition" label="路况" />
            <el-table-column prop="note" label="备注" show-overflow-tooltip />
          </el-table>
          <el-empty v-if="!roads.length" description="暂无路况数据" />
        </div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getWeather, getRoad } from '@/api/weather'

const loading = ref(false)
const weather = ref(null)
const roads = ref([])

onMounted(fetchAll)

async function fetchAll() {
  loading.value = true
  try {
    const [w, r] = await Promise.all([getWeather(), getRoad()])
    weather.value = w.data || null
    roads.value = r.data || []
  } finally {
    loading.value = false
  }
}
</script>
