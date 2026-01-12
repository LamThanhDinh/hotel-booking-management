package com.hotel.booking.application;

import com.hotel.booking.domain.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
    Optional<Customer> findByPhoneOrIdentity(String phone, String identityNo);
    
    Optional<Customer> findById(String customerId);

    Customer save(Customer customer);

    List<Customer> findAll();
}
