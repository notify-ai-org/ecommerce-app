package com.notify.ecommerce.controller;

import com.notify.ecommerce.dto.NotificationListDto;
import com.notify.ecommerce.service.InAppNotificationService;
import com.notify.ecommerce.web.SessionAuth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * In-app notifications. {@code /webhook} is called by Notify (authenticated by its HMAC
 * signature, not a session); the other endpoints serve the signed-in storefront customer.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final InAppNotificationService notifications;

    public NotificationController(InAppNotificationService notifications) {
        this.notifications = notifications;
    }

    /** Delivery target for the tenant's IN_APP channel; Notify treats any 2xx as delivered. */
    @PostMapping("/webhook")
    public ResponseEntity<Void> receive(@RequestBody String body,
            @RequestHeader(value = "X-Signature", required = false) String signature) {
        notifications.receive(body, signature);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public NotificationListDto list(HttpServletRequest http) {
        return notifications.list(SessionAuth.requireCustomerId(http));
    }

    @PostMapping("/read")
    public NotificationListDto markAllRead(HttpServletRequest http) {
        String customerId = SessionAuth.requireCustomerId(http);
        notifications.markAllRead(customerId);
        return notifications.list(customerId);
    }
}
