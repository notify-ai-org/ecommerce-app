import type { ReactNode } from 'react';
import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import Layout from './components/Layout';
import CartPage from './pages/CartPage';
import LoginPage from './pages/LoginPage';
import OrdersPage from './pages/OrdersPage';
import ProductDetailPage from './pages/ProductDetailPage';
import ProductsPage from './pages/ProductsPage';
import { useSession } from './session';

function RequireAuth({ children }: { children: ReactNode }) {
  const { customer } = useSession();
  const location = useLocation();
  if (customer === undefined) return <div className="page-loading">Loading…</div>;
  if (!customer) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  return <Layout>{children}</Layout>;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/products" element={<RequireAuth><ProductsPage /></RequireAuth>} />
      <Route path="/products/:productId" element={<RequireAuth><ProductDetailPage /></RequireAuth>} />
      <Route path="/cart" element={<RequireAuth><CartPage /></RequireAuth>} />
      <Route path="/orders" element={<RequireAuth><OrdersPage /></RequireAuth>} />
      <Route path="*" element={<Navigate to="/products" replace />} />
    </Routes>
  );
}
