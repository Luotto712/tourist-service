<template>
  <el-card>
    <template #header>
      <div class="header-bar">
        <span>我的投诉</span>
        <el-button type="primary" @click="openSubmit">提交投诉</el-button>
      </div>
    </template>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column label="编号" width="80">
        <template #default="{ $index }">{{
          (pagination.page - 1) * pagination.size + $index + 1
        }}</template>
      </el-table-column>
      <el-table-column prop="content" label="投诉内容" show-overflow-tooltip min-width="240" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="180" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="$router.push(`/complaints/${row.id}`)"
            >查看</el-button
          >
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top: 16px; justify-content: flex-end"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :page-sizes="[10, 20, 50]"
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      @current-change="fetchList"
      @size-change="fetchList"
    />

    <el-dialog v-model="submitVisible" title="提交投诉" width="560px" :close-on-click-modal="false">
      <el-form :model="submitForm" label-width="80px">
        <el-form-item label="投诉内容">
          <el-input
            v-model="submitForm.content"
            type="textarea"
            :rows="6"
            placeholder="请描述您遇到的问题"
          />
        </el-form-item>
        <el-form-item label="图片/视频">
          <el-upload
            ref="uploadRef"
            multiple
            :auto-upload="false"
            accept="image/*,video/*"
            :on-change="onFileChange"
            :on-remove="onFileRemove"
            :file-list="fileList"
            list-type="picture-card"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getMyComplaints, submitComplaint, deleteComplaint } from '@/api/complaint'
import { uploadFile } from '@/api/file'

const list = ref([])
const loading = ref(false)
const pagination = reactive({ page: 1, size: 10, total: 0 })

const submitVisible = ref(false)
const submitting = ref(false)
const submitForm = reactive({ content: '' })
const fileList = ref([])
const selectedFiles = ref([])

onMounted(fetchList)

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该投诉记录吗？', '提示', { type: 'warning' })
  await deleteComplaint(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getMyComplaints({ page: pagination.page, pageSize: pagination.size })
    const data = res.data || {}
    list.value = data.list || []
    pagination.total = data.total || 0
  } finally {
    loading.value = false
  }
}

function openSubmit() {
  submitForm.content = ''
  fileList.value = []
  selectedFiles.value = []
  submitVisible.value = true
}

function onFileChange(file, files) {
  if (file.raw) selectedFiles.value.push(file.raw)
  fileList.value = files
}

function onFileRemove(file, files) {
  selectedFiles.value = selectedFiles.value.filter(r => r !== file.raw)
  fileList.value = files
}

async function handleSubmit() {
  if (!submitForm.content.trim()) {
    ElMessage.warning('请填写投诉内容')
    return
  }
  submitting.value = true
  try {
    const res = await submitComplaint({ content: submitForm.content })
    const newId = (res.data || res).id
    // 先建投诉，再上传所选图片/视频（关联到新投诉）
    for (const raw of selectedFiles.value) {
      const fd = new FormData()
      fd.append('file', raw)
      fd.append('relatedType', 'COMPLAINT')
      fd.append('relatedId', newId)
      await uploadFile(fd)
    }
    ElMessage.success('提交成功')
    submitVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

const statusText = {
  PENDING: ['待审批', 'warning'],
  APPROVED: ['已通过', 'success'],
  REJECTED: ['未通过', 'danger'],
  PROCESSING: ['处理中', 'primary'],
  RESOLVED: ['处理完成', 'success'],
  CONFIRMED: ['已确认', 'success'],
  CLOSED: ['已结案', 'info']
}
function statusLabel(s) {
  return (statusText[s] || [s, 'info'])[0]
}
function statusType(s) {
  return (statusText[s] || [s, 'info'])[1]
}
</script>

<style lang="scss" scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
