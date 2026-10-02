package com.notify.ecommerce.controller;

import com.notify.ecommerce.dto.PriceChangeRequest;
import com.notify.ecommerce.dto.PriceChangeResponse;
import com.notify.ecommerce.dto.ProductDto;
import com.notify.ecommerce.model.ProductViewedPayload;
import com.notify.ecommerce.service.CatalogService;
import com.notify.ecommerce.web.SessionAuth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CatalogService catalog;

    public ProductController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public List<ProductDto> list() {
        return catalog.list();
    }

    @GetMapping("/{productId}")
    public ProductDto get(@PathVariable String productId) {
        return ProductDto.from(catalog.require(productId));
    }

    /** Fires PRODUCT_VIEWED for the signed-in customer. */
    @PostMapping("/{productId}/view")
    public ProductViewedPayload view(@PathVariable String productId, HttpServletRequest http) {
        return catalog.recordView(SessionAuth.requireCustomerId(http), productId);
    }

    /**
     * Merchandising/test hook (unauthenticated, like {@code /api/orders/*}):
     * a lower price fires PRICE_DROP.
     */
    @PutMapping("/{productId}/price")
    public PriceChangeResponse changePrice(@PathVariable String productId,
            @Valid @RequestBody PriceChangeRequest request) {
        return catalog.changePrice(productId, request.newPrice());
    }
}
