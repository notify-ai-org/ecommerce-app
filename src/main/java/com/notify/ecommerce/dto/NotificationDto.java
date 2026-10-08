package com.notify.ecommerce.dto;

import com.notify.ecommerce.entity.InAppNotification;

import java.time.Instant;

public record NotificationDto(String id, String message, Instant createdAt, boolean read) {

    public static NotificationDto from(InAppNotification n) {
        return new NotificationDto(n.getId(), n.getMessage(), n.getCreatedAt(), n.getReadAt() != null);
    }
}
