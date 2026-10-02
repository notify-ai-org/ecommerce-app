package com.notify.ecommerce.service;

import com.notify.ecommerce.dto.PriceChangeResponse;
import com.notify.ecommerce.dto.ProductDto;
import com.notify.ecommerce.entity.Product;
import com.notify.ecommerce.entity.ProductView;
import com.notify.ecommerce.events.ProductEvents;
import com.notify.ecommerce.model.PriceDropPayload;
import com.notify.ecommerce.model.ProductViewedPayload;
import com.notify.ecommerce.repository.ProductRepository;
import com.notify.ecommerce.repository.ProductViewRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class CatalogService {

    private final ProductRepository products;
    private final ProductViewRepository views;
    private final ProductEvents productEvents;

    public CatalogService(ProductRepository products, ProductViewRepository views, ProductEvents productEvents) {
        this.products = products;
        this.views = views;
        this.productEvents = productEvents;
    }

    public List<ProductDto> list() {
        return products.findAllByOrderByCategoryAscNameAsc().stream().map(ProductDto::from).toList();
    }

    public Product require(String productId) {
        return products.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    /** Records the view in browsing history and fires PRODUCT_VIEWED. */
    public ProductViewedPayload recordView(String customerId, String productId) {
        Product p = require(productId);
        views.save(new ProductView(customerId, productId));
        long viewCount = views.countByCustomerIdAndProductId(customerId, productId);
        return productEvents.productViewed(new ProductViewedPayload(customerId, p.getId(), p.getName(),
                p.getCategory(), p.getPrice(), viewCount, Instant.now().toString()));
    }

    /** Updates the price; fires PRICE_DROP only when the new price is lower. */
    public PriceChangeResponse changePrice(String productId, double requestedPrice) {
        Product p = require(productId);
        double previous = p.getPrice();
        double newPrice = Money.round2(requestedPrice);

        p.setPrice(newPrice);
        p.setListPrice(Math.max(p.getListPrice(), newPrice));
        p = products.save(p);

        PriceDropPayload drop = null;
        if (newPrice < previous) {
            double dropPercent = Math.round((previous - newPrice) / previous * 1000.0) / 10.0;
            drop = productEvents.priceDropped(new PriceDropPayload(p.getId(), p.getName(), p.getCategory(),
                    previous, newPrice, dropPercent, Instant.now().toString()));
        }
        return new PriceChangeResponse(ProductDto.from(p), drop);
    }
}
