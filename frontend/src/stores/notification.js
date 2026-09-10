import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount } from '@/api/notification'

// 消息未读数：Navbar 铃铛与通知页共用，实时同步（标记已读后立即反映）。
export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)

  async function refresh() {
    try {
      const res = await getUnreadCount()
      unreadCount.value = (res.data ?? res) || 0
    } catch {
      unreadCount.value = 0
    }
  }

  function clear() {
    unreadCount.value = 0
  }

  return { unreadCount, refresh, clear }
})
