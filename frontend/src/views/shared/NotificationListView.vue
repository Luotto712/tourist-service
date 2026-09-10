<template>
  <div class="notifications-page">
    <el-card>
      <template #header>
        <div class="page-header">
          <h2>消息通知</h2>
          <el-button type="primary" link @click="handleMarkAll">全部已读</el-button>
        </div>
      </template>
      <div v-loading="loading">
        <el-empty v-if="notifications.length === 0" description="暂无通知" />
        <div v-else v-for="item in notifications" :key="item.id" class="notification-item" :class="{ unread: !item.isRead }" @click="handleRead(item)">
          <div class="notif-header">
            <el-tag size="small" :type="typeTag(item.type)">{{ typeLabel(item.type) }}</el-tag>
            <span class="notif-time">{{ formatTime(item.createTime) }}</span>
            <el-button type="danger" size="small" link @click.stop="handleDelete(item, index)">删除</el-button>
          </div>
          <h4 class="notif-title">{{ item.title }}</h4>
          <p class="notif-content">{{ item.content }}</p>
        </div>
      </div>
      <div v-if="total > 10" class="pagination-wrapper">
        <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev, pager, next" @current-change="fetchData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useNotificationStore } from '@/stores/notification'
import { getNotifications, markAsRead, markAllAsRead, deleteNotification } from '@/api/notification'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const notifications = ref([])
const total = ref(0)
const page = ref(1)
const notifyStore = useNotificationStore()

function typeLabel(type) {
  const map = {
    COMPLAINT_SUBMITTED: '投诉提交', COMPLAINT_APPROVED: '投诉通过', COMPLAINT_REJECTED: '投诉驳回',
    COMPLAINT_ASSIGNED: '投诉分派', COMPLAINT_RESOLVED: '投诉处理', COMPLAINT_CLOSED: '投诉结案',
    EMERGENCY_SUBMITTED: '应急待审批', EMERGENCY_PUBLISHED: '应急通知', EMERGENCY_REJECTED: '应急被驳回',
    HOTEL_MARKETING: '酒店营销',
    SYSTEM: '系统通知'
  }
  return map[type] || '系统通知'
}

function typeTag(type) {
  const map = {
    COMPLAINT_SUBMITTED: 'warning', COMPLAINT_APPROVED: 'success', COMPLAINT_REJECTED: 'danger',
    COMPLAINT_ASSIGNED: 'warning', COMPLAINT_RESOLVED: 'success', COMPLAINT_CLOSED: 'info',
    EMERGENCY_SUBMITTED: 'warning', EMERGENCY_PUBLISHED: 'danger', EMERGENCY_REJECTED: 'danger',
    HOTEL_MARKETING: 'success',
    SYSTEM: 'info'
  }
  return map[type] || 'info'
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getNotifications({ page: page.value, pageSize: 10 })
    const data = res.data || res
    notifications.value = data.list || data.records || []
    total.value = data.total || 0
  } catch { notifications.value = [] } finally { loading.value = false }
}

async function handleRead(item) {
  if (!item.isRead) {
    try { await markAsRead(item.id); item.isRead = 1; notifyStore.refresh() } catch {}
  }
}

async function handleMarkAll() {
  try { await markAllAsRead(); notifications.value.forEach(n => n.isRead = 1); notifyStore.clear(); ElMessage.success('全部已读') } catch {}
}

async function handleDelete(item, index) {
  try { await ElMessageBox.confirm('确定删除这条消息？', '提示', { type: 'warning' }) } catch { return }
  try { await deleteNotification(item.id); notifications.value.splice(index, 1); notifyStore.refresh(); ElMessage.success('已删除') } catch {}
}

onMounted(fetchData)
</script>

<style lang="scss" scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; h2 { font-size: 18px; margin: 0; } }
.notification-item { padding: 16px; border-bottom: 1px solid #ebeef5; cursor: pointer; transition: background .2s; &.unread { background: #ecf5ff8f; } &:hover { background: #f5f7fa; } }
.notif-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; .notif-time { font-size: 12px; color: #999; margin-left: auto; } }
.notif-title { margin: 0 0 6px; font-size: 15px; color: #303133; }
.notif-content { margin: 0; font-size: 13px; color: #666; line-height: 1.5; }
.pagination-wrapper { display: flex; justify-content: center; padding-top: 20px; }
</style>
