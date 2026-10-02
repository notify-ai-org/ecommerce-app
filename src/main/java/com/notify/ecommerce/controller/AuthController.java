package com.notify.ecommerce.controller;

import com.notify.ecommerce.dto.CustomerDto;
import com.notify.ecommerce.dto.LoginRequest;
import com.notify.ecommerce.dto.LoginResponse;
import com.notify.ecommerce.service.AuthService;
import com.notify.ecommerce.web.SessionAuth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Storefront sign-in / sign-out. Login fires USER_LOGIN. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        AuthService.LoginResult result = authService.login(request);
        SessionAuth.signIn(http, result.customer().getId());
        return new LoginResponse(CustomerDto.from(result.customer()), result.firstLogin());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest http) {
        SessionAuth.signOut(http);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public CustomerDto me(HttpServletRequest http) {
        return CustomerDto.from(authService.get(SessionAuth.requireCustomerId(http)));
    }
}
