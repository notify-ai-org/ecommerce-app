package com.notify.ecommerce.service;

import com.notify.ecommerce.dto.CartDto;
import com.notify.ecommerce.entity.CartItem;
import com.notify.ecommerce.entity.Product;
import com.notify.ecommerce.events.CartEvents;
import com.notify.ecommerce.model.AddToCartPayload;
import com.notify.ecommerce.model.CartPayload;
import com.notify.ecommerce.repository.CartItemRepository;
import com.notify.ecommerce.repository.ProductRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CartService {

    static final int MAX_QUANTITY = 99;

    private final CartItemRepository cartItems;
    private final ProductRepository products;
    private final CatalogService catalog;
    private final CartEvents cartEvents;
    private final OrderService orderService;

    public CartService(CartItemRepository cartItems, ProductRepository products, CatalogService catalog,
            CartEvents cartEvents, OrderService orderService) {
        this.cartItems = cartItems;
        this.products = products;
        this.catalog = catalog;
        this.cartEvents = cartEvents;
        this.orderService = orderService;
    }

    public static String cartId(String customerId) {
        return "CART-" + customerId;
    }

    public CartDto view(String customerId) {
        List<CartItem> items = cartItems.findByCustomerIdOrderByAddedAtAsc(customerId);
        Map<String, Product> byId = products.findAllById(items.stream().map(CartItem::getProductId).toList())
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        List<CartDto.Line> lines = items.stream()
                .filter(i -> byId.containsKey(i.getProductId()))
                .map(i -> {
                    Product p = byId.get(i.getProductId());
                    return new CartDto.Line(p.getId(), p.getName(), p.getCategory(), p.getPrice(),
                            p.getListPrice(), i.getQuantity(), Money.round2(p.getPrice() * i.getQuantity()));
                })
                .toList();
        int count = lines.stream().mapToInt(CartDto.Line::quantity).sum();
        double total = Money.round2(lines.stream().mapToDouble(CartDto.Line::lineTotal).sum());
        return new CartDto(cartId(customerId), lines, count, total);
    }

    /** Adds (or increments) a line and fires ADD_TO_CART. */
    public CartDto add(String customerId, String productId, int quantity) {
        Product p = catalog.require(productId);
        CartItem item = cartItems.findByCustomerIdAndProductId(customerId, productId)
                .orElseGet(() -> new CartItem(customerId, productId, 0));
        int newQuantity = Math.min(MAX_QUANTITY, item.getQuantity() + quantity);
        if (newQuantity > p.getStock()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only " + p.getStock() + " in stock");
        }
        item.setQuantity(newQuantity);
        cartItems.save(item);

        CartDto cart = view(customerId);
        cartEvents.addedToCart(new AddToCartPayload(cart.cartId(), customerId, p.getId(), p.getName(), quantity,
                p.getPrice(), cart.itemCount(), cart.total(), Instant.now().toString()));
        return cart;
    }

    public CartDto updateQuantity(String customerId, String productId, int quantity) {
        if (quantity <= 0) {
            return remove(customerId, productId);
        }
        Product p = catalog.require(productId);
        if (quantity > p.getStock()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only " + p.getStock() + " in stock");
        }
        CartItem item = cartItems.findByCustomerIdAndProductId(customerId, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not in cart"));
        item.setQuantity(quantity);
        cartItems.save(item);
        return view(customerId);
    }

    public CartDto remove(String customerId, String productId) {
        cartItems.deleteByCustomerIdAndProductId(customerId, productId);
        return view(customerId);
    }

    public void clear(String customerId) {
        cartItems.deleteByCustomerId(customerId);
    }

    /** Demo hook: reports the current cart as abandoned (fires ABANDONED_CART). */
    public CartPayload abandon(String customerId) {
        List<CartItem> items = cartItems.findByCustomerIdOrderByAddedAtAsc(customerId);
        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }
        CartDto cart = view(customerId);
        String lastActivity = items.stream().map(CartItem::getUpdatedAt).max(Comparator.naturalOrder())
                .orElseGet(Instant::now).toString();
        return orderService.abandonCart(new CartPayload(cart.cartId(), customerId,
                cart.items().stream().map(CartDto.Line::name).toList(), lastActivity));
    }
}
