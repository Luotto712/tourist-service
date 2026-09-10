<template>
  <div class="file-uploader">
    <el-upload
      ref="uploadRef"
      class="upload-area"
      drag
      multiple
      :show-file-list="false"
      :http-request="handleUpload"
      :accept="accept"
      :limit="10"
      :before-upload="beforeUpload"
    >
      <el-icon class="upload-icon"><UploadFilled /></el-icon>
      <div class="upload-text">
        <em>点击上传</em> 或将文件拖拽到此处
      </div>
      <div class="upload-tip">
        支持 pdf、doc、docx、xls、xlsx、jpg、jpeg、png、mp4 格式，单个文件不超过 50MB
      </div>
    </el-upload>

    <div v-if="uploadingItems.length > 0" class="uploading-list">
      <div v-for="item in uploadingItems" :key="item.uid" class="uploading-item">
        <div class="file-info">
          <el-icon class="file-icon"><Document /></el-icon>
          <span class="file-name">{{ item.name }}</span>
          <span class="file-size">{{ formatSize(item.size) }}</span>
        </div>
        <el-progress
          :percentage="item.progress"
          :status="item.status"
          :stroke-width="8"
          class="upload-progress"
        />
      </div>
    </div>

    <div v-if="fileList.length > 0" class="uploaded-list">
      <div class="list-header">
        <span class="list-title">已上传文件（{{ fileList.length }}）</span>
      </div>
      <div v-for="file in fileList" :key="file.id" class="file-row">
        <div class="file-row-info">
          <el-icon class="file-type-icon" :style="{ color: getFileColor(file.fileExt || file.fileName) }">
            <Document />
          </el-icon>
          <span class="file-row-name">{{ file.fileName }}</span>
          <span class="file-row-size">{{ formatSize(file.fileSize) }}</span>
        </div>
        <el-button
          type="danger"
          size="small"
          text
          :icon="Delete"
          @click="handleDeleteFile(file)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Document, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'

const props = defineProps({
  relatedType: {
    type: String,
    default: ''
  },
  relatedId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['upload-success', 'upload-error'])

const accept = '.pdf,.doc,.docx,.xls,.xlsx,.jpg,.jpeg,.png,.mp4'
const maxSize = 50 * 1024 * 1024
const uploadRef = ref(null)
const fileList = ref([])
const uploadingItems = reactive([])

function formatSize(bytes) {
  if (!bytes && bytes !== 0) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

function getFileColor(fileName) {
  const ext = (fileName || '').split('.').pop()?.toLowerCase()
  const colorMap = {
    pdf: '#e74c3c',
    doc: '#2980b9',
    docx: '#2980b9',
    xls: '#27ae60',
    xlsx: '#27ae60',
    jpg: '#8e44ad',
    jpeg: '#8e44ad',
    png: '#8e44ad',
    mp4: '#e67e22'
  }
  return colorMap[ext] || '#909399'
}

function beforeUpload(file) {
  const fileExt = '.' + (file.name || '').split('.').pop()?.toLowerCase()
  const allowedExts = accept.split(',')
  if (!allowedExts.some(ext => ext.trim().toLowerCase() === fileExt)) {
    ElMessage.warning(`文件 "${file.name}" 格式不支持，仅支持 ${accept}`)
    return false
  }
  if (file.size > maxSize) {
    ElMessage.warning(`文件 "${file.name}" 超过 50MB 限制`)
    return false
  }
  return true
}

async function handleUpload(options) {
  const { file } = options
  const formData = new FormData()
  formData.append('file', file)
  if (props.relatedType) {
    formData.append('relatedType', props.relatedType)
  }
  if (props.relatedId !== null) {
    formData.append('relatedId', props.relatedId)
  }

  const item = reactive({
    uid: file.uid,
    name: file.name,
    size: file.size,
    progress: 0,
    status: ''
  })
  uploadingItems.push(item)

  try {
    const res = await request.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (event) => {
        if (event.total) {
          item.progress = Math.round((event.loaded * 100) / event.total)
        }
      }
    })
    item.status = 'success'
    item.progress = 100
    const data = res.data || res
    fileList.value.push(data)
    ElMessage.success(`文件 "${file.name}" 上传成功`)
    emit('upload-success', data)
  } catch (error) {
    item.status = 'exception'
    ElMessage.error(`文件 "${file.name}" 上传失败`)
    emit('upload-error', error)
  } finally {
    setTimeout(() => {
      const idx = uploadingItems.findIndex(i => i.uid === item.uid)
      if (idx > -1) uploadingItems.splice(idx, 1)
    }, 2000)
  }
}

function handleDeleteFile(file) {
  const idx = fileList.value.findIndex(f => f.id === file.id)
  if (idx > -1) {
    fileList.value.splice(idx, 1)
    ElMessage.success('文件已移除')
  }
}

defineExpose({
  fileList,
  clearFiles() {
    fileList.value = []
    uploadingItems.length = 0
  }
})
</script>

<style lang="scss" scoped>
.file-uploader {
  .upload-area {
    width: 100%;

    :deep(.el-upload-dragger) {
      padding: 30px 20px;
      border: 2px dashed #d9d9d9;
      border-radius: 8px;
      transition: border-color 0.3s;

      &:hover {
        border-color: #409eff;
      }
    }

    .upload-icon {
      font-size: 48px;
      color: #c0c4cc;
      margin-bottom: 12px;
    }

    .upload-text {
      font-size: 14px;
      color: #606266;

      em {
        color: #409eff;
        font-style: normal;
        cursor: pointer;

        &:hover {
          text-decoration: underline;
        }
      }
    }

    .upload-tip {
      font-size: 12px;
      color: #c0c4cc;
      margin-top: 8px;
    }
  }

  .uploading-list {
    margin-top: 16px;

    .uploading-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 12px;
      background: #f5f7fa;
      border-radius: 6px;
      margin-bottom: 8px;

      .file-info {
        display: flex;
        align-items: center;
        gap: 8px;
        min-width: 0;
        flex: 1;

        .file-icon {
          font-size: 18px;
          color: #409eff;
          flex-shrink: 0;
        }

        .file-name {
          font-size: 13px;
          color: #303133;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .file-size {
          font-size: 12px;
          color: #909399;
          flex-shrink: 0;
        }
      }

      .upload-progress {
        width: 160px;
        flex-shrink: 0;
      }
    }
  }

  .uploaded-list {
    margin-top: 16px;

    .list-header {
      margin-bottom: 8px;

      .list-title {
        font-size: 14px;
        font-weight: 500;
        color: #303133;
      }
    }

    .file-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 8px 12px;
      background: #f5f7fa;
      border-radius: 6px;
      margin-bottom: 6px;
      transition: background-color 0.2s;

      &:hover {
        background: #e8edf3;
      }

      .file-row-info {
        display: flex;
        align-items: center;
        gap: 8px;
        min-width: 0;
        flex: 1;

        .file-type-icon {
          font-size: 20px;
          flex-shrink: 0;
        }

        .file-row-name {
          font-size: 13px;
          color: #303133;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .file-row-size {
          font-size: 12px;
          color: #909399;
          flex-shrink: 0;
        }
      }
    }
  }
}
</style>
