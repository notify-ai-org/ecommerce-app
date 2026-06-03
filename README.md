<p align="center">
  <span style="font-size: 50px;">⚡</span>
</p>
<h1 align="center" style="border-bottom: none;">
  <span style="background: linear-gradient(135deg, #eab308 0%, #f97316 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; font-weight: 800;">Notify.ai</span>
</h1>
<p align="center"><b>E-Commerce Example Application</b> — SDK Integration Demo</p>

---

## 📖 Overview

The `ecommerce-app` module is a fully functional sample Spring Boot application demonstrating how to integrate the **Notify.ai** client SDK into an e-commerce ecosystem. It defines typical domain structures (Carts, Customers, Orders, Shipments) and illustrates how `@Event`, `@Rule`, `@Callback`, and `@Model` annotations work together to enable agentic notifications.

## ⚙️ How it is annotated

- **Vocabulary Models**: `Customer` and `OrderPayload` classes are marked with `@Model` or `@Vocabulary` annotations, exposing their properties to the semantic analysis engine.
- **Events**: Methods in `OrderService` (like `createOrder`, `processShipment`) are decorated with `@Event` to capture transactional events automatically when triggered by controllers.
- **Subject Suppliers**: Resolves the target recipient email/phone number for events.

## 🚀 Running Locally

You can run the e-commerce sample locally to test the event capture and dispatch flow.

### Prerequisites
- **Notify.ai Control Plane**: Ensure the backend application (`access` module) is running on `http://localhost:8080`.

### Execution
From the root directory of the project, run:
```bash
mvn spring-boot:run -pl examples/ecommerce-app
```
By default, the application runs on port **8090**.

### Testing the Integration
Once the application is running, trigger order/cart events by making HTTP requests:
```bash
# Add an item to the shopping cart
curl -X POST http://localhost:8090/order/cart \
  -H "Content-Type: application/json" \
  -d '{"customerId":"cust-99","item":"Mechanical Keyboard","price":120.0}'

# Check out/place an order
curl -X POST http://localhost:8090/order/checkout \
  -H "Content-Type: application/json" \
  -d '{"customerId":"cust-99","items":["Mechanical Keyboard"],"total":120.0}'
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
