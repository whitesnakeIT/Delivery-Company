package com.kapusniak.tomasz.controller;

import com.kapusniak.tomasz.openapi.api.CustomersApi;
import com.kapusniak.tomasz.openapi.model.Customer;
import com.kapusniak.tomasz.service.model.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<Void> deleteCustomer(@PathVariable("uuid") UUID customerUuid) {
        customerService.delete(customerUuid);

        return ResponseEntity
                .noContent()
                .build();
    }

    @Override
    public ResponseEntity<List<Customer>> getAllCustomers(@RequestParam(defaultValue = "0") Integer pageNumber) {
        Page<Customer> customerList = customerService.findAll(pageNumber);

        return ResponseEntity.ok(customerList.getContent());
    }

    @Override
    public ResponseEntity<Customer> getCustomer(@PathVariable("uuid") UUID customerUuid) {
        Customer customer = customerService.findByUuid(customerUuid);

        return ResponseEntity.ok(customer);
    }

    @Override
    public ResponseEntity<Customer> updateCustomer(@PathVariable("uuid") UUID customerUuid, @RequestBody @Valid Customer customer) {
        Customer update = customerService.update(customerUuid, customer);

        return ResponseEntity.ok(update);
    }
}
