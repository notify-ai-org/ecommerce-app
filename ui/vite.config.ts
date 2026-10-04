import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

// The production build is written straight into the Spring Boot classpath and
// served by PortalController at <prefix>/portals/shop/. An absolute base keeps
// asset URLs valid on deep links such as /portals/shop/products/P-1001.
export default defineConfig(({ mode }) => {
  // Path prefix the app is deployed under, e.g. VITE_BASE_PATH=/ecommerce when
  // a reverse proxy forwards https://host/ecommerce/* to this app (pair it with
  // Spring's server.servlet.context-path). Empty for local development.
  const prefix = (loadEnv(mode, '.', 'VITE_').VITE_BASE_PATH ?? '').replace(/\/+$/, '');
  return {
    plugins: [react()],
    base: `${prefix}/portals/shop/`,
    build: {
      outDir: '../src/main/resources/static/portals/shop',
      emptyOutDir: true,
    },
    server: {
      port: 5174,
      proxy: { [`${prefix}/api`]: { target: 'http://localhost:8090', changeOrigin: true } },
    },
  };
});
