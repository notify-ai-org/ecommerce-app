package com.notify.ecommerce.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notify.ecommerce.dto.NotificationDto;
import com.notify.ecommerce.dto.NotificationListDto;
import com.notify.ecommerce.entity.InAppNotification;
import com.notify.ecommerce.repository.CustomerRepository;
import com.notify.ecommerce.repository.InAppNotificationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Stores the IN_APP notifications Notify delivers to this app's webhook and serves them to the
 * signed-in customer.
 */
@Service
public class InAppNotificationService {

    private static final Logger log = LoggerFactory.getLogger(InAppNotificationService.class);
    private static final int MAX_MESSAGE_LENGTH = 4000;

    private final InAppNotificationRepository notifications;
    private final CustomerRepository customers;
    private final ObjectMapper json;
    private final String signingSecret;

    public InAppNotificationService(InAppNotificationRepository notifications, CustomerRepository customers,
            ObjectMapper json, @Value("${ecommerce.in-app.signing-secret:}") String signingSecret) {
        this.notifications = notifications;
        this.customers = customers;
        this.json = json;
        this.signingSecret = signingSecret;
    }

    /**
     * Accepts one delivery from Notify's webhook connector. The body is
     * {@code {"id", "timestamp", "payload", "channel": "IN_APP", "tenantId", "recipientId"}} and
     * {@code X-Signature} is {@code sha256=<hex HMAC-SHA256 of the raw body>}.
     */
    @Transactional
    public void receive(String rawBody, String signature) {
        verifySignature(rawBody, signature);

        JsonNode envelope;
        try {
            envelope = json.readTree(rawBody);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body is not valid JSON");
        }
        String id = envelope.path("id").asText("");
        String customerId = envelope.path("recipientId").asText("");
        String message = envelope.path("payload").asText("").strip();
        if (!"IN_APP".equals(envelope.path("channel").asText()) || id.isBlank() || customerId.isBlank()
                || message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Expected an IN_APP delivery with id, recipientId and payload");
        }
        if (!customers.existsById(customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown recipient");
        }
        if (notifications.existsById(id)) {
            return; // Redelivery of a notification that is already stored.
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            message = message.substring(0, MAX_MESSAGE_LENGTH);
        }
        notifications.save(new InAppNotification(id, customerId, message));
        log.info("🔔 In-app notification {} stored for {}", id, customerId);
    }

    @Transactional(readOnly = true)
    public NotificationListDto list(String customerId) {
        return new NotificationListDto(
                notifications.findTop50ByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                        .map(NotificationDto::from).toList(),
                notifications.countByCustomerIdAndReadAtIsNull(customerId));
    }

    @Transactional
    public void markAllRead(String customerId) {
        notifications.findByCustomerIdAndReadAtIsNull(customerId).forEach(InAppNotification::markRead);
    }

    private void verifySignature(String rawBody, String signature) {
        if (signingSecret.isBlank()) {
            // Fail closed: without a secret anyone could post notifications into a customer's panel.
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "In-app notifications are not configured (ecommerce.in-app.signing-secret)");
        }
        byte[] expected = ("sha256=" + hmacHex(rawBody)).getBytes(StandardCharsets.UTF_8);
        byte[] actual = (signature == null ? "" : signature.trim()).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid signature");
        }
    }

    private String hmacHex(String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 is unavailable", e);
        }
    }
}
