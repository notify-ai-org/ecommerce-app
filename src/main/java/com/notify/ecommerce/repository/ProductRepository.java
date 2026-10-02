package com.notify.ecommerce.repository;

import com.notify.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findAllByOrderByCategoryAscNameAsc();
}
