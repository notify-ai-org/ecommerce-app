import { useEffect, useRef, useState, type FormEvent } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api, ApiError, money, type Product } from '../api';
import Price from '../components/Price';
import ProductArt from '../components/ProductArt';
import QuantityStepper from '../components/QuantityStepper';
import { useSession } from '../session';
import { useToast } from '../toast';

export default function ProductDetailPage() {
  const { productId = '' } = useParams();
  const { setCart } = useSession();
  const toast = useToast();
  const [product, setProduct] = useState<Product | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [viewCount, setViewCount] = useState<number | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [adding, setAdding] = useState(false);
  const [newPrice, setNewPrice] = useState('');
  const [repricing, setRepricing] = useState(false);
  // StrictMode runs effects twice in dev; record each product view once.
  const viewed = useRef<string | null>(null);

  useEffect(() => {
    setProduct(null);
    setError(null);
    setQuantity(1);
    api.product(productId)
      .then((p) => {
        setProduct(p);
        setNewPrice((Math.round(p.price * 0.85 * 100) / 100).toFixed(2));
      })
      .catch((e) => setError(e.message));

    if (viewed.current !== productId) {
      viewed.current = productId;
      api.viewProduct(productId).then((v) => setViewCount(v.viewCount)).catch(() => {});
    }
  }, [productId]);

  async function addToCart() {
    if (!product) return;
    setAdding(true);
    try {
      setCart(await api.addToCart(product.id, quantity));
      toast.success(`Added ${quantity} × ${product.name} to cart`, 'ADD_TO_CART');
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : 'Could not add to cart');
    } finally {
      setAdding(false);
    }
  }

  async function applyPrice(e: FormEvent) {
    e.preventDefault();
    if (!product) return;
    const value = Number(newPrice);
    if (!Number.isFinite(value) || value <= 0) {
      toast.error('Enter a price greater than zero');
      return;
    }
    setRepricing(true);
    try {
      const res = await api.changePrice(product.id, value);
      setProduct(res.product);
      if (res.priceDrop) {
        toast.success(`Price dropped ${res.priceDrop.dropPercent}% to ${money(res.priceDrop.newPrice)}`, 'PRICE_DROP');
      } else {
        toast.success(`Price set to ${money(res.product.price)} (no drop, no event)`);
      }
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not change price');
    } finally {
      setRepricing(false);
    }
  }

  if (error) return <div className="page"><Link to="/products" className="back">← Products</Link><div className="alert">{error}</div></div>;
  if (!product) return <div className="page"><div className="card skeleton skeleton-lg" /></div>;

  return (
    <div className="page">
      <Link to="/products" className="back">← Products</Link>
      <div className="detail">
        <ProductArt id={product.id} category={product.category} size="lg" />
        <div className="detail-body">
          <span className="eyebrow">{product.category}</span>
          <h1>{product.name}</h1>
          <Price price={product.price} listPrice={product.listPrice} />
          <p className="detail-desc">{product.description}</p>

          <div className="detail-buy">
            <QuantityStepper value={quantity} max={Math.max(1, Math.min(99, product.stock))}
              onChange={setQuantity} disabled={product.stock === 0} />
            <button className="btn btn-primary" onClick={addToCart} disabled={adding || product.stock === 0}>
              {adding ? 'Adding…' : `Add to cart · ${money(product.price * quantity)}`}
            </button>
          </div>
          <p className="muted small">
            {product.stock} in stock
            {viewCount !== null && <> · You've viewed this {viewCount === 1 ? 'once' : `${viewCount} times`}
              {viewCount >= 2 ? ' — eligible for a PRODUCT_VIEWED nudge' : ''}</>}
          </p>

          <form className="demo-panel" onSubmit={applyPrice}>
            <div>
              <h4>Merchandising demo</h4>
              <p className="muted small">
                Lower the price to fire <code>PRICE_DROP</code> to everyone who viewed or carted this
                product. Drops of 20% or more also go out by SMS.
              </p>
            </div>
            <div className="demo-row">
              <label className="sr-only" htmlFor="newPrice">New price</label>
              <span className="input-prefix">$</span>
              <input id="newPrice" inputMode="decimal" value={newPrice} onChange={(e) => setNewPrice(e.target.value)} />
              <button className="btn btn-secondary" disabled={repricing}>
                {repricing ? 'Applying…' : 'Apply price'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
