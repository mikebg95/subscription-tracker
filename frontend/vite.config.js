import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // forward api calls to the backend, so CORS is not needed in dev
      '/api': { target: 'http://localhost:8080', changeOrigin: false },
    },
  },
})
