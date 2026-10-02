package com.notify.ecommerce.events;

import com.notify.agent.client.models.subject.EmailSubject;
import com.notify.agent.client.models.subject.SmsSubject;
import com.notify.agent.client.models.subject.Subject;
import com.notify.ecommerce.entity.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds notification subjects from a customer's profile (name, email, mobile
 * number). Every subject carries the same personalisation attributes so
 * templates can use {@code firstName}, {@code fullName} and {@code customerId}.
 */
public final class CustomerSubjects {

    private CustomerSubjects() {}

    public static Map<String, String> attributes(Customer c) {
        return Map.of(
                "firstName", c.getFirstName(),
                "fullName", c.getName(),
                "customerId", c.getId());
    }

    public static EmailSubject email(Customer c) {
        return new EmailSubject(c.getEmail(), null, null, null, attributes(c));
    }

    /** Email always; SMS too when the customer has a mobile number on file. */
    public static List<Subject> emailAndSms(Customer c) {
        List<Subject> subjects = new ArrayList<>();
        subjects.add(email(c));
        if (hasPhone(c)) {
            subjects.add(new SmsSubject(c.getPhone(), null, attributes(c)));
        }
        return subjects;
    }

    public static boolean hasPhone(Customer c) {
        return c.getPhone() != null && !c.getPhone().isBlank();
    }
}
