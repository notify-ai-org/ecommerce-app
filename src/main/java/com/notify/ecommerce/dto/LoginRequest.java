package com.notify.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Storefront sign-in form. Signing in with an unknown email creates the
 * account; a known email must match its password.
 */
public record LoginRequest(
        @NotBlank(message = "Name is required") @Size(max = 100) String name,
        @NotBlank(message = "Email is required") @Email(message = "Enter a valid email address") String email,
        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^\\+?[0-9][0-9 ()-]{6,19}$", message = "Enter a valid mobile number") String phone,
        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be at least 6 characters") String password) {
}
