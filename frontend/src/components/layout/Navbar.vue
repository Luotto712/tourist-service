<template>
  <div class="navbar">
    <div class="navbar-left">
      <el-icon class="hamburger" @click="appStore.toggleSidebar()">
        <Fold v-if="!appStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>
      <span class="app-title">武侯祠游客服务中心</span>
    </div>

    <div class="navbar-center">
      <el-badge :value="notifyStore.unreadCount" :hidden="notifyStore.unreadCount === 0" :max="99" class="notification-bell">
        <el-icon :size="20" @click="goNotifications"><Bell /></el-icon>
      </el-badge>
    </div>

    <div class="navbar-right">
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-icon class="avatar-icon"><UserFilled /></el-icon>
          <span class="username">{{ userStore.realName || userStore.username }}</span>
          <el-tag
            size="small"
            :type="roleTagType"
            class="role-tag"
          >
            {{ roleLabel }}
          </el-tag>
          <el-icon class="arrow-icon"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item disabled>
              <div class="dropdown-user-detail">
                <div>{{ userStore.realName || userStore.username }}</div>
                <div class="dropdown-role">{{ roleLabel }}</div>
              </div>
            </el-dropdown-item>
            <el-dropdown-item command="profile">
              <el-icon><UserFilled /></el-icon>
              个人信息
            </el-dropdown-item>
            <el-dropdown-item divided command="changePassword">
              <el-icon><Lock /></el-icon>
              修改密码
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { useNotificationStore } from '@/stores/notification'
import {
  Fold,
  Expand,
  UserFilled,
  ArrowDown,
  Lock,
  SwitchButton,
  Bell
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { roleLabel as roleLabelMap, roleType } from '@/constants/roles'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()
const notifyStore = useNotificationStore()

const roleLabel = computed(() => roleLabelMap[userStore.role] || userStore.role || '用户')

const roleTagType = computed(() => roleType[userStore.role] || 'info')


function goNotifications() {
  router.push('/notifications')
}

let timer = null
onMounted(() => {
  notifyStore.refresh()
  timer = setInterval(() => notifyStore.refresh(), 30000)
})
onUnmounted(() => {
  if (timer) clearInterval(timer)
})

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'changePassword') {
    router.push('/change-password')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  z-index: 10;

  .navbar-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .hamburger {
      font-size: 20px;
      cursor: pointer;
      color: #333;
      transition: color 0.2s;

      &:hover {
        color: #409eff;
      }
    }

    .app-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      white-space: nowrap;
    }
  }

  .navbar-center {
    display: flex;
    align-items: center;
    margin-left: auto;
    margin-right: 16px;
    gap: 16px;

    .school-logo-link {
      display: flex;
      align-items: center;

      .school-logo {
        height: auto;
        width: 168px;
        cursor: pointer;
      }
    }

    .notification-bell {
      cursor: pointer;
      color: #666;
      font-size: 20px;
      padding: 6px;
      border-radius: 50%;
      transition: all 0.2s;
      &:hover { color: #409eff; background: #ecf5ff; }
    }
  }

  .navbar-right {
    display: flex;
    align-items: center;

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 4px;
      transition: background-color 0.2s;

      &:hover {
        background-color: #f5f7fa;
      }

      .avatar-icon {
        font-size: 20px;
        color: #409eff;
      }

      .username {
        font-size: 14px;
        color: #333;
      }

      .role-tag {
        margin-left: 4px;
      }

      .arrow-icon {
        font-size: 12px;
        color: #999;
        margin-left: 4px;
      }
    }
  }
}

.dropdown-user-detail {
  text-align: center;
  padding: 4px 0;

  .dropdown-role {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}
</style>
