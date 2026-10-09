<template>
  <div class="file-list">
    <div v-if="files.length === 0" class="empty-hint">
      <el-empty description="暂无文件" :image-size="80" />
    </div>
    <div v-else class="file-items">
      <div v-for="file in files" :key="file.id" class="file-item">
        <el-icon class="file-icon" :style="{ color: getFileColor(file.fileExt) }" :size="28">
          <component :is="getFileIcon(file.fileExt)" />
        </el-icon>
        <div class="file-detail">
          <span class="file-name" :title="file.fileName">{{ file.fileName }}</span>
          <span class="file-meta">
            {{ formatSize(file.fileSize) }}
            <span v-if="file.createTime" class="file-time">
              &middot; {{ formatTime(file.createTime) }}
            </span>
          </span>
        </div>
        <div class="file-actions">
          <el-button size="small" text type="primary" @click="handleDownload(file)">
            <el-icon><Download /></el-icon>
            下载
          </el-button>
          <el-button
            v-if="showDelete"
            size="small"
            text
            type="danger"
            @click="handleDelete(file)"
          >
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getToken } from '@/utils/auth'
import {
  Document,
  PictureFilled,
  VideoCameraFilled,
  Download,
  Delete
} from '@element-plus/icons-vue'

defineProps({
  files: {
    type: Array,
    default: () => []
  },
  showDelete: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['delete'])

function formatSize(bytes) {
  if (!bytes && bytes !== 0) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const Y = date.getFullYear()
  const M = String(date.getMonth() + 1).padStart(2, '0')
  const D = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  return `${Y}-${M}-${D} ${h}:${m}`
}

function getFileColor(ext) {
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
  return colorMap[ext?.toLowerCase()] || '#909399'
}

function getFileIcon(ext) {
  ext = ext?.toLowerCase() || ''
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext)) {
    return PictureFilled
  }
  if (['mp4', 'avi', 'mov', 'mkv'].includes(ext)) {
    return VideoCameraFilled
  }
  return Document
}

function handleDownload(file) {
  const url = `${window.location.origin}/api/files/${file.id}/download`
  const token = getToken()
  const link = document.createElement('a')
  link.href = url
  if (token) {
    link.href = url + (url.includes('?') ? '&' : '?') + 'token=' + encodeURIComponent(token)
  }
  window.open(url, '_blank')
}

function handleDelete(file) {
  emit('delete', file.id)
}
</script>

<style lang="scss" scoped>
.file-list {
  .empty-hint {
    padding: 20px 0;
  }

  .file-items {
    .file-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 14px;
      background: #f9fafc;
      border: 1px solid #ebeef5;
      border-radius: 8px;
      margin-bottom: 8px;
      transition: all 0.2s;

      &:hover {
        border-color: #c6d9f5;
        background: #ecf5ff;
      }

      .file-icon {
        flex-shrink: 0;
      }

      .file-detail {
        flex: 1;
        min-width: 0;
        display: flex;
        flex-direction: column;
        gap: 4px;

        .file-name {
          font-size: 14px;
          font-weight: 500;
          color: #303133;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .file-meta {
          font-size: 12px;
          color: #909399;

          .file-time {
            margin-left: 4px;
          }
        }
      }

      .file-actions {
        flex-shrink: 0;
        display: flex;
        gap: 4px;
      }
    }
  }
}
</style>
