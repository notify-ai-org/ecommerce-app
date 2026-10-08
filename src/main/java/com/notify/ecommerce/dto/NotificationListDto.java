package com.notify.ecommerce.dto;

import java.util.List;

/** The newest notifications plus the customer's total unread count (which may exceed the list). */
public record NotificationListDto(List<NotificationDto> items, long unreadCount) {}
