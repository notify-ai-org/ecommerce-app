package com.notify.ecommerce.service;

import com.notify.ecommerce.dto.CartDto;
import com.notify.ecommerce.dto.OrderDto;
import com.notify.ecommerce.entity.OrderLine;
import com.notify.ecommerce.entity.Product;
import com.notify.ecommerce.entity.PurchaseOrder;
import com.notify.ecommerce.model.OrderPayload;
import com.notify.ecommerce.model.ShipmentPayload;
import com.notify.ecommerce.repository.ProductRepository;
import com.notify.ecommerce.repository.PurchaseOrderRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Turns a customer's cart into a persisted order, then fires ORDER_PLACED. */
@Service
public class CheckoutService {

    private final CartService cartService;
    private final ProductRepository products;
    private final PurchaseOrderRepository orders;
    private final OrderService orderService;

    public CheckoutService(CartService cartService, ProductRepository products, PurchaseOrderRepository orders,
            OrderService orderService) {
        this.cartService = cartService;
        this.products = products;
        this.orders = orders;
        this.orderService = orderService;
    }

    public OrderDto checkout(String customerId, String shippingAddress) {
        CartDto cart = cartService.view(customerId);
        if (cart.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        for (CartDto.Line line : cart.items()) {
            Product p = products.findById(line.productId()).orElseThrow();
            if (line.quantity() > p.getStock()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Only " + p.getStock() + " of " + p.getName() + " left in stock");
            }
            p.setStock(p.getStock() - line.quantity());
            products.save(p);
        }

        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        List<OrderLine> lines = cart.items().stream()
                .map(l -> new OrderLine(l.productId(), l.name(), l.quantity(), l.unitPrice()))
                .toList();
        PurchaseOrder order = orders.save(
                new PurchaseOrder(orderId, customerId, cart.total(), shippingAddress.trim(), lines));
        cartService.clear(customerId);

        List<String> itemNames = cart.items().stream()
                .map(l -> l.quantity() > 1 ? l.name() + " x" + l.quantity() : l.name())
                .toList();
        orderService.placeOrder(new OrderPayload(orderId, customerId, cart.total(), itemNames,
                order.getShippingAddress()));
        return OrderDto.from(order);
    }

    public List<OrderDto> list(String customerId) {
        return orders.findByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(OrderDto::from).toList();
    }

    /** Demo hook: ships one of the customer's orders (fires ORDER_SHIPPED). */
    public OrderDto simulateShipment(String customerId, String orderId) {
        PurchaseOrder order = orders.findById(orderId)
                .filter(o -> o.getCustomerId().equals(customerId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (order.getStatus() != PurchaseOrder.Status.PLACED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is already " + order.getStatus());
        }
        String tracking = "1Z" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
        orderService.shipOrder(new ShipmentPayload(orderId, tracking, "UPS", LocalDate.now().plusDays(4).toString()));
        return OrderDto.from(orders.findById(orderId).orElseThrow());
    }
}
