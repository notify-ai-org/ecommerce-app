import type { ReactNode } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { firstName } from '../api';
import { useSession } from '../session';
import { useToast } from '../toast';
import NotificationBell from './NotificationBell';

export default function Layout({ children }: { children: ReactNode }) {
  const { customer, cart, logout } = useSession();
  const toast = useToast();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    toast.success('Signed out');
    navigate('/login', { replace: true });
  }

  return (
    <div className="shell">
      <header className="topbar">
        <div className="topbar-inner">
          <NavLink to="/products" className="brand">
            <span className="brand-mark" aria-hidden>
              <svg viewBox="0 0 24 24" width="18" height="18">
                <path d="M5 8h14l-1.4 11H6.4z" fill="none" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
                <path d="M9 8a3 3 0 0 1 6 0" fill="none" stroke="currentColor" strokeWidth="2" />
              </svg>
            </span>
            Notify Shop
          </NavLink>
          <nav className="nav">
            <NavLink to="/products">Products</NavLink>
            <NavLink to="/cart">
              Cart
              {cart && cart.itemCount > 0 && <span className="badge">{cart.itemCount}</span>}
            </NavLink>
            <NavLink to="/orders">Orders</NavLink>
          </nav>
          {customer && (
            <div className="user">
              <NotificationBell />
              <div className="user-meta">
                <span className="user-name">Hi, {firstName(customer.name)}</span>
                <span className="user-email">{customer.email}</span>
              </div>
              <button className="btn btn-ghost" onClick={handleLogout}>Log out</button>
            </div>
          )}
        </div>
      </header>
      <main className="content">{children}</main>
    </div>
  );
}
