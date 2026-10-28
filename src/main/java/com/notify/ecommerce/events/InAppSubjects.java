package com.notify.ecommerce.events;

import com.notify.agent.client.models.subject.InAppSubject;
import com.notify.agent.client.models.subject.Subject;
import com.notify.ecommerce.entity.Customer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds the storefront's in-app inbox as a recipient. The subject's URL is this app's public
 * webhook and must equal the endpoint of the tenant's IN_APP channel in Notify; the customer id
 * comes back as {@code recipientId} on delivery. Disabled while no webhook URL is configured.
 */
@Component
public class InAppSubjects {

    /** The given subjects plus the customer's in-app inbox, when in-app delivery is configured. */
    public List<Subject> with(Customer c, List<? extends Subject> subjects) {
        List<Subject> all = new ArrayList<>(subjects);
        all.add(new InAppSubject(null, c.getId(), null, CustomerSubjects.attributes(c)));
        return all;
    }
}
