package com.notify.ecommerce.dto;

import com.notify.ecommerce.entity.Customer;

import java.time.Instant;

public record CustomerDto(String id, String name, String email, String phone, Instant createdAt) {

    public static CustomerDto from(Customer c) {
        return new CustomerDto(c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.getCreatedAt());
    }
}
