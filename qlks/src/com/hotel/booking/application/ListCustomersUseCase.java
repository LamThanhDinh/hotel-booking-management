package com.hotel.booking.application;

import com.hotel.booking.domain.Customer;

import java.util.List;
import java.util.stream.Collectors;

public class ListCustomersUseCase {
    private final CustomerRepository customerRepository;

    public ListCustomersUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerDTO> execute() {
        return customerRepository.findAll()
                .stream()
                .map(c -> new CustomerDTO(
                        c.getCustomerId(),
                        c.getName(),
                        c.getPhone(),
                        c.getIdentityNo()
                ))
                .collect(Collectors.toList());
    }
}
