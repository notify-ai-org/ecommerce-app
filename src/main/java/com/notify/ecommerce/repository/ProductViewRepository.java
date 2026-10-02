package com.notify.ecommerce.repository;

import com.notify.ecommerce.entity.ProductView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductViewRepository extends JpaRepository<ProductView, Long> {

    long countByCustomerIdAndProductId(String customerId, String productId);

    @Query("select distinct v.customerId from ProductView v where v.productId = :productId")
    List<String> findCustomerIdsWhoViewed(String productId);
}
