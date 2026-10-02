package com.notify.ecommerce.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** Quantity 0 removes the line. */
public record UpdateQuantityRequest(@Min(0) @Max(99) int quantity) {
}
