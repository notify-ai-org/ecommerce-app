import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// The production build is written straight into the Spring Boot classpath and
// served by PortalController at /portals/shop/. An absolute base keeps asset
// URLs valid on deep links such as /portals/shop/products/P-1001.
export default defineConfig({
  plugins: [react()],
  base: '/portals/shop/',
  build: {
    outDir: '../src/main/resources/static/portals/shop',
    emptyOutDir: true,
  },
  server: {
    port: 5174,
    proxy: { '/api': { target: 'http://localhost:8090', changeOrigin: true } },
  },
});
