import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import Components from 'unplugin-vue-components/vite'          // ✏️ 新增
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'  // ✏️ 新增

export default defineConfig({
  plugins: [
    vue(),
    // ✏️ 新增：模板里的 <el-xxx> 自动按需引入
    Components({
      resolvers: [ElementPlusResolver()],
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
