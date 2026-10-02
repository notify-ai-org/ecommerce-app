package com.notify.ecommerce.dto;

import com.notify.ecommerce.model.PriceDropPayload;

/** {@code priceDrop} is null when the new price is not lower than the old one. */
public record PriceChangeResponse(ProductDto product, PriceDropPayload priceDrop) {
}
