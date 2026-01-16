import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      // API 代理配置
      // 前端请求：/api/xxx → 后端：http://localhost:8080/api/xxx
      // 注意：后端 spring.servlet.context-path=/api，所以不需要 rewrite
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
