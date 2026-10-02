package com.notify.ecommerce.model;

import com.notify.agent.annotations.Model;
import com.notify.agent.annotations.Vocabulary;

@Model(description = "Payload for add-to-cart events")
public class AddToCartPayload {

    @Vocabulary(name = "cartId", description = "Cart identifier")
    private String cartId;

    @Vocabulary(name = "customerId", description = "Customer who owns the cart")
    private String customerId;

    @Vocabulary(name = "productId", description = "Product added to the cart")
    private String productId;

    @Vocabulary(name = "productName", description = "Name of the product added to the cart")
    private String productName;

    @Vocabulary(name = "quantity", description = "Quantity added in this action")
    private int quantity;

    @Vocabulary(name = "unitPrice", description = "Unit price of the product in USD")
    private double unitPrice;

    @Vocabulary(name = "cartItemCount", description = "Total number of units in the cart after this action")
    private int cartItemCount;

    @Vocabulary(name = "cartTotal", description = "Cart total in USD after this action")
    private double cartTotal;

    @Vocabulary(name = "addedAt", description = "Timestamp of the action (ISO-8601)")
    private String addedAt;

    public AddToCartPayload() {}

    public AddToCartPayload(String cartId, String customerId, String productId, String productName, int quantity,
            double unitPrice, int cartItemCount, double cartTotal, String addedAt) {
        this.cartId = cartId;
        this.customerId = customerId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.cartItemCount = cartItemCount;
        this.cartTotal = cartTotal;
        this.addedAt = addedAt;
    }

    public String getCartId() { return cartId; }
    public void setCartId(String cartId) { this.cartId = cartId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public int getCartItemCount() { return cartItemCount; }
    public void setCartItemCount(int cartItemCount) { this.cartItemCount = cartItemCount; }
    public double getCartTotal() { return cartTotal; }
    public void setCartTotal(double cartTotal) { this.cartTotal = cartTotal; }
    public String getAddedAt() { return addedAt; }
    public void setAddedAt(String addedAt) { this.addedAt = addedAt; }
}
