package com.notify.ecommerce.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class PurchaseOrder {

    public enum Status { PLACED, PAYMENT_FAILED, SHIPPED }

    @Id
    private String orderId;

    private String customerId;
    private double amount;
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String carrier;
    private String trackingNumber;
    private String estimatedDelivery;
    private Instant createdAt;

    // Eager: subject suppliers and the order page always need the lines, and
    // open-in-view is disabled.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_lines", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderLine> lines = new ArrayList<>();

    protected PurchaseOrder() {}

    public PurchaseOrder(String orderId, String customerId, double amount, String shippingAddress, List<OrderLine> lines) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.shippingAddress = shippingAddress;
        this.lines = new ArrayList<>(lines);
        this.status = Status.PLACED;
        this.createdAt = Instant.now();
    }

    public void markShipped(String carrier, String trackingNumber, String estimatedDelivery) {
        this.status = Status.SHIPPED;
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.estimatedDelivery = estimatedDelivery;
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public double getAmount() { return amount; }
    public String getShippingAddress() { return shippingAddress; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getCarrier() { return carrier; }
    public String getTrackingNumber() { return trackingNumber; }
    public String getEstimatedDelivery() { return estimatedDelivery; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OrderLine> getLines() { return lines; }
}
