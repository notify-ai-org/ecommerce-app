package com.notify.ecommerce.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Catalog product. {@code listPrice} is the original (highest) price, so the UI
 * can show a strike-through after a price drop.
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private String category;
    private double price;
    private double listPrice;
    private int stock;

    protected Product() {}

    public Product(String id, String name, String description, String category, double price, int stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.listPrice = price;
        this.stock = stock;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getListPrice() { return listPrice; }
    public void setListPrice(double listPrice) { this.listPrice = listPrice; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
