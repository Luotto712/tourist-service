import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import Components from 'unplugin-vue-components/vite'          // ✏️ 新增
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'  // ✏️ 新增

export default defineConfig({
  // 动态 import 的依赖提前预构建，避免第一次访问时触发整页重载
  optimizeDeps: {
    include: ['echarts']
  },

  // 用 Sass 新 API（Vite 5.4+），消掉 legacy-js-api 弃用警告
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern-compiler'
      }
    }
  },
  plugins: [
    vue(),
    // ✏️ 新增：模板里的 <el-xxx> 自动按需引入
    Components({
      // importStyle: false —— 样式已在 main.js 全量引入，
      // 关掉逐组件注入，避免重复引入 + 开发时反复预构建重载
      resolvers: [ElementPlusResolver({ importStyle: false })],   // ✏️ 不注入样式引入
      dts: 'src/components.d.ts'      // ✏️ 生成到 src 里
    })
  ],

  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
