package com.notify.ecommerce.repository;

import com.notify.ecommerce.entity.InAppNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InAppNotificationRepository extends JpaRepository<InAppNotification, String> {

    List<InAppNotification> findTop50ByCustomerIdOrderByCreatedAtDesc(String customerId);

    List<InAppNotification> findByCustomerIdAndReadAtIsNull(String customerId);

    long countByCustomerIdAndReadAtIsNull(String customerId);
}
