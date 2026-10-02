import { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { api, ApiError, money, type Order, type OrderStatus } from '../api';
import { useToast } from '../toast';

const STATUS_LABEL: Record<OrderStatus, string> = {
  PLACED: 'Placed',
  SHIPPED: 'Shipped',
  PAYMENT_FAILED: 'Payment failed',
};

const dateFormat = new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' });

export default function OrdersPage() {
  const toast = useToast();
  const highlight = (useLocation().state as { highlight?: string } | null)?.highlight;
  const [orders, setOrders] = useState<Order[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [shipping, setShipping] = useState<string | null>(null);

  useEffect(() => {
    api.orders().then(setOrders).catch((e) => setError(e.message));
  }, []);

  async function ship(orderId: string) {
    setShipping(orderId);
    try {
      const updated = await api.simulateShipment(orderId);
      setOrders((list) => list?.map((o) => (o.orderId === orderId ? updated : o)) ?? null);
      toast.success(`Order ${orderId} shipped`, 'ORDER_SHIPPED');
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : 'Could not ship order');
    } finally {
      setShipping(null);
    }
  }

  return (
    <div className="page">
      <h1>Orders</h1>
      {error && <div className="alert">{error}</div>}
      {!orders && !error && <div className="card skeleton skeleton-lg" />}
      {orders && orders.length === 0 && (
        <div className="empty">
          <p>You haven't placed any orders yet.</p>
          <Link to="/products" className="btn btn-primary">Start shopping</Link>
        </div>
      )}
      <div className="orders">
        {orders?.map((o) => (
          <article key={o.orderId} className={`card order ${o.orderId === highlight ? 'order-new' : ''}`}>
            <header className="order-head">
              <div>
                <h3>{o.orderId}</h3>
                <span className="muted small">{dateFormat.format(new Date(o.createdAt))}</span>
              </div>
              <span className={`status status-${o.status.toLowerCase()}`}>{STATUS_LABEL[o.status]}</span>
            </header>
            <ul className="order-lines">
              {o.lines.map((l, i) => (
                <li key={i}>
                  <span>{l.quantity} × {l.productName}</span>
                  <span>{l.unitPrice != null ? money(l.unitPrice * l.quantity) : '—'}</span>
                </li>
              ))}
            </ul>
            <footer className="order-foot">
              <div className="small">
                {o.shippingAddress && <div><span className="muted">Ship to</span> {o.shippingAddress}</div>}
                {o.trackingNumber && (
                  <div><span className="muted">{o.carrier}</span> <code>{o.trackingNumber}</code> · arrives {o.estimatedDelivery}</div>
                )}
              </div>
              <div className="order-total">
                <strong>{money(o.amount)}</strong>
                {o.status === 'PLACED' && (
                  <button className="btn btn-secondary" disabled={shipping === o.orderId} onClick={() => ship(o.orderId)}>
                    {shipping === o.orderId ? 'Shipping…' : 'Simulate shipment'}
                  </button>
                )}
              </div>
            </footer>
          </article>
        ))}
      </div>
    </div>
  );
}
