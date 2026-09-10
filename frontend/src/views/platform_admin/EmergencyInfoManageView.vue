<template>
  <el-card>
    <template #header>
      <div class="header-bar">
        <span>应急信息管理</span>
        <el-button type="primary" @click="openDialog()">新增应急信息</el-button>
      </div>
    </template>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
      <el-table-column label="有效期" width="200">
        <template #default="{ row }">{{ row.validFrom }} ~ {{ row.validTo }}</template>
      </el-table-column>
      <el-table-column prop="publisherName" label="发布人" width="110" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑应急信息' : '新增应急信息'" width="560px" :close-on-click-modal="false">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="生效日期">
          <el-date-picker v-model="form.validFrom" type="date" value-format="YYYY-MM-DD" placeholder="开始" style="width: 45%" />
          <span style="margin: 0 8px">~</span>
          <el-date-picker v-model="form.validTo" type="date" value-format="YYYY-MM-DD" placeholder="结束" style="width: 45%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEmergencyAdmin, publishEmergency, updateEmergency, deleteEmergency } from '@/api/emergency'

const list = ref([])
const loading = ref(false)
const pagination = reactive({ page: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const submitting = ref(false)
const editing = ref(null)
const form = reactive({ title: '', content: '', validFrom: '', validTo: '' })

const statusLabelMap = { PENDING: '待审批', APPROVED: '已发布', REJECTED: '已驳回' }
const statusTypeMap = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
function statusLabel(s) { return statusLabelMap[s] || s }
function statusType(s) { return statusTypeMap[s] || 'info' }

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const res = await getEmergencyAdmin({ page: pagination.page, pageSize: pagination.size })
    const data = res.data || {}
    list.value = data.list || []
    pagination.total = data.total || 0
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  editing.value = row || null
  form.title = row?.title || ''
  form.content = row?.content || ''
  form.validFrom = row?.validFrom || ''
  form.validTo = row?.validTo || ''
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.title.trim()) { ElMessage.warning('请填写标题'); return }
  submitting.value = true
  try {
    const payload = { title: form.title, content: form.content, validFrom: form.validFrom, validTo: form.validTo }
    if (editing.value) await updateEmergency(editing.value.id, payload)
    else await publishEmergency(payload)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该应急信息吗？', '提示', { type: 'warning' })
  await deleteEmergency(row.id)
  ElMessage.success('删除成功')
  fetchList()
}
</script>

<style lang="scss" scoped>
.header-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
