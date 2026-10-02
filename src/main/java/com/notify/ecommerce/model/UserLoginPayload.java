package com.notify.ecommerce.model;

import com.notify.agent.annotations.Model;
import com.notify.agent.annotations.Vocabulary;

@Model(description = "Payload for customer sign-in events")
public class UserLoginPayload {

    @Vocabulary(name = "customerId", description = "Customer who signed in")
    private String customerId;

    @Vocabulary(name = "customerName", description = "Full name of the customer")
    private String customerName;

    @Vocabulary(name = "email", description = "Email address of the customer")
    private String email;

    @Vocabulary(name = "phone", description = "Mobile number of the customer")
    private String phone;

    @Vocabulary(name = "firstLogin", description = "True when the account was created by this sign-in")
    private boolean firstLogin;

    @Vocabulary(name = "loginAt", description = "Timestamp of the sign-in (ISO-8601)")
    private String loginAt;

    public UserLoginPayload() {}

    public UserLoginPayload(String customerId, String customerName, String email, String phone,
            boolean firstLogin, String loginAt) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.email = email;
        this.phone = phone;
        this.firstLogin = firstLogin;
        this.loginAt = loginAt;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isFirstLogin() { return firstLogin; }
    public void setFirstLogin(boolean firstLogin) { this.firstLogin = firstLogin; }
    public String getLoginAt() { return loginAt; }
    public void setLoginAt(String loginAt) { this.loginAt = loginAt; }
}
