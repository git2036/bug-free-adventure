import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';
import sass from 'sass'; // 静态导入 sass

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        implementation: sass, // 使用导入的 sass 对象
      },
    },
  },
  optimizeDeps: {
    include: ['tabulator-tables'],
  },
});