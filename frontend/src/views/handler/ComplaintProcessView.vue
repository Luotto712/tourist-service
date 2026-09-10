<template>
  <el-card>
    <template #header><span>待处理投诉</span></template>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column label="编号" width="80">
        <template #default="{ $index }">{{ (pagination.page - 1) * pagination.size + $index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="userName" label="投诉人" width="120" />
      <el-table-column label="投诉" width="90">
        <template #default="{ row }"><el-button size="small" type="primary" link @click="$router.push(`/complaints/${row.id}`)">详情</el-button></template>
      </el-table-column>
      <el-table-column prop="assignTime" label="分派时间" width="180" />
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openProcess(row)">处理</el-button>
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

    <el-dialog v-model="dialogVisible" title="处理投诉" width="600px" :close-on-click-modal="false">
      <div v-if="current" style="margin-bottom: 12px">
        <strong>投诉内容：</strong>{{ current.content }}
      </div>
      <el-form label-width="90px">
        <el-form-item label="处理结果">
          <el-input v-model="result" type="textarea" :rows="4" placeholder="请填写处理结果" />
        </el-form-item>
        <el-form-item label="处理图片/视频">
          <el-upload
            multiple
            :auto-upload="false"
            accept="image/*,video/*"
            :on-change="onProcessFileChange"
            :on-remove="onProcessFileRemove"
            :file-list="processFileList"
            list-type="picture-card"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="acting" @click="handleSubmit">提交处理结果</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingComplaints, processComplaint } from '@/api/complaint'
import { uploadFile } from '@/api/file'
import { Plus } from '@element-plus/icons-vue'

const list = ref([])
const loading = ref(false)
const pagination = reactive({ page: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const acting = ref(false)
const current = ref(null)
const result = ref('')
const processFileList = ref([])
const processFiles = ref([])

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const res = await getPendingComplaints({ page: pagination.page, pageSize: pagination.size })
    const data = res.data || {}
    list.value = data.list || []
    pagination.total = data.total || 0
  } finally {
    loading.value = false
  }
}

function openProcess(row) {
  current.value = row
  result.value = ''
  processFileList.value = []
  processFiles.value = []
  dialogVisible.value = true
}

function onProcessFileChange(file, files) {
  if (file.raw) processFiles.value.push(file.raw)
  processFileList.value = files
}

function onProcessFileRemove(file, files) {
  processFiles.value = processFiles.value.filter(r => r !== file.raw)
  processFileList.value = files
}

async function handleSubmit() {
  if (!result.value.trim()) { ElMessage.warning('请填写处理结果'); return }
  acting.value = true
  try {
    const res = await processComplaint(current.value.id, { result: result.value })
    const reply = res.data || res
    for (const raw of processFiles.value) {
      const fd = new FormData()
      fd.append('file', raw)
      fd.append('relatedType', 'COMPLAINT_REPLY')
      fd.append('relatedId', reply.id)
      await uploadFile(fd)
    }
    ElMessage.success('处理完成')
    dialogVisible.value = false
    fetchList()
  } finally {
    acting.value = false
  }
}
</script>
