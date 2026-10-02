import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react';
import { api, setUnauthorizedHandler, type Cart, type Customer, type LoginForm } from './api';

interface Session {
  /** undefined while the initial /me check is in flight. */
  customer: Customer | null | undefined;
  cart: Cart | null;
  setCart: (cart: Cart) => void;
  refreshCart: () => Promise<void>;
  login: (form: LoginForm) => Promise<{ customer: Customer; firstLogin: boolean }>;
  logout: () => Promise<void>;
}

const SessionContext = createContext<Session | null>(null);

export function SessionProvider({ children }: { children: ReactNode }) {
  const [customer, setCustomer] = useState<Customer | null | undefined>(undefined);
  const [cart, setCart] = useState<Cart | null>(null);

  useEffect(() => {
    setUnauthorizedHandler(() => {
      setCustomer(null);
      setCart(null);
    });
    api.me().then(setCustomer).catch(() => setCustomer(null));
  }, []);

  const refreshCart = useCallback(async () => {
    setCart(await api.cart());
  }, []);

  useEffect(() => {
    if (customer) refreshCart().catch(() => {});
  }, [customer, refreshCart]);

  const login = useCallback(async (form: LoginForm) => {
    const result = await api.login(form);
    setCustomer(result.customer);
    return result;
  }, []);

  const logout = useCallback(async () => {
    await api.logout().catch(() => {});
    setCustomer(null);
    setCart(null);
  }, []);

  return (
    <SessionContext.Provider value={{ customer, cart, setCart, refreshCart, login, logout }}>
      {children}
    </SessionContext.Provider>
  );
}

export function useSession() {
  const ctx = useContext(SessionContext);
  if (!ctx) throw new Error('useSession must be used inside SessionProvider');
  return ctx;
}
