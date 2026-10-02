package com.notify.ecommerce.repository;

import com.notify.ecommerce.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    Optional<Customer> findByEmailIgnoreCase(String email);
}
