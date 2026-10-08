package com.notify.ecommerce.service;

import com.notify.agent.annotations.*;
import com.notify.agent.client.models.subject.SmsSubject;
import com.notify.agent.client.models.subject.Subject;
import com.notify.ecommerce.entity.Customer;
import com.notify.ecommerce.entity.OrderLine;
import com.notify.ecommerce.entity.PurchaseOrder;
import com.notify.ecommerce.events.CustomerSubjects;
import com.notify.ecommerce.events.InAppSubjects;
import com.notify.ecommerce.model.*;
import com.notify.ecommerce.repository.CustomerRepository;
import com.notify.ecommerce.repository.PurchaseOrderRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Core e-commerce service that exercises all Notify SDK annotations.
 *
 * Events:
 * - ORDER_PLACED (immediate email, fraud + inventory rules)
 * - PAYMENT_FAILED (high-priority SMS)
 * - ORDER_SHIPPED (immediate email)
 * - ABANDONED_CART (deferred/scheduled email)
 *
 * Storefront events (USER_LOGIN, PRODUCT_VIEWED, ADD_TO_CART, PRICE_DROP) live
 * in {@code com.notify.ecommerce.events}.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final CustomerRepository customers;
    private final PurchaseOrderRepository orders;
    private final InAppSubjects inApp;

    public OrderService(CustomerRepository customers, PurchaseOrderRepository orders, InAppSubjects inApp) {
        this.customers = customers;
        this.orders = orders;
        this.inApp = inApp;
    }

    // ═══════════════════════════════════════════
    // EVENTS
    // ═══════════════════════════════════════════

    @Event(key = "ORDER_PLACED", description = "Customer placed an order", eventType = "static", scheduleIntent = "immediate", preferredTimeWindow = "09:00-18:00", priority = 2, payload = OrderPayload.class)
    public OrderPayload placeOrder(OrderPayload payload) {
        log.info("📦 Order placed: {} for customer {} — ${}", payload.getOrderId(), payload.getCustomerId(),
                payload.getAmount());
        // Storefront checkout persists the order (with priced lines) before
        // firing; the raw test endpoint only carries item names.
        if (!orders.existsById(payload.getOrderId())) {
            List<OrderLine> lines = payload.getItems() == null ? List.of()
                    : payload.getItems().stream().map(name -> new OrderLine(null, name, 1, null)).toList();
            orders.save(new PurchaseOrder(payload.getOrderId(), payload.getCustomerId(), payload.getAmount(),
                    payload.getShippingAddress(), lines));
        }
        return payload;
    }

    @Event(key = "PAYMENT_FAILED", description = "Payment processing failed for an order", eventType = "static", scheduleIntent = "immediate", preferredTimeWindow = "00:00-23:59", priority = 4, payload = OrderPayload.class)
    public OrderPayload reportPaymentFailed(OrderPayload payload) {
        log.warn("💳 Payment FAILED for order: {} — ${}", payload.getOrderId(), payload.getAmount());
        orders.findById(payload.getOrderId()).ifPresent(order -> {
            order.setStatus(PurchaseOrder.Status.PAYMENT_FAILED);
            orders.save(order);
        });
        return payload;
    }

    @Event(key = "ORDER_SHIPPED", description = "Order has been shipped to the customer", eventType = "static", scheduleIntent = "immediate", preferredTimeWindow = "09:00-18:00", priority = 5, payload = ShipmentPayload.class)
    public ShipmentPayload shipOrder(ShipmentPayload payload) {
        log.info("Order shipped: {} via {} — tracking: {}", payload.getOrderId(), payload.getCarrier(),
                payload.getTrackingNumber());
        orders.findById(payload.getOrderId()).ifPresent(order -> {
            order.markShipped(payload.getCarrier(), payload.getTrackingNumber(), payload.getEstimatedDelivery());
            orders.save(order);
        });
        return payload;
    }

    @Event(key = "ABANDONED_CART", description = "Customer abandoned their shopping cart", eventType = "deferred", scheduleIntent = "deferred", preferredTimeWindow = "09:00-21:00", priority = 3, payload = CartPayload.class)
    public CartPayload abandonCart(CartPayload payload) {
        // Cart contents are already persisted as cart_items; nothing to store here.
        log.info("🛒 Cart abandoned: {} by customer {}", payload.getCartId(), payload.getCustomerId());
        return payload;
    }

    // ═══════════════════════════════════════════
    // SUBJECT SUPPLIERS
    // ═══════════════════════════════════════════

    @SubjectSupplier(event = "ORDER_PLACED", description = "Resolves order customer to email recipients")
    public List<Subject> getOrderPlacedSubjects(OrderPayload payload) {
        Customer c = customers.findById(payload.getCustomerId()).orElse(null);
        if (c == null) {
            log.warn("Customer not found: {}", payload.getCustomerId());
            return List.of();
        }
        return inApp.with(c, List.of(CustomerSubjects.email(c)));
    }

    @SubjectSupplier(event = "PAYMENT_FAILED", description = "Resolves customer to SMS for urgent payment alerts")
    public List<Subject> getPaymentFailedSubjects(OrderPayload payload) {
        Customer c = customers.findById(payload.getCustomerId()).orElse(null);
        if (c == null)
            return List.of();
        if (!CustomerSubjects.hasPhone(c))
            return inApp.with(c, List.of());
        return inApp.with(c, List.of(new SmsSubject(c.getPhone(), null, CustomerSubjects.attributes(c))));
    }

    @SubjectSupplier(event = "ORDER_SHIPPED", description = "Resolves order to email recipients for shipment tracking")
    public List<Subject> getShipmentSubjects(ShipmentPayload payload) {
        // Look up the order to get the customer
        PurchaseOrder order = orders.findById(payload.getOrderId()).orElse(null);
        if (order == null) {
            log.warn("Order not found for shipment: {}", payload.getOrderId());
            return List.of();
        }
        return customers.findById(order.getCustomerId())
                .map(c -> inApp.with(c, List.of(CustomerSubjects.email(c))))
                .orElse(List.of());
    }

    @SubjectSupplier(event = "ABANDONED_CART", description = "Resolves cart owner to email for re-engagement")
    public List<Subject> getCartSubjects(CartPayload payload) {
        return customers.findById(payload.getCustomerId())
                .map(c -> inApp.with(c, List.of(CustomerSubjects.email(c))))
                .orElse(List.of());
    }

    // ═══════════════════════════════════════════
    // VOCABULARY SUPPLIER
    // ═══════════════════════════════════════════

    @VocabularySupplier(event = "ORDER_PLACED", description = "Enriches order payload with customer name and item count")
    public OrderPayload orderPlacedVocabulary(OrderPayload payload) {
        Customer c = customers.findById(payload.getCustomerId()).orElse(null);
        if (c != null) {
            // Enrich by adding shipping address if missing
            if (payload.getShippingAddress() == null || payload.getShippingAddress().isEmpty()) {
                payload.setShippingAddress("Default address for " + c.getName());
            }
        }
        return payload;
    }

    // ═══════════════════════════════════════════
    // RULES
    // ═══════════════════════════════════════════

    @Rule(name = "fraud-check", event = "ORDER_PLACED", description = "Blocks orders over $1000 as potential fraud")
    public boolean fraudCheck(OrderPayload payload) {
        boolean passed = payload.getAmount() < 1000.0;
        log.info("🔍 Fraud check for order {}: {} (amount=${})", payload.getOrderId(), passed ? "PASSED" : "BLOCKED",
                payload.getAmount());
        return passed;
    }

    @Rule(name = "inventory-check", event = "ORDER_PLACED", description = "Checks whether all items are in stock")
    public boolean inventoryCheck(OrderPayload payload) {
        // In-memory: always in stock
        log.info("📦 Inventory check for order {}: all {} items in stock", payload.getOrderId(),
                payload.getItems() != null ? payload.getItems().size() : 0);
        return true;
    }

    // ═══════════════════════════════════════════
    // CALLBACKS
    // ═══════════════════════════════════════════

    @Callback(event = "ORDER_PLACED", when = Callback.When.BEFORE)
    public void beforeOrderPlaced(OrderPayload payload) {
        log.info("⏳ [BEFORE] About to process ORDER_PLACED for order: {}", payload.getOrderId());
    }

    @Callback(event = "ORDER_PLACED", when = Callback.When.AFTER)
    public void afterOrderPlaced(OrderPayload payload) {
        log.info("✅ [AFTER] ORDER_PLACED processing complete for order: {}", payload.getOrderId());
    }
}
