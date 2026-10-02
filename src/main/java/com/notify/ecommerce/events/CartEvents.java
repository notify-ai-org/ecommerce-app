package com.notify.ecommerce.events;

import com.notify.agent.annotations.Event;
import com.notify.agent.annotations.SubjectSupplier;
import com.notify.agent.client.models.subject.Subject;
import com.notify.ecommerce.model.AddToCartPayload;
import com.notify.ecommerce.repository.CustomerRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/** Cart events. ABANDONED_CART stays on {@code OrderService}. */
@Component
public class CartEvents {

    private static final Logger log = LoggerFactory.getLogger(CartEvents.class);

    private final CustomerRepository customers;

    public CartEvents(CustomerRepository customers) {
        this.customers = customers;
    }

    @Event(key = "ADD_TO_CART", description = "Customer added a product to their cart", eventType = "static", scheduleIntent = "immediate", preferredTimeWindow = "09:00-21:00", priority = 2, payload = AddToCartPayload.class)
    public AddToCartPayload addedToCart(AddToCartPayload payload) {
        log.info("🛒 Added to cart: {} x{} by {} — cart total ${}", payload.getProductId(), payload.getQuantity(),
                payload.getCustomerId(), payload.getCartTotal());
        return payload;
    }

    @SubjectSupplier(event = "ADD_TO_CART", description = "Resolves the cart owner to an email recipient")
    public List<Subject> getAddToCartSubjects(AddToCartPayload payload) {
        return customers.findById(payload.getCustomerId())
                .<List<Subject>>map(c -> List.of(CustomerSubjects.email(c)))
                .orElse(List.of());
    }
}
