export interface Customer {
  id: string;
  name: string;
  email: string;
  phone: string;
  createdAt: string;
}

export interface Product {
  id: string;
  name: string;
  description: string;
  category: string;
  price: number;
  listPrice: number;
  stock: number;
}

export interface CartLine {
  productId: string;
  name: string;
  category: string;
  unitPrice: number;
  listPrice: number;
  quantity: number;
  lineTotal: number;
}

export interface Cart {
  cartId: string;
  items: CartLine[];
  itemCount: number;
  total: number;
}

export interface OrderLine {
  productId: string | null;
  productName: string;
  quantity: number;
  unitPrice: number | null;
}

export type OrderStatus = 'PLACED' | 'PAYMENT_FAILED' | 'SHIPPED';

export interface Order {
  orderId: string;
  amount: number;
  status: OrderStatus;
  shippingAddress: string | null;
  carrier: string | null;
  trackingNumber: string | null;
  estimatedDelivery: string | null;
  createdAt: string;
  lines: OrderLine[];
}

export interface LoginForm {
  name: string;
  email: string;
  phone: string;
  password: string;
}

export interface PriceDrop {
  previousPrice: number;
  newPrice: number;
  dropPercent: number;
}

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

/** Called on any 401 so the app can drop the session and show the login page. */
let onUnauthorized: () => void = () => {};
export function setUnauthorizedHandler(handler: () => void) {
  onUnauthorized = handler;
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const res = await fetch(path, {
    method,
    credentials: 'same-origin',
    headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (res.status === 204) return undefined as T;
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    if (res.status === 401 && path !== '/api/auth/login') onUnauthorized();
    throw new ApiError(res.status, data?.message ?? `Request failed (${res.status})`);
  }
  return data as T;
}

export const api = {
  me: () => request<Customer>('GET', '/api/auth/me'),
  login: (form: LoginForm) =>
    request<{ customer: Customer; firstLogin: boolean }>('POST', '/api/auth/login', form),
  logout: () => request<void>('POST', '/api/auth/logout'),

  products: () => request<Product[]>('GET', '/api/products'),
  product: (id: string) => request<Product>('GET', `/api/products/${encodeURIComponent(id)}`),
  viewProduct: (id: string) =>
    request<{ viewCount: number }>('POST', `/api/products/${encodeURIComponent(id)}/view`),
  changePrice: (id: string, newPrice: number) =>
    request<{ product: Product; priceDrop: PriceDrop | null }>(
      'PUT', `/api/products/${encodeURIComponent(id)}/price`, { newPrice }),

  cart: () => request<Cart>('GET', '/api/cart'),
  addToCart: (productId: string, quantity: number) =>
    request<Cart>('POST', '/api/cart/items', { productId, quantity }),
  updateCartItem: (productId: string, quantity: number) =>
    request<Cart>('PATCH', `/api/cart/items/${encodeURIComponent(productId)}`, { quantity }),
  removeCartItem: (productId: string) =>
    request<Cart>('DELETE', `/api/cart/items/${encodeURIComponent(productId)}`),
  abandonCart: () => request<{ cartId: string }>('POST', '/api/cart/abandon'),

  orders: () => request<Order[]>('GET', '/api/orders'),
  checkout: (shippingAddress: string) => request<Order>('POST', '/api/orders/checkout', { shippingAddress }),
  simulateShipment: (orderId: string) =>
    request<Order>('POST', `/api/orders/${encodeURIComponent(orderId)}/simulate-shipment`),
};

const currency = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });
export const money = (n: number) => currency.format(n);

export const firstName = (name: string) => name.trim().split(/\s+/)[0] ?? name;
