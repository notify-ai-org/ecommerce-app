package com.notify.ecommerce.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/** An IN_APP notification delivered by Notify for one customer; shown in the storefront's bell panel. */
@Entity
@Table(name = "in_app_notifications", indexes = @Index(columnList = "customerId, createdAt"))
public class InAppNotification {

    /** Notify's notification id, so a redelivered webhook does not create a duplicate. */
    @Id
    private String id;

    private String customerId;

    @Column(length = 4000)
    private String message;

    private Instant createdAt;
    private Instant readAt;

    protected InAppNotification() {}

    public InAppNotification(String id, String customerId, String message) {
        this.id = id;
        this.customerId = customerId;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public void markRead() {
        if (readAt == null) {
            readAt = Instant.now();
        }
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReadAt() { return readAt; }
}
