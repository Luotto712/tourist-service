<template>
  <el-card>
    <template #header><span>应急信息审批</span></template>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
      <el-table-column label="有效期" width="200">
        <template #default="{ row }">{{ row.validFrom }} ~ {{ row.validTo }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="handleApprove(row, true)">通过</el-button>
          <el-button size="small" type="danger" @click="handleApprove(row, false)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && !list.length" description="暂无待审批的应急信息" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEmergencyPending, approveEmergency, rejectEmergency } from '@/api/emergency'

const list = ref([])
const loading = ref(false)

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const res = await getEmergencyPending()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handleApprove(row, approved) {
  if (approved) {
    await approveEmergency(row.id)
  } else {
    await ElMessageBox.confirm('确定驳回该应急信息吗？', '驳回', { type: 'warning' })
    await rejectEmergency(row.id)
  }
  ElMessage.success('操作成功')
  fetchList()
}
</script>
