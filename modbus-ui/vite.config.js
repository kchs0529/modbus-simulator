import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      // /api 요청을 Spring Boot로 프록시 → CORS 문제 없음
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  },
  build: {
    // 빌드 결과물을 Spring Boot static 폴더로 직접 출력
    outDir: '../modbus-simulator/src/main/resources/static',
    emptyOutDir: true,
  }
})
