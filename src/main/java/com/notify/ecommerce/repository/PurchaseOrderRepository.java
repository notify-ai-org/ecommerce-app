package com.notify.ecommerce.repository;

import com.notify.ecommerce.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {

    List<PurchaseOrder> findByCustomerIdOrderByCreatedAtDesc(String customerId);
}
