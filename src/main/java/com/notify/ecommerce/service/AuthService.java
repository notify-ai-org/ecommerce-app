package com.notify.ecommerce.service;

import com.notify.ecommerce.dto.LoginRequest;
import com.notify.ecommerce.entity.Customer;
import com.notify.ecommerce.events.UserEvents;
import com.notify.ecommerce.model.UserLoginPayload;
import com.notify.ecommerce.repository.CustomerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/**
 * Sign-in-or-register: an unknown email creates the account, a known email must
 * match its password. Either way fires USER_LOGIN.
 */
@Service
public class AuthService {

    public record LoginResult(Customer customer, boolean firstLogin) {}

    private final CustomerRepository customers;
    private final PasswordEncoder passwordEncoder;
    private final UserEvents userEvents;

    public AuthService(CustomerRepository customers, PasswordEncoder passwordEncoder, UserEvents userEvents) {
        this.customers = customers;
        this.passwordEncoder = passwordEncoder;
        this.userEvents = userEvents;
    }

    public LoginResult login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String name = request.name().trim();
        String phone = request.phone().trim();

        Customer customer = customers.findByEmailIgnoreCase(email).orElse(null);
        boolean firstLogin = customer == null;
        if (firstLogin) {
            String id = "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            customer = new Customer(id, name, email, phone, passwordEncoder.encode(request.password()));
        } else {
            if (!passwordEncoder.matches(request.password(), customer.getPasswordHash())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password for this email");
            }
            // Keep the profile current with what the customer just entered.
            customer.setName(name);
            customer.setPhone(phone);
        }
        Instant now = Instant.now();
        customer.setLastLoginAt(now);
        customer = customers.save(customer);

        userEvents.userLoggedIn(new UserLoginPayload(customer.getId(), customer.getName(), customer.getEmail(),
                customer.getPhone(), firstLogin, now.toString()));
        return new LoginResult(customer, firstLogin);
    }

    public Customer get(String customerId) {
        return customers.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in"));
    }
}
