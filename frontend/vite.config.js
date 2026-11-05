import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // forward api calls to the backend, so no CORS is needed in dev
      '/api': 'http://localhost:8080',
    },
  },
})
