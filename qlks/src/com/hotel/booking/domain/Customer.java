package com.hotel.booking.domain;

import java.util.Objects;

public class Customer {
    private final String customerId;
    private final String name;
    private final String phone;
    private final String identityNo;

    public Customer(String customerId, String name, String phone, String identityNo) {
        this.customerId = Objects.requireNonNull(customerId, "customerId");
        this.name = Objects.requireNonNull(name, "name");
        this.phone = Objects.requireNonNull(phone, "phone");
        this.identityNo = identityNo == null ? "" : identityNo;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getIdentityNo() {
        return identityNo;
    }
}
