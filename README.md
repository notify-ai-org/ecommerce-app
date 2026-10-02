<p align="center">
  <img src="../assets/notify-ai-logo.svg" alt="Notify.ai" width="96" />
</p>

<h1 align="center">Notify.ai</h1>

<p align="center"><b>E-Commerce Example Application</b> — SDK Integration Demo</p>

---

## 📖 Overview

The `ecommerce-app` module is a fully functional sample Spring Boot application demonstrating how to integrate the **Notify.ai** client SDK into an e-commerce ecosystem. It defines typical domain structures (Carts, Customers, Orders, Shipments) and illustrates how `@Event`, `@Rule`, `@Callback`, and `@Model` annotations work together to enable agentic notifications.

## ⚙️ How it is annotated

- **Vocabulary Models**: event payloads in `model/` (`OrderPayload`, `CartPayload`, `ShipmentPayload`, `UserLoginPayload`, `ProductViewedPayload`, `AddToCartPayload`, `PriceDropPayload`) are marked with `@Model` / `@Vocabulary`.
- **Events**: `@Event` methods take the payload as their only argument and are always invoked through a Spring proxy (from another bean), so the SDK aspect intercepts them.
- **Subject Suppliers**: built from the customer's profile (name, email, mobile number) via `events/CustomerSubjects`. Every subject carries `firstName`, `fullName` and `customerId` attributes.

| Event | Fired by | Subjects | Rules |
|---|---|---|---|
| `USER_LOGIN` | `POST /api/auth/login` | Email + SMS to the customer (welcome when `firstLogin`, security alert otherwise) | — |
| `PRODUCT_VIEWED` | `POST /api/products/{id}/view` | Email to the viewer | `repeat-interest`: view count ≥ 2 |
| `ADD_TO_CART` | `POST /api/cart/items` | Email to the cart owner | — |
| `PRICE_DROP` | `PUT /api/products/{id}/price` with a lower price | Email to everyone who viewed or carted the product; SMS too when the drop is ≥ 20% | `meaningful-drop`: drop ≥ 5% |
| `ORDER_PLACED` | `POST /api/orders/checkout`, `POST /api/orders/place` | Email | `fraud-check` (< $1000), `inventory-check` |
| `PAYMENT_FAILED` | `POST /api/orders/payment-failed` | SMS | — |
| `ORDER_SHIPPED` | `POST /api/orders/{id}/simulate-shipment`, `POST /api/orders/ship` | Email | — |
| `ABANDONED_CART` | `POST /api/cart/abandon`, `POST /api/orders/abandon-cart` | Email | — |

## 💾 Persistence (H2)

Customers, products, carts, browsing history and orders are stored in a file-backed H2 database at `examples/ecommerce-app/data/` (git-ignored), created and seeded on first start. Override with `ECOMMERCE_DB_URL` (for example `jdbc:h2:mem:ecommerce` for a throwaway DB). The H2 console is at `http://localhost:8090/h2-console` (user `sa`, empty password).

Seeded customers `alice@example.com`, `rohan.nn1203@gmail.com` and `carol@example.com` sign in with password `password123`.

## 🛍️ Storefront UI

A React + Vite app in `ui/` with login, products, product detail, cart and orders pages plus logout. Every action shows a toast naming the Notify event it fired.

The login form takes name, email, mobile number and password. An unknown email creates the account; a known email must match its password, and the name/number are updated from the form.

The build is written to `src/main/resources/static/portals/shop/` and served by `PortalController` (same pattern as the access service) at `http://localhost:8090/portals/shop/`; `/` redirects there.

```bash
# Build the UI into the Spring Boot classpath
cd examples/ecommerce-app/ui && npm install && npm run build
# …or as part of the Maven build
mvn -Pui package -pl examples/ecommerce-app

# Hot-reload development (proxies /api to :8090)
cd examples/ecommerce-app/ui && npm run dev   # → http://localhost:5174/portals/shop/
```

## 🚀 Running Locally

You can run the e-commerce sample locally to test the event capture and dispatch flow.

### Prerequisites
- **Notify.ai Control Plane**: Ensure the backend application (`access` module) is running on `http://localhost:8080`.

### Execution
From the root directory of the project, run:
```bash
mvn spring-boot:run -pl examples/ecommerce-app
```
By default, the application runs on port **8090**. Open `http://localhost:8090/` for the storefront.

### Testing the Integration
Storefront endpoints use the session cookie from login:
```bash
curl -c jar -H "Content-Type: application/json" -X POST http://localhost:8090/api/auth/login \
  -d '{"name":"Alice Johnson","email":"alice@example.com","phone":"+1-555-0101","password":"password123"}'
curl -b jar -X POST http://localhost:8090/api/products/P-1003/view
curl -b jar -H "Content-Type: application/json" -X POST http://localhost:8090/api/cart/items \
  -d '{"productId":"P-1003","quantity":1}'
curl -b jar -H "Content-Type: application/json" -X POST http://localhost:8090/api/orders/checkout \
  -d '{"shippingAddress":"221B Baker Street"}'

# Price changes are an unauthenticated test hook; a lower price fires PRICE_DROP
curl -H "Content-Type: application/json" -X PUT http://localhost:8090/api/products/P-1003/price \
  -d '{"newPrice":199.00}'
```

The raw test hooks still work without a session:
```bash
curl -X POST http://localhost:8090/api/orders/place \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD-1","customerId":"CUST-1","amount":120.0,"items":["Mechanical Keyboard"]}'
```
These calls will be intercepted by the Notify SDK and pushed as event payloads to the main control plane (`access` module) on port `8080`.

---

## 👥 Developer Contact & Contributing

For questions, issues, or support regarding this module:
- **Lead Developer**: Rohan Naik ([rohan.naik07@github](https://github.com/rohan-naik07))
- **Email**: dev-support@notify.ai

### Contributing

We welcome contributions! Please follow these guidelines:
1. **Fork** the repository and create your branch from `master`.
2. Ensure your changes compile and all tests pass.
3. Follow the project's Java coding standards and naming conventions.
4. Submit a **Pull Request** with a detailed description of your changes.
