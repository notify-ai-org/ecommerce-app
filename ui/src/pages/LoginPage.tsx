import { useState, type FormEvent } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { ApiError, firstName, type LoginForm } from '../api';
import { useSession } from '../session';
import { useToast } from '../toast';

type Errors = Partial<Record<keyof LoginForm, string>>;

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PHONE_RE = /^\+?[0-9][0-9 ()-]{6,19}$/;

function validate(form: LoginForm): Errors {
  const errors: Errors = {};
  if (!form.name.trim()) errors.name = 'Name is required';
  if (!EMAIL_RE.test(form.email.trim())) errors.email = 'Enter a valid email address';
  if (!PHONE_RE.test(form.phone.trim())) errors.phone = 'Enter a valid mobile number';
  if (form.password.length < 6) errors.password = 'Password must be at least 6 characters';
  return errors;
}

export default function LoginPage() {
  const { customer, login } = useSession();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from ?? '/products';

  const [form, setForm] = useState<LoginForm>({ name: '', email: '', phone: '', password: '' });
  const [errors, setErrors] = useState<Errors>({});
  const [serverError, setServerError] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  if (customer) return <Navigate to={from} replace />;

  const set = (field: keyof LoginForm) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((f) => ({ ...f, [field]: e.target.value }));
    setErrors((er) => ({ ...er, [field]: undefined }));
  };

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    const found = validate(form);
    setErrors(found);
    setServerError(null);
    if (Object.keys(found).length > 0) return;

    setSubmitting(true);
    try {
      const { customer: c, firstLogin } = await login(form);
      toast.success(firstLogin ? `Welcome to Notify Shop, ${firstName(c.name)}!` : `Welcome back, ${firstName(c.name)}`, 'USER_LOGIN');
      navigate(from, { replace: true });
    } catch (err) {
      setServerError(err instanceof ApiError ? err.message : 'Could not sign in. Is the server running?');
    } finally {
      setSubmitting(false);
    }
  }

  const field = (key: keyof LoginForm, label: string, input: JSX.Element, hint?: string) => (
    <div className={`field ${errors[key] ? 'field-invalid' : ''}`}>
      <label htmlFor={key}>{label}</label>
      {input}
      {errors[key] ? <p className="field-error" id={`${key}-error`}>{errors[key]}</p>
        : hint && <p className="field-hint">{hint}</p>}
    </div>
  );

  return (
    <div className="login-screen">
      <section className="login-aside">
        <div className="brand brand-lg">
          <span className="brand-mark" aria-hidden>
            <svg viewBox="0 0 24 24" width="22" height="22">
              <path d="M5 8h14l-1.4 11H6.4z" fill="none" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
              <path d="M9 8a3 3 0 0 1 6 0" fill="none" stroke="currentColor" strokeWidth="2" />
            </svg>
          </span>
          Notify Shop
        </div>
        <h1>Shop, and see every notification it triggers.</h1>
        <p>
          Each action in this store — signing in, viewing a product, adding to cart, a price drop,
          checking out — fires a Notify.ai event. Your name, email and mobile number are used to
          address those messages.
        </p>
        <ul className="event-list">
          <li><code>USER_LOGIN</code> email + SMS</li>
          <li><code>PRODUCT_VIEWED</code> email nudge on repeat views</li>
          <li><code>ADD_TO_CART</code> email</li>
          <li><code>PRICE_DROP</code> email, + SMS for 20%+ drops</li>
        </ul>
      </section>

      <section className="login-card-wrap">
        <form className="card login-card" onSubmit={onSubmit} noValidate>
          <h2>Sign in</h2>
          <p className="muted">New email? We'll create your account automatically.</p>

          {serverError && <div className="alert" role="alert">{serverError}</div>}

          {field('name', 'Full name',
            <input id="name" autoComplete="name" value={form.name} onChange={set('name')}
              placeholder="Alice Johnson" aria-invalid={!!errors.name} />)}
          {field('email', 'Email',
            <input id="email" type="email" autoComplete="email" value={form.email} onChange={set('email')}
              placeholder="you@example.com" aria-invalid={!!errors.email} />)}
          {field('phone', 'Mobile number',
            <input id="phone" type="tel" autoComplete="tel" value={form.phone} onChange={set('phone')}
              placeholder="+1 555 010 1234" aria-invalid={!!errors.phone} />,
            'Used for SMS alerts')}
          {field('password', 'Password',
            <div className="password-input">
              <input id="password" type={showPassword ? 'text' : 'password'} autoComplete="current-password"
                value={form.password} onChange={set('password')} aria-invalid={!!errors.password} />
              <button type="button" className="btn-link" onClick={() => setShowPassword((s) => !s)}>
                {showPassword ? 'Hide' : 'Show'}
              </button>
            </div>)}

          <button className="btn btn-primary btn-block" type="submit" disabled={submitting}>
            {submitting ? 'Signing in…' : 'Sign in'}
          </button>
          <p className="demo-hint">Demo account: <code>alice@example.com</code> / <code>password123</code></p>
        </form>
      </section>
    </div>
  );
}
