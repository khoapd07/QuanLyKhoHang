import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  // --- THÊM ĐOẠN NÀY ĐỂ NỐI VỚI BACKEND ---
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // Trỏ sang Spring Boot
        changeOrigin: true,
        secure: false,
        // rewrite: (path) => path.replace(/^\/api/, '') // Chỉ bật dòng này nếu Backend KHÔNG có /api
      }
    }
  }
})