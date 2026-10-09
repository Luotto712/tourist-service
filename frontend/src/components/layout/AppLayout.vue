<template>
  <el-container class="app-layout">
    <el-aside :width="asideWidth" class="app-aside">
      <div class="logo-container">
        <img src="/ODF.png" alt="logo" class="logo-img" />
        <span v-show="!appStore.sidebarCollapsed" class="logo-title">武侯祠游客服务中心</span>
      </div>
      <SidebarMenu />
    </el-aside>
    <el-container class="main-container">
      <el-header height="60px" class="app-header">
        <Navbar />
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'
import SidebarMenu from './SidebarMenu.vue'
import Navbar from './Navbar.vue'

const appStore = useAppStore()

const asideWidth = computed(() => {
  return appStore.sidebarCollapsed ? '64px' : '220px'
})
</script>

<style lang="scss" scoped>
.app-layout {
  height: 100vh;
  overflow: hidden;

  .app-aside {
    background-color: #560c0b;
    transition: width 0.3s ease;
    overflow: hidden;
    flex-shrink: 0;

    .logo-container {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      background-color: #560c0b;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      overflow: hidden;

      .logo-img {
        width: 32px;
        height: 32px;
        flex-shrink: 0;
      }

      .logo-title {
        color: #fff;
        font-size: 16px;
        font-weight: 600;
        white-space: nowrap;
        overflow: hidden;
      }
    }
  }

  .main-container {
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .app-header {
      padding: 0;
      flex-shrink: 0;
    }

    .app-main {
      flex: 1;
      overflow-y: auto;
      background-color: #f5f7fa;
      padding: 20px;
      position: relative;

      &::before {
        content: '';
        position: fixed;
        top: 60px;
        left: 220px;
        right: 0;
        bottom: 0;
        background: url('/bg_img.jpg') center/cover no-repeat;
        opacity: 0.15;
        pointer-events: none;
        z-index: 0;
        mask-image: linear-gradient(
          to bottom,
          transparent 0%,
          rgba(0, 0, 0, 0.3) 8%,
          rgba(0, 0, 0, 1) 20%,
          rgba(0, 0, 0, 1) 100%
        );
        -webkit-mask-image: linear-gradient(
          to bottom,
          transparent 0%,
          rgba(0, 0, 0, 0.3) 8%,
          rgba(0, 0, 0, 1) 20%,
          rgba(0, 0, 0, 1) 100%
        );
      }

      :deep(> *) {
        position: relative;
        z-index: 1;
      }
    }
  }
}
</style>
