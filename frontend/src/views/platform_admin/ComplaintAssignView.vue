<template>
  <el-card>
    <template #header><span>投诉分派</span></template>
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
      <el-table-column prop="createTime" label="提交时间" width="180" />
      <el-table-column label="分派处理人员" width="220" fixed="right">
        <template #default="{ row }">
          <el-select
            v-model="eachHandler[row.id]"
            placeholder="选择处理人员"
            size="small"
            style="width: 150px"
          >
            <el-option
              v-for="u in handlers"
              :key="u.id"
              :label="u.realName || u.username"
              :value="u.id"
            />
          </el-select>
          <el-button
            size="small"
            type="primary"
            :loading="acting === row.id"
            @click="handleAssign(row)"
            >分派</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && !list.length" description="暂无待分派的投诉" />
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getApprovedComplaints, assignComplaint } from '@/api/complaint'
import { searchUsers } from '@/api/user'
import { ROLE } from '@/constants/roles'

const list = ref([])
const handlers = ref([])
const loading = ref(false)
const acting = ref(null)
const eachHandler = reactive({})

onMounted(async () => {
  fetchList()
  const res = await searchUsers({ role: ROLE.COMPLAINT_HANDLER, page: 1, pageSize: 100 })
  handlers.value = (res.data && res.data.list) || []
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getApprovedComplaints()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handleAssign(row) {
  const handlerId = eachHandler[row.id]
  if (!handlerId) {
    ElMessage.warning('请选择处理人员')
    return
  }
  acting.value = row.id
  try {
    await assignComplaint(row.id, { handlerId })
    ElMessage.success('分派成功')
    fetchList()
  } finally {
    acting.value = null
  }
}
</script>
