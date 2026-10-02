package com.notify.ecommerce.dto;

import com.notify.ecommerce.entity.OrderLine;
import com.notify.ecommerce.entity.PurchaseOrder;

import java.time.Instant;
import java.util.List;

public record OrderDto(String orderId, double amount, String status, String shippingAddress,
        String carrier, String trackingNumber, String estimatedDelivery, Instant createdAt, List<OrderLine> lines) {

    public static OrderDto from(PurchaseOrder o) {
        return new OrderDto(o.getOrderId(), o.getAmount(), o.getStatus().name(), o.getShippingAddress(),
                o.getCarrier(), o.getTrackingNumber(), o.getEstimatedDelivery(), o.getCreatedAt(),
                List.copyOf(o.getLines()));
    }
}
