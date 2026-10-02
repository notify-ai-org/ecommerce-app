package com.notify.ecommerce.config;

import com.notify.ecommerce.entity.Customer;
import com.notify.ecommerce.entity.Product;
import com.notify.ecommerce.repository.CustomerRepository;
import com.notify.ecommerce.repository.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds demo customers and the product catalog into an empty H2 database.
 * Demo customers sign in with password {@value #DEMO_PASSWORD}.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    static final String DEMO_PASSWORD = "password123";

    private final CustomerRepository customers;
    private final ProductRepository products;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CustomerRepository customers, ProductRepository products, PasswordEncoder passwordEncoder) {
        this.customers = customers;
        this.products = products;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (customers.count() == 0) {
            String hash = passwordEncoder.encode(DEMO_PASSWORD);
            customers.saveAll(List.of(
                    new Customer("CUST-1", "Alice Johnson", "alice@example.com", "+1-555-0101", hash),
                    new Customer("CUST-2", "Bob Smith", "rohan.nn1203@gmail.com", "+1-555-0102", hash),
                    new Customer("CUST-3", "Carol Davis", "carol@example.com", "+1-555-0103", hash)));
            log.info("Seeded demo customers");
        }
        if (products.count() == 0) {
            products.saveAll(List.of(
                    new Product("P-1001", "Mechanical Keyboard", "Hot-swappable 75% board with tactile switches and PBT keycaps.", "Electronics", 120.00, 40),
                    new Product("P-1002", "Wireless Mouse", "Ergonomic 2.4 GHz / Bluetooth mouse with 70-day battery life.", "Electronics", 49.99, 75),
                    new Product("P-1003", "Noise-Cancelling Headphones", "Over-ear ANC headphones with 30-hour playback.", "Electronics", 249.00, 25),
                    new Product("P-1004", "27\" 4K Monitor", "IPS panel, USB-C 90 W power delivery, factory calibrated.", "Electronics", 399.00, 15),
                    new Product("P-2001", "Merino Crew Sweater", "Lightweight merino wool, machine washable.", "Apparel", 89.00, 60),
                    new Product("P-2002", "Trail Running Shoes", "Grippy outsole, rock plate and breathable mesh upper.", "Apparel", 135.00, 30),
                    new Product("P-2003", "Rain Shell Jacket", "Packable 2.5-layer waterproof jacket.", "Apparel", 159.00, 20),
                    new Product("P-3001", "Pour-Over Coffee Set", "Glass dripper, carafe and 100 paper filters.", "Home", 39.50, 50),
                    new Product("P-3002", "Cast Iron Skillet", "Pre-seasoned 12\" skillet for stovetop and oven.", "Home", 54.00, 35),
                    new Product("P-3003", "Linen Bedding Set", "Stonewashed French linen duvet cover and two pillowcases.", "Home", 210.00, 12),
                    new Product("P-4001", "The Pragmatic Programmer", "20th anniversary edition, hardcover.", "Books", 42.00, 80),
                    new Product("P-4002", "Designing Data-Intensive Applications", "The big ideas behind reliable, scalable systems.", "Books", 48.00, 65)));
            log.info("Seeded product catalog");
        }
    }
}
