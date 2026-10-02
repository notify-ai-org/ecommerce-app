package com.notify.ecommerce.dto;

import com.notify.ecommerce.entity.Product;

public record ProductDto(String id, String name, String description, String category,
        double price, double listPrice, int stock) {

    public static ProductDto from(Product p) {
        return new ProductDto(p.getId(), p.getName(), p.getDescription(), p.getCategory(),
                p.getPrice(), p.getListPrice(), p.getStock());
    }
}
