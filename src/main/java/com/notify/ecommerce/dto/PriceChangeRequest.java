package com.notify.ecommerce.dto;

import jakarta.validation.constraints.Positive;

public record PriceChangeRequest(@Positive(message = "Price must be greater than zero") double newPrice) {
}
