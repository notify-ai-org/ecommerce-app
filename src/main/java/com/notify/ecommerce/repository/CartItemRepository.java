package com.notify.ecommerce.repository;

import com.notify.ecommerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCustomerIdOrderByAddedAtAsc(String customerId);

    Optional<CartItem> findByCustomerIdAndProductId(String customerId, String productId);

    @Query("select distinct c.customerId from CartItem c where c.productId = :productId")
    List<String> findCustomerIdsWithProduct(String productId);

    @Transactional
    void deleteByCustomerId(String customerId);

    @Transactional
    void deleteByCustomerIdAndProductId(String customerId, String productId);
}
