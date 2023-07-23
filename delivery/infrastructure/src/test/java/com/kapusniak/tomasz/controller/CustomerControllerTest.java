package com.kapusniak.tomasz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapusniak.tomasz.openapi.model.Customer;
import com.kapusniak.tomasz.service.model.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CustomerControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    public Customer getCustomer() {
        Customer customer1 = new Customer();
        customer1.setId(1L);
        customer1.setFirstName("John");
        customer1.setLastName("Wick");
        customer1.setUuid(UUID.randomUUID());
        customer1.setEmail("john@o2.pl");

        return customer1;
    }

    public List<Customer> getCustomerList() {
        List<Customer> customers = new ArrayList<>();
        Customer customer1 = new Customer();
        customer1.setId(1L);
        customer1.setFirstName("John");

        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setFirstName("Tom");
        customers.add(customer1);
        customers.add(customer2);

        return customers;
    }

    @Test
    @DisplayName("should correctly return list of customers")
    public void getAllCustomers() throws Exception {
        // given
        List<Customer> customers = getCustomerList();
        when(customerService.findAll())
                .thenReturn(customers);

        // when
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].firstName").value("Tom"));

        // then
        verify(customerService,
                times(1))
                .findAll();
        verifyNoMoreInteractions(customerService);
    }

    @Test
    @DisplayName("should correctly return customer based on customer uuid")
    public void getCustomerByUuid() throws Exception {
        // given
        Customer customer = getCustomer();
        UUID customerUuid = customer.getUuid();

        when(customerService.findByUuid(customerUuid))
                .thenReturn(customer);

        // when
        mockMvc.perform(get("/api/v1/customers/" + customerUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));

        // then
        verify(customerService,
                times(1))
                .findByUuid(customerUuid);
        verifyNoMoreInteractions(customerService);
    }

    @Test
    @DisplayName("should return created customer")
    public void createCustomer() throws Exception {
        // given
        Customer customer = getCustomer();
        when(customerService
                .save(any(Customer.class)))
                .thenReturn(getCustomer());

        // when
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));

        // then
        verify(customerService,
                times(1))
                .save(any(Customer.class));
        verifyNoMoreInteractions(customerService);
    }

    @Test
    @DisplayName("should return updated customer")
    public void updateCustomer() throws Exception {
        // given
        Customer customer = getCustomer();
        UUID customerUuid = customer.getUuid();

        when(customerService.update(any(UUID.class),
                any(Customer.class)))
                .thenReturn(customer);

        // when
        mockMvc.perform(put("/api/v1/customers/" + customerUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));

        // then
        verify(customerService,
                times(1))
                .update(eq(customerUuid), any(Customer.class));
        verifyNoMoreInteractions(customerService);
    }

    @Test
    @DisplayName("should return no content after deleting customer")
    public void deleteCustomer() throws Exception {
        // given
        Customer customer = getCustomer();
        UUID customerUuid = customer.getUuid();

        // when
        mockMvc.perform(delete("/api/v1/customers/" + customerUuid))
                .andExpect(status().isNoContent());

        // then
        verify(customerService,
                times(1))
                .delete(customerUuid);
        verifyNoMoreInteractions(customerService);
    }
}