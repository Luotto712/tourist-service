<template>
  <el-card class="home-card">
    <div class="hero">
      <h2>武侯祠游客服务中心</h2>
      <p class="welcome">{{ userName }}，欢迎回来（{{ roleLabel }}）</p>
      <p class="tip">请通过左侧菜单使用对应功能；操作进度留意右上角通知提醒。</p>
    </div>

    <el-divider />

    <div class="quick-links">
      <el-button v-for="link in quickLinks" :key="link.path" :type="link.type || 'primary'" @click="$router.push(link.path)">
        {{ link.label }}
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { roleLabel as roleLabelMap } from '@/constants/roles'

const userStore = useUserStore()
const userName = computed(() => userStore.realName || userStore.username || '游客')
const roleLabel = computed(() => roleLabelMap[userStore.role] || userStore.role || '用户')

const quickLinks = computed(() => {
  const r = userStore.role
  if (r === 'TOURIST') return [
    { path: '/complaints', label: '我的投诉', type: 'primary' },
    { path: '/catalog/attractions', label: '景点', type: 'success' },
    { path: '/catalog/hotels/star', label: '星级酒店', type: 'warning' },
    { path: '/weather-conditions', label: '天气路况', type: 'info' },
    { path: '/emergency-info/list', label: '应急信息', type: 'danger' }
  ]
  if (r === 'PLATFORM_ADMIN') return [
    { path: '/complaints/assign', label: '投诉分派' },
    { path: '/complaints/close', label: '投诉结案' },
    { path: '/emergency-info', label: '应急信息管理' },
    { path: '/dashboard', label: '数据看板', type: 'success' },
    { path: '/manage/attractions', label: '内容管理', type: 'warning' }
  ]
  if (r === 'APPROVER') return [
    { path: '/complaints/approval', label: '投诉审批' },
    { path: '/emergency-info/approval', label: '应急信息审批', type: 'warning' }
  ]
  if (r === 'COMPLAINT_HANDLER') return [
    { path: '/complaints/handler', label: '待处理投诉' }
  ]
  if (r === 'HOTEL_ADMIN') return [
    { path: '/hotel/rooms', label: '客房信息录入' }
  ]
  return []
})
</script>

<style lang="scss" scoped>
.home-card { max-width: 760px; margin: 20px auto; }
.hero { text-align: center; h2 { margin: 0 0 8px; color: #560c0b; } }
.welcome { font-size: 16px; font-weight: 600; margin: 0 0 6px; color: #303133; }
.tip { color: #909399; margin: 0; }
.quick-links { display: flex; flex-wrap: wrap; gap: 12px; justify-content: center; }
</style>
