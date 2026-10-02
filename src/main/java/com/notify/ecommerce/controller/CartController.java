package com.notify.ecommerce.controller;

import com.notify.ecommerce.dto.AddToCartRequest;
import com.notify.ecommerce.dto.CartDto;
import com.notify.ecommerce.dto.UpdateQuantityRequest;
import com.notify.ecommerce.service.CartService;
import com.notify.ecommerce.web.SessionAuth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** The signed-in customer's cart. Adding an item fires ADD_TO_CART. */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartDto get(HttpServletRequest http) {
        return cartService.view(SessionAuth.requireCustomerId(http));
    }

    @PostMapping("/items")
    public CartDto add(@Valid @RequestBody AddToCartRequest request, HttpServletRequest http) {
        return cartService.add(SessionAuth.requireCustomerId(http), request.productId(), request.quantity());
    }

    @PatchMapping("/items/{productId}")
    public CartDto update(@PathVariable String productId, @Valid @RequestBody UpdateQuantityRequest request,
            HttpServletRequest http) {
        return cartService.updateQuantity(SessionAuth.requireCustomerId(http), productId, request.quantity());
    }

    @DeleteMapping("/items/{productId}")
    public CartDto remove(@PathVariable String productId, HttpServletRequest http) {
        return cartService.remove(SessionAuth.requireCustomerId(http), productId);
    }

    /** Demo hook: reports the current cart as abandoned (fires ABANDONED_CART). */
    @PostMapping("/abandon")
    public ResponseEntity<Map<String, Object>> abandon(HttpServletRequest http) {
        var payload = cartService.abandon(SessionAuth.requireCustomerId(http));
        return ResponseEntity.ok(Map.of("status", "ABANDONED_CART", "cartId", payload.getCartId()));
    }
}
