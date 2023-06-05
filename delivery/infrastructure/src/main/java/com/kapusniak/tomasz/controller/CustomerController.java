package com.kapusniak.tomasz.controller;

import com.kapusniak.tomasz.openapi.api.CustomersApi;
import com.kapusniak.tomasz.openapi.model.Customer;
import com.kapusniak.tomasz.service.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class CustomerController implements CustomersApi {

    private final CustomerService customerService;

    @Override
    public ResponseEntity<Customer> createCustomer(@RequestBody @Valid Customer customer) {
        Customer save = customerService.save(customer);

        return ResponseEntity.status(201).body(save);
    }

    @Override
    public ResponseEntity<Void> deleteCustomer(@PathVariable("id") @Min(1) Long customerId) {
        customerService.delete(customerId);

        return ResponseEntity
                .noContent()
                .build();
    }

    @Override
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customerList = customerService.findAll();

        return ResponseEntity.ok(customerList);
    }

    @Override
    public ResponseEntity<Customer> getCustomer(@PathVariable("id") @Min(1) Long customerId) {
        Customer customer = customerService.findById(customerId);

        return ResponseEntity.ok(customer);
    }

    @Override
    public ResponseEntity<Customer> updateCustomer(@PathVariable("id") @Min(1) Long customerId, @RequestBody @Valid Customer customer) {
        Customer update = customerService.update(customerId, customer);

        return ResponseEntity.ok(update);
    }
}
