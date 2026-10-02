package com.notify.ecommerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/** Browsing history row; used for repeat-interest rules and price-drop audiences. */
@Entity
@Table(name = "product_views", indexes = @Index(columnList = "productId"))
public class ProductView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerId;
    private String productId;
    private Instant viewedAt;

    protected ProductView() {}

    public ProductView(String customerId, String productId) {
        this.customerId = customerId;
        this.productId = productId;
        this.viewedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getProductId() { return productId; }
    public Instant getViewedAt() { return viewedAt; }
}
