package com.notify.ecommerce.entity;

import jakarta.persistence.Embeddable;

/**
 * Line item snapshot taken at checkout. {@code productId} and {@code unitPrice}
 * are null for orders created through the raw {@code /api/orders/place} test
 * endpoint, which only carries item names.
 */
@Embeddable
public class OrderLine {

    private String productId;
    private String productName;
    private int quantity;
    private Double unitPrice;

    protected OrderLine() {}

    public OrderLine(String productId, String productName, int quantity, Double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public Double getUnitPrice() { return unitPrice; }
}
