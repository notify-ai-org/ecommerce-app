package com.notify.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
        @NotBlank(message = "Shipping address is required") @Size(max = 500) String shippingAddress) {
}
