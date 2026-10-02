package com.notify.ecommerce.controller;

import com.notify.ecommerce.dto.CheckoutRequest;
import com.notify.ecommerce.dto.OrderDto;
import com.notify.ecommerce.model.CartPayload;
import com.notify.ecommerce.model.OrderPayload;
import com.notify.ecommerce.model.ShipmentPayload;
import com.notify.ecommerce.service.CheckoutService;
import com.notify.ecommerce.service.OrderService;
import com.notify.ecommerce.web.SessionAuth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Order endpoints. {@code /checkout}, {@code GET /} and
 * {@code /{orderId}/simulate-shipment} serve the signed-in storefront customer;
 * the rest are raw hooks to trigger e-commerce events for testing.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final CheckoutService checkoutService;

    public OrderController(OrderService orderService, CheckoutService checkoutService) {
        this.orderService = orderService;
        this.checkoutService = checkoutService;
    }

    @GetMapping
    public List<OrderDto> myOrders(HttpServletRequest http) {
        return checkoutService.list(SessionAuth.requireCustomerId(http));
    }

    /** Places an order from the signed-in customer's cart (fires ORDER_PLACED). */
    @PostMapping("/checkout")
    public OrderDto checkout(@Valid @RequestBody CheckoutRequest request, HttpServletRequest http) {
        return checkoutService.checkout(SessionAuth.requireCustomerId(http), request.shippingAddress());
    }

    @PostMapping("/{orderId}/simulate-shipment")
    public OrderDto simulateShipment(@PathVariable String orderId, HttpServletRequest http) {
        return checkoutService.simulateShipment(SessionAuth.requireCustomerId(http), orderId);
    }

    @PostMapping("/place")
    public ResponseEntity<Map<String, Object>> placeOrder(@RequestBody OrderPayload payload) {
        OrderPayload result = orderService.placeOrder(payload);
        return ResponseEntity.ok(Map.of(
                "status", "ORDER_PLACED",
                "orderId", result.getOrderId(),
                "amount", result.getAmount()));
    }

    @PostMapping("/payment-failed")
    public ResponseEntity<Map<String, Object>> paymentFailed(@RequestBody OrderPayload payload) {
        orderService.reportPaymentFailed(payload);
        return ResponseEntity.ok(Map.of(
                "status", "PAYMENT_FAILED",
                "orderId", payload.getOrderId()));
    }

    @PostMapping("/ship")
    public ResponseEntity<Map<String, Object>> shipOrder(@RequestBody ShipmentPayload payload) {
        orderService.shipOrder(payload);
        return ResponseEntity.ok(Map.of(
                "status", "ORDER_SHIPPED",
                "orderId", payload.getOrderId(),
                "trackingNumber", payload.getTrackingNumber()));
    }

    @PostMapping("/abandon-cart")
    public ResponseEntity<Map<String, Object>> abandonCart(@RequestBody CartPayload payload) {
        orderService.abandonCart(payload);
        return ResponseEntity.ok(Map.of(
                "status", "ABANDONED_CART",
                "cartId", payload.getCartId()));
    }
}
