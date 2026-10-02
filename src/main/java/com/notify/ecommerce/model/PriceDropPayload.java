package com.notify.ecommerce.model;

import com.notify.agent.annotations.Model;
import com.notify.agent.annotations.Vocabulary;

@Model(description = "Payload for product price drop events")
public class PriceDropPayload {

    @Vocabulary(name = "productId", description = "Product whose price dropped")
    private String productId;

    @Vocabulary(name = "productName", description = "Name of the product")
    private String productName;

    @Vocabulary(name = "category", description = "Product category")
    private String category;

    @Vocabulary(name = "previousPrice", description = "Price before the drop in USD")
    private double previousPrice;

    @Vocabulary(name = "newPrice", description = "Price after the drop in USD")
    private double newPrice;

    @Vocabulary(name = "dropPercent", description = "Percentage reduction, rounded to one decimal place")
    private double dropPercent;

    @Vocabulary(name = "changedAt", description = "Timestamp of the price change (ISO-8601)")
    private String changedAt;

    public PriceDropPayload() {}

    public PriceDropPayload(String productId, String productName, String category, double previousPrice,
            double newPrice, double dropPercent, String changedAt) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.previousPrice = previousPrice;
        this.newPrice = newPrice;
        this.dropPercent = dropPercent;
        this.changedAt = changedAt;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPreviousPrice() { return previousPrice; }
    public void setPreviousPrice(double previousPrice) { this.previousPrice = previousPrice; }
    public double getNewPrice() { return newPrice; }
    public void setNewPrice(double newPrice) { this.newPrice = newPrice; }
    public double getDropPercent() { return dropPercent; }
    public void setDropPercent(double dropPercent) { this.dropPercent = dropPercent; }
    public String getChangedAt() { return changedAt; }
    public void setChangedAt(String changedAt) { this.changedAt = changedAt; }
}
