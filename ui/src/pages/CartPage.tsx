import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api, ApiError, money } from '../api';
import Price from '../components/Price';
import ProductArt from '../components/ProductArt';
import QuantityStepper from '../components/QuantityStepper';
import { useSession } from '../session';
import { useToast } from '../toast';

export default function CartPage() {
  const { cart, setCart, refreshCart } = useSession();
  const toast = useToast();
  const navigate = useNavigate();
  const [busy, setBusy] = useState<string | null>(null);
  const [address, setAddress] = useState('');
  const [placing, setPlacing] = useState(false);

  async function run(key: string, action: () => Promise<void>) {
    setBusy(key);
    try {
      await action();
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : 'Something went wrong');
    } finally {
      setBusy(null);
    }
  }

  async function checkout(e: FormEvent) {
    e.preventDefault();
    if (!address.trim()) {
      toast.error('Enter a shipping address');
      return;
    }
    setPlacing(true);
    try {
      const order = await api.checkout(address);
      await refreshCart();
      toast.success(`Order ${order.orderId} placed`, 'ORDER_PLACED');
      navigate('/orders', { state: { highlight: order.orderId } });
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Checkout failed');
    } finally {
      setPlacing(false);
    }
  }

  if (!cart) return <div className="page"><div className="card skeleton skeleton-lg" /></div>;

  if (cart.items.length === 0) {
    return (
      <div className="page">
        <h1>Cart</h1>
        <div className="empty">
          <p>Your cart is empty.</p>
          <Link to="/products" className="btn btn-primary">Browse products</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <h1>Cart <span className="muted">({cart.itemCount} {cart.itemCount === 1 ? 'item' : 'items'})</span></h1>
      <div className="cart-layout">
        <ul className="card cart-lines">
          {cart.items.map((line) => (
            <li key={line.productId} className="cart-line">
              <ProductArt id={line.productId} category={line.category} size="sm" />
              <div className="cart-line-info">
                <Link to={`/products/${line.productId}`}><strong>{line.name}</strong></Link>
                <Price price={line.unitPrice} listPrice={line.listPrice} />
              </div>
              <QuantityStepper value={line.quantity} min={1} disabled={busy === line.productId}
                onChange={(q) => run(line.productId, async () => setCart(await api.updateCartItem(line.productId, q)))} />
              <span className="cart-line-total">{money(line.lineTotal)}</span>
              <button className="btn-link danger" disabled={busy === line.productId}
                onClick={() => run(line.productId, async () => setCart(await api.removeCartItem(line.productId)))}>
                Remove
              </button>
            </li>
          ))}
        </ul>

        <aside className="card summary">
          <h3>Order summary</h3>
          <div className="summary-row"><span>Subtotal</span><span>{money(cart.total)}</span></div>
          <div className="summary-row"><span>Shipping</span><span>Free</span></div>
          <div className="summary-row summary-total"><span>Total</span><span>{money(cart.total)}</span></div>
          <form onSubmit={checkout}>
            <div className="field">
              <label htmlFor="address">Shipping address</label>
              <textarea id="address" rows={3} value={address} onChange={(e) => setAddress(e.target.value)}
                placeholder="221B Baker Street, London" />
            </div>
            <button className="btn btn-primary btn-block" disabled={placing}>
              {placing ? 'Placing order…' : `Place order · ${money(cart.total)}`}
            </button>
          </form>
          {cart.total >= 1000 && (
            <p className="muted small">Orders of $1,000 or more fail the <code>fraud-check</code> rule, so no confirmation is sent.</p>
          )}
          <button className="btn-link small" disabled={busy === 'abandon'}
            onClick={() => run('abandon', async () => {
              await api.abandonCart();
              toast.success('Reported cart as abandoned', 'ABANDONED_CART');
            })}>
            Simulate abandoned cart
          </button>
        </aside>
      </div>
    </div>
  );
}
