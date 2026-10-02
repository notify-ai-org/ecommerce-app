package com.notify.ecommerce.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AddToCartRequest(@NotBlank String productId, @Min(1) @Max(99) int quantity) {
}
