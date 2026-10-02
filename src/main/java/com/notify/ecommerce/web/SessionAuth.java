package com.notify.ecommerce.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Storefront login state is the customer id held in the servlet session. */
public final class SessionAuth {

    static final String CUSTOMER_ID = "customerId";

    private SessionAuth() {}

    public static String requireCustomerId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object id = session == null ? null : session.getAttribute(CUSTOMER_ID);
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in");
        }
        return (String) id;
    }

    public static void signIn(HttpServletRequest request, String customerId) {
        // Rotate the session id on login to prevent session fixation.
        request.getSession(true);
        request.changeSessionId();
        request.getSession().setAttribute(CUSTOMER_ID, customerId);
    }

    public static void signOut(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
