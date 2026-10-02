package com.notify.ecommerce.events;

import com.notify.agent.annotations.Event;
import com.notify.agent.annotations.SubjectSupplier;
import com.notify.agent.client.models.subject.Subject;
import com.notify.ecommerce.model.UserLoginPayload;
import com.notify.ecommerce.repository.CustomerRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Account events. USER_LOGIN goes to email and SMS: a welcome message for new
 * accounts, a sign-in security alert for returning customers.
 */
@Component
public class UserEvents {

    private static final Logger log = LoggerFactory.getLogger(UserEvents.class);

    private final CustomerRepository customers;

    public UserEvents(CustomerRepository customers) {
        this.customers = customers;
    }

    @Event(key = "USER_LOGIN", description = "Customer signed in to the store (firstLogin=true for new accounts)", eventType = "static", scheduleIntent = "immediate", preferredTimeWindow = "00:00-23:59", priority = 3, payload = UserLoginPayload.class)
    public UserLoginPayload userLoggedIn(UserLoginPayload payload) {
        log.info("🔐 User login: {} ({}) firstLogin={}", payload.getCustomerId(), payload.getEmail(),
                payload.isFirstLogin());
        return payload;
    }

    @SubjectSupplier(event = "USER_LOGIN", description = "Resolves the signed-in customer to email and SMS recipients")
    public List<Subject> getLoginSubjects(UserLoginPayload payload) {
        return customers.findById(payload.getCustomerId())
                .map(CustomerSubjects::emailAndSms)
                .orElse(List.of());
    }
}
