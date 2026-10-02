import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, ApiError, type Product } from '../api';
import Price from '../components/Price';
import ProductArt from '../components/ProductArt';
import { useSession } from '../session';
import { useToast } from '../toast';

type Sort = 'featured' | 'price-asc' | 'price-desc';

export default function ProductsPage() {
  const { setCart } = useSession();
  const toast = useToast();
  const [products, setProducts] = useState<Product[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState('All');
  const [sort, setSort] = useState<Sort>('featured');
  const [adding, setAdding] = useState<string | null>(null);

  useEffect(() => {
    api.products().then(setProducts).catch((e) => setError(e.message));
  }, []);

  const categories = useMemo(
    () => ['All', ...new Set((products ?? []).map((p) => p.category))],
    [products],
  );

  const visible = useMemo(() => {
    const q = query.trim().toLowerCase();
    const list = (products ?? []).filter((p) =>
      (category === 'All' || p.category === category) &&
      (!q || p.name.toLowerCase().includes(q) || p.description.toLowerCase().includes(q)));
    if (sort === 'price-asc') return [...list].sort((a, b) => a.price - b.price);
    if (sort === 'price-desc') return [...list].sort((a, b) => b.price - a.price);
    return list;
  }, [products, query, category, sort]);

  async function addToCart(p: Product) {
    setAdding(p.id);
    try {
      setCart(await api.addToCart(p.id, 1));
      toast.success(`Added ${p.name} to cart`, 'ADD_TO_CART');
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : 'Could not add to cart');
    } finally {
      setAdding(null);
    }
  }

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <h1>Products</h1>
          <p className="muted">Open a product to fire <code>PRODUCT_VIEWED</code>.</p>
        </div>
        <div className="filters">
          <input className="search" type="search" placeholder="Search products" value={query}
            onChange={(e) => setQuery(e.target.value)} aria-label="Search products" />
          <select value={sort} onChange={(e) => setSort(e.target.value as Sort)} aria-label="Sort">
            <option value="featured">Featured</option>
            <option value="price-asc">Price: low to high</option>
            <option value="price-desc">Price: high to low</option>
          </select>
        </div>
      </div>

      <div className="chips" role="tablist" aria-label="Categories">
        {categories.map((c) => (
          <button key={c} role="tab" aria-selected={c === category}
            className={`chip ${c === category ? 'chip-active' : ''}`} onClick={() => setCategory(c)}>
            {c}
          </button>
        ))}
      </div>

      {error && <div className="alert">{error}</div>}
      {!products && !error && <div className="grid">{Array.from({ length: 8 }, (_, i) => <div key={i} className="card skeleton" />)}</div>}
      {products && visible.length === 0 && <div className="empty">No products match your search.</div>}

      <div className="grid">
        {visible.map((p) => (
          <article key={p.id} className="card product-card">
            <Link to={`/products/${p.id}`} className="product-link">
              <ProductArt id={p.id} category={p.category} />
              <div className="product-body">
                <span className="eyebrow">{p.category}</span>
                <h3>{p.name}</h3>
                <Price price={p.price} listPrice={p.listPrice} />
              </div>
            </Link>
            <div className="product-actions">
              <span className={`stock ${p.stock < 15 ? 'stock-low' : ''}`}>
                {p.stock === 0 ? 'Out of stock' : p.stock < 15 ? `Only ${p.stock} left` : 'In stock'}
              </span>
              <button className="btn btn-secondary" disabled={p.stock === 0 || adding === p.id}
                onClick={() => addToCart(p)}>
                {adding === p.id ? 'Adding…' : 'Add to cart'}
              </button>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
