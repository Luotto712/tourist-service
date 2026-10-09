<template>
  <el-menu
    :default-active="activeMenu"
    :collapse="appStore.sidebarCollapsed"
    :router="true"
    background-color="#560c0b"
    text-color="#f3e2e2"
    active-text-color="white"
    class="sidebar-menu"
  >
    <el-menu-item index="/">
      <el-icon><HomeFilled /></el-icon>
      <template #title>首页</template>
    </el-menu-item>

    <template v-for="item in roleMenus" :key="item.title">
      <el-sub-menu v-if="'children' in item" :index="'sub-' + item.title">
        <template #title>
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </template>
        <el-menu-item v-for="child in item.children" :key="child.index" :index="child.index">
          <el-icon><component :is="child.icon" /></el-icon>
          <template #title>{{ child.title }}</template>
        </el-menu-item>
      </el-sub-menu>
      <el-menu-item v-else :index="item.index">
        <el-icon><component :is="item.icon" /></el-icon>
        <template #title>{{ item.title }}</template>
      </el-menu-item>
    </template>

    <el-menu-item index="/change-password">
      <el-icon><Lock /></el-icon>
      <template #title>修改密码</template>
    </el-menu-item>
  </el-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { ROLE, isRole } from '@/constants/roles'
import type { Role } from '@/constants/roles'

const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

/** 叶子菜单项：能点击跳转的 */
interface MenuLeaf {
  index: string // 必填（el-menu-item 要求）
  title: string
  icon: string
}

/** 分组菜单项：只展开，不跳转 */
interface MenuGroup {
  title: string
  icon: string
  children: MenuLeaf[] // ⚠️ 注意这里是 MenuLeaf，不是 MenuItem
}

type MenuItem = MenuLeaf | MenuGroup

/** 角色 → 菜单列表 */
type MenuMap = Record<Role, MenuItem[]>

// 每个角色一组菜单（icon 为全局注册的 Element Plus 图标；children 渲染为子菜单）
const menuByRole: MenuMap = {
  [ROLE.TOURIST]: [
    { index: '/complaints', title: '我的投诉', icon: 'Document' },
    { index: '/emergency-info/list', title: '应急信息', icon: 'Warning' },
    {
      title: '景区服务',
      icon: 'List',
      children: [
        { index: '/catalog/attractions', title: '景点', icon: 'Location' },
        { index: '/catalog/routes', title: '旅游线路', icon: 'Guide' },
        { index: '/catalog/catering', title: '餐饮娱乐', icon: 'Food' },
        { index: '/catalog/performance-groups', title: '演出团体', icon: 'Microphone' },
        { index: '/catalog/transport', title: '景区交通', icon: 'Van' }
      ]
    },
    {
      title: '酒店',
      icon: 'OfficeBuilding',
      children: [
        { index: '/catalog/hotels/star', title: '星级酒店', icon: 'OfficeBuilding' },
        { index: '/catalog/hotels/nonstar', title: '非星级/乡村', icon: 'OfficeBuilding' },
        { index: '/my-bookings', title: '订单详情', icon: 'Tickets' }
      ]
    },
    { index: '/weather-conditions', title: '天气路况', icon: 'Sunny' }
  ],
  [ROLE.PLATFORM_ADMIN]: [
    { index: '/complaints/assign', title: '投诉分派', icon: 'Checked' },
    { index: '/complaints/close', title: '投诉结案', icon: 'CircleCheck' },
    { index: '/emergency-info', title: '应急信息管理', icon: 'Warning' },
    {
      title: '内容管理',
      icon: 'Setting',
      children: [
        { index: '/manage/attractions', title: '景点', icon: 'Location' },
        { index: '/manage/routes', title: '旅游线路', icon: 'Guide' },
        { index: '/manage/catering', title: '餐饮娱乐', icon: 'Food' },
        { index: '/manage/performance-groups', title: '演出团体', icon: 'Microphone' },
        { index: '/manage/transport', title: '景区交通', icon: 'Van' },
        { index: '/manage/hotels/star', title: '星级酒店', icon: 'OfficeBuilding' },
        { index: '/manage/hotels/nonstar', title: '非星级/乡村', icon: 'OfficeBuilding' }
      ]
    },
    { index: '/hotel-marketing', title: '酒店营销', icon: 'Promotion' },
    { index: '/dashboard', title: '数据看板', icon: 'DataBoard' },
    { index: '/users', title: '用户管理', icon: 'User' }
  ],
  [ROLE.APPROVER]: [
    { index: '/complaints/approval', title: '投诉审批', icon: 'Checked' },
    { index: '/emergency-info/approval', title: '应急信息审批', icon: 'Warning' }
  ],
  [ROLE.COMPLAINT_HANDLER]: [
    { index: '/complaints/handler', title: '待处理投诉', icon: 'Document' }
  ],
  [ROLE.HOTEL_ADMIN]: [{ index: '/hotel/rooms', title: '客房信息录入', icon: 'OfficeBuilding' }]
}

const roleMenus = computed(() => {
  const r = userStore.role
  return isRole(r) ? menuByRole[r] : []
  //     ↑ 这里收窄了，所以 menuByRole[r] 合法   ↑ 不是合法角色 → 空菜单
})

const activeMenu = computed(() => route.meta.activeMenu || route.path)
</script>

<style lang="scss" scoped>
.sidebar-menu {
  height: 100%;
  border-right: none;

  :deep(.el-menu-item:hover),
  :deep(.el-sub-menu__title:hover) {
    background-color: #bb0505;
  }

  &:not(.el-menu--collapse) {
    width: 220px;
  }
}
</style>
