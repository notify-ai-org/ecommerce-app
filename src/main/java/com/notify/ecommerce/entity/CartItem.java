package com.notify.ecommerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/** One product line in a customer's cart. A customer has exactly one cart. */
@Entity
@Table(name = "cart_items", uniqueConstraints = @UniqueConstraint(columnNames = { "customerId", "productId" }))
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerId;
    private String productId;
    private int quantity;
    private Instant addedAt;
    private Instant updatedAt;

    protected CartItem() {}

    public CartItem(String customerId, String productId, int quantity) {
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.addedAt = Instant.now();
        this.updatedAt = this.addedAt;
    }

    public Long getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.updatedAt = Instant.now();
    }
    public Instant getAddedAt() { return addedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
