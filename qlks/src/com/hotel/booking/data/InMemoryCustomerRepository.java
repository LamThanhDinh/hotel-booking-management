package com.hotel.booking.data;

import com.hotel.booking.application.CustomerRepository;
import com.hotel.booking.domain.Customer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class InMemoryCustomerRepository implements CustomerRepository {
    private final List<Customer> customers = new ArrayList<>();

    public InMemoryCustomerRepository() {
        seed();
    }

    @Override
    public Optional<Customer> findByPhoneOrIdentity(String phone, String identityNo) {
        String normalizedPhone = phone == null ? "" : phone.trim();
        String normalizedIdentity = identityNo == null ? "" : identityNo.trim();
        return customers.stream()
                .filter(c -> c.getPhone().equalsIgnoreCase(normalizedPhone)
                        || (!normalizedIdentity.isEmpty() && c.getIdentityNo().equalsIgnoreCase(normalizedIdentity)))
                .findFirst();
    }

    @Override
    public Customer save(Customer customer) {
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getCustomerId().equalsIgnoreCase(customer.getCustomerId())) {
                customers.set(i, customer);
                return customer;
            }
        }
        customers.add(customer);
        return customer;
    }

    @Override
    public List<Customer> findAll() {
        return Collections.unmodifiableList(customers);
    }

    private void seed() {
        customers.add(new Customer("CUST-1001", "Nguyen Van A", "0900000001", "ID1001"));
        customers.add(new Customer("CUST-1002", "Nguyen Van B", "0900000002", "ID1002"));
        customers.add(new Customer("CUST-1003", "Tran Thi C", "0900000003", "ID1003"));
        customers.add(new Customer("CUST-1004", "Le Van D", "0900000004", "ID1004"));
        customers.add(new Customer("CUST-1005", "Pham Thi E", "0900000005", "ID1005"));
    }
}
