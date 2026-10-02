package com.notify.ecommerce.dto;

import java.util.List;

public record CartDto(String cartId, List<Line> items, int itemCount, double total) {

    public record Line(String productId, String name, String category, double unitPrice,
            double listPrice, int quantity, double lineTotal) {
    }
}
