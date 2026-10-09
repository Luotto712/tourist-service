<template>
  <el-card>
    <template #header><span>景区应急信息</span></template>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" min-width="280" show-overflow-tooltip />
      <el-table-column label="有效期" width="220">
        <template #default="{ row }">{{ row.validFrom }} ~ {{ row.validTo }}</template>
      </el-table-column>
      <el-table-column label="查看" width="90">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="showDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="应急信息详情" width="520px">
      <template v-if="current">
        <h3>{{ current.title }}</h3>
        <p style="color: #909399">有效期：{{ current.validFrom }} ~ {{ current.validTo }}</p>
        <p>{{ current.content }}</p>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getEmergencyPublic } from '@/api/emergency'

const list = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const current = ref(null)

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const res = await getEmergencyPublic()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function showDetail(row) {
  current.value = row
  detailVisible.value = true
}
</script>
