<template>
  <el-card>
    <template #header><span>投诉审批</span></template>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column label="编号" width="80">
        <template #default="{ $index }">{{ (pagination.page - 1) * pagination.size + $index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="userName" label="投诉人" width="120" />
      <el-table-column label="投诉" width="90">
        <template #default="{ row }"><el-button size="small" type="primary" link @click="$router.push(`/complaints/${row.id}`)">详情</el-button></template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="openDialog(row, 'approve')">通过</el-button>
          <el-button size="small" type="danger" @click="openDialog(row, 'reject')">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top: 16px; justify-content: flex-end"
      background
      layout="total, prev, pager, next"
      v-model:current-page="pagination.page"
      :page-size="pagination.size"
      :total="pagination.total"
      @current-change="fetchList"
    />

    <el-dialog v-model="dialogVisible" :title="dialogAction === 'approve' ? '通过投诉' : '驳回投诉'" width="480px">
      <el-input v-model="comment" type="textarea" :rows="3" placeholder="审批意见（可选）" />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          :type="dialogAction === 'approve' ? 'success' : 'danger'"
          :loading="acting"
          @click="handleConfirm"
        >{{ dialogAction === 'approve' ? '通过' : '驳回' }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getForApproval, approveComplaint, rejectComplaint } from '@/api/complaint'

const list = ref([])
const loading = ref(false)
const pagination = reactive({ page: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const dialogAction = ref('approve')
const current = ref(null)
const comment = ref('')
const acting = ref(false)

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const res = await getForApproval({ page: pagination.page, pageSize: pagination.size })
    const data = res.data || {}
    list.value = data.list || []
    pagination.total = data.total || 0
  } finally {
    loading.value = false
  }
}

function openDialog(row, action) {
  current.value = row
  dialogAction.value = action
  comment.value = ''
  dialogVisible.value = true
}

async function handleConfirm() {
  if (!current.value) return
  acting.value = true
  try {
    const payload = comment.value.trim() ? { comment: comment.value } : {}
    if (dialogAction.value === 'approve') await approveComplaint(current.value.id, payload)
    else await rejectComplaint(current.value.id, payload)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    acting.value = false
  }
}
</script>
