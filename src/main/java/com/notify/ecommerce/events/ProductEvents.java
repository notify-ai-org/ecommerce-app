package com.notify.ecommerce.events;

import com.notify.agent.annotations.Event;
import com.notify.agent.annotations.Rule;
import com.notify.agent.annotations.SubjectSupplier;
import com.notify.agent.client.models.subject.Subject;
import com.notify.ecommerce.entity.Customer;
import com.notify.ecommerce.model.PriceDropPayload;
import com.notify.ecommerce.model.ProductViewedPayload;
import com.notify.ecommerce.repository.CartItemRepository;
import com.notify.ecommerce.repository.CustomerRepository;
import com.notify.ecommerce.repository.ProductViewRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Catalog events.
 *
 * - PRODUCT_VIEWED (deferred email nudge, only on repeat interest)
 * - PRICE_DROP (email to everyone who viewed or carted the product; SMS too for big drops)
 */
@Component
public class ProductEvents {

    private static final Logger log = LoggerFactory.getLogger(ProductEvents.class);

    /** Drops at or above this percentage also go out over SMS. */
    static final double SMS_DROP_THRESHOLD_PERCENT = 20.0;

    private final CustomerRepository customers;
    private final ProductViewRepository views;
    private final CartItemRepository cartItems;

    public ProductEvents(CustomerRepository customers, ProductViewRepository views, CartItemRepository cartItems) {
        this.customers = customers;
        this.views = views;
        this.cartItems = cartItems;
    }

    // ═══════════════════════════════════════════
    // EVENTS
    // ═══════════════════════════════════════════

    @Event(key = "PRODUCT_VIEWED", description = "Customer viewed a product detail page", eventType = "deferred", scheduleIntent = "scheduled", preferredTimeWindow = "10:00-20:00", priority = 1, payload = ProductViewedPayload.class)
    public ProductViewedPayload productViewed(ProductViewedPayload payload) {
        log.info("👀 Product viewed: {} by {} (view #{})", payload.getProductId(), payload.getCustomerId(),
                payload.getViewCount());
        return payload;
    }

    @Event(key = "PRICE_DROP", description = "Price of a product was reduced", eventType = "static", scheduleIntent = "immediate", preferredTimeWindow = "09:00-21:00", priority = 3, payload = PriceDropPayload.class)
    public PriceDropPayload priceDropped(PriceDropPayload payload) {
        log.info("📉 Price drop: {} ${} → ${} (-{}%)", payload.getProductId(), payload.getPreviousPrice(),
                payload.getNewPrice(), payload.getDropPercent());
        return payload;
    }

    // ═══════════════════════════════════════════
    // SUBJECT SUPPLIERS
    // ═══════════════════════════════════════════

    @SubjectSupplier(event = "PRODUCT_VIEWED", description = "Resolves the viewing customer to an email recipient")
    public List<Subject> getProductViewedSubjects(ProductViewedPayload payload) {
        return customers.findById(payload.getCustomerId())
                .<List<Subject>>map(c -> List.of(CustomerSubjects.email(c)))
                .orElse(List.of());
    }

    @SubjectSupplier(event = "PRICE_DROP", description = "Resolves customers who viewed or carted the product; email for all, SMS added for drops of 20% or more")
    public List<Subject> getPriceDropSubjects(PriceDropPayload payload) {
        Set<String> audience = new LinkedHashSet<>(cartItems.findCustomerIdsWithProduct(payload.getProductId()));
        audience.addAll(views.findCustomerIdsWhoViewed(payload.getProductId()));

        boolean bigDrop = payload.getDropPercent() >= SMS_DROP_THRESHOLD_PERCENT;
        List<Subject> subjects = new ArrayList<>();
        for (Customer c : customers.findAllById(audience)) {
            subjects.addAll(bigDrop ? CustomerSubjects.emailAndSms(c) : List.of(CustomerSubjects.email(c)));
        }
        log.info("📉 Price drop audience for {}: {} customers, {} subjects", payload.getProductId(),
                audience.size(), subjects.size());
        return subjects;
    }

    // ═══════════════════════════════════════════
    // RULES
    // ═══════════════════════════════════════════

    @Rule(name = "repeat-interest", event = "PRODUCT_VIEWED", description = "Only nudge customers who have viewed the product at least twice")
    public boolean repeatInterest(ProductViewedPayload payload) {
        return payload.getViewCount() >= 2;
    }

    @Rule(name = "meaningful-drop", event = "PRICE_DROP", description = "Ignore price drops smaller than 5%")
    public boolean meaningfulDrop(PriceDropPayload payload) {
        return payload.getDropPercent() >= 5.0;
    }
}
