<template>
  <el-card>
    <template #header><span>投诉结案</span></template>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column label="编号" width="80">
        <template #default="{ $index }">{{ $index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="userName" label="投诉人" width="120" />
      <el-table-column label="投诉" width="90">
        <template #default="{ row }"
          ><el-button
            size="small"
            type="primary"
            link
            @click="$router.push(`/complaints/${row.id}`)"
            >详情</el-button
          ></template
        >
      </el-table-column>
      <el-table-column prop="result" label="处理结果" show-overflow-tooltip min-width="200" />
      <el-table-column prop="handlerName" label="处理人员" width="120" />
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            type="primary"
            :loading="acting === row.id"
            @click="handleClose(row)"
            >结案</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && !list.length" description="暂无待结案的投诉" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfirmedComplaints, closeComplaint } from '@/api/complaint'

const list = ref([])
const loading = ref(false)
const acting = ref(null)

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const res = await getConfirmedComplaints()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handleClose(row) {
  acting.value = row.id
  try {
    await closeComplaint(row.id)
    ElMessage.success('已结案')
    fetchList()
  } finally {
    acting.value = null
  }
}
</script>
