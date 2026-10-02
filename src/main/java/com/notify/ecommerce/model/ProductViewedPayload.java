package com.notify.ecommerce.model;

import com.notify.agent.annotations.Model;
import com.notify.agent.annotations.Vocabulary;

@Model(description = "Payload for product detail view events")
public class ProductViewedPayload {

    @Vocabulary(name = "customerId", description = "Customer who viewed the product")
    private String customerId;

    @Vocabulary(name = "productId", description = "Viewed product identifier")
    private String productId;

    @Vocabulary(name = "productName", description = "Viewed product name")
    private String productName;

    @Vocabulary(name = "category", description = "Product category")
    private String category;

    @Vocabulary(name = "price", description = "Current product price in USD")
    private double price;

    @Vocabulary(name = "viewCount", description = "How many times this customer has viewed the product, including this view")
    private long viewCount;

    @Vocabulary(name = "viewedAt", description = "Timestamp of the view (ISO-8601)")
    private String viewedAt;

    public ProductViewedPayload() {}

    public ProductViewedPayload(String customerId, String productId, String productName, String category,
            double price, long viewCount, String viewedAt) {
        this.customerId = customerId;
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.viewCount = viewCount;
        this.viewedAt = viewedAt;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public long getViewCount() { return viewCount; }
    public void setViewCount(long viewCount) { this.viewCount = viewCount; }
    public String getViewedAt() { return viewedAt; }
    public void setViewedAt(String viewedAt) { this.viewedAt = viewedAt; }
}
