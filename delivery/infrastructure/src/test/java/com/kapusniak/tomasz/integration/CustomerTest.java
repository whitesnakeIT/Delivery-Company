package com.kapusniak.tomasz.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapusniak.tomasz.openapi.model.Customer;
import com.kapusniak.tomasz.openapi.model.Order;
import com.kapusniak.tomasz.service.model.CustomerService;
import com.kapusniak.tomasz.service.model.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureDataJpa
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@TestPropertySource("classpath:application-integration-test.properties")
@SqlGroup(
        @Sql(
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
                scripts = {
                        "classpath:integration-test-scripts/cleanup.sql",
                        "classpath:integration-test-scripts/insert-data.sql"})
)
@WithMockUser(authorities = "ADMIN")
public class CustomerTest {

    private static final Integer PAGE_NUMBER = 0;
    private static final UUID UUID_CUSTOMER_1 = UUID.fromString("28f60dc1-993a-4d08-ac54-850a1fefb6a3");
    private static final UUID ORDER_UUID_1 = UUID.fromString("29755321-c483-4a12-9f64-30a132038b70");


    @Autowired
    private CustomerService customerService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private Customer prepareCustomer() {
        Customer customer = new Customer();
        Long testId = 1L;
        String testEmail = "test@email.com";
        String testFirstName = "testFirstName";
        String testLastName = "testLastName";
        customer.setId(testId);
        customer.setEmail(testEmail);
        customer.setFirstName(testFirstName);
        customer.setLastName(testLastName);
        customer.setUuid(UUID_CUSTOMER_1);
        customer.setVersion(0L);

        Order order = orderService.findByUuid(ORDER_UUID_1);

        customer.addOrdersItem(ORDER_UUID_1);


        return customer;
    }

    @Test
    @DisplayName("should return http status 403 unauthorized when user is anonymous" +
            " (test shouldn't return 401 cause of RFC 7231)")
    @WithAnonymousUser
    public void getAllCustomersAnonymous() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/customers"));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should return http status 403 forbidden when user not have ADMIN authority")
    @WithMockUser(authorities = "USER")
    public void getAllCustomersForbidden() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/customers"));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should correctly get Customer from database and verify" +
            " properties with Customer from controller method")
    void getCustomerExisting() throws Exception {
        // given
        UUID customerUuid = UUID_CUSTOMER_1;
        Customer customer = customerService.findByUuid(customerUuid);

        // when
        ResultActions result =
                mockMvc.perform(get(
                        "/api/v1/customers/" + customerUuid));


        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(customer.getUuid().toString()));
    }

    @Test
    @DisplayName("should return ResponseEntity<ApiError> with correct json data" +
            " when provided customer uuid is not existing in database for searching")
    void getCustomerNonExisting() throws Exception {
        // given
        UUID customerUuid = UUID.randomUUID();

        /// when
        ResultActions result = mockMvc.perform(get(
                "/api/v1/customers/" + customerUuid));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("httpStatus", equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("timestamp", notNullValue()))
                .andExpect(jsonPath("message", equalTo("Searching for customer failed. Unrecognized uuid " + customerUuid)));
    }

    @Test
    @DisplayName("should correctly return page Customers from database after executing" +
            " method from controller")
    void getAllCustomers() throws Exception {
        // given
        Page<Customer> customerPage = customerService.findAll(PAGE_NUMBER);

        // when
        ResultActions result =
                mockMvc.perform(get(
                        "/api/v1/customers"));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].uuid")
                        .value(customerPage.getContent().get(0).getUuid().toString()))
                .andExpect(jsonPath("$[1].uuid")
                        .value(customerPage.getContent().get(1).getUuid().toString()));


    }

    @Sql("classpath:integration-test-scripts/cleanup.sql")
    @Test
    @DisplayName("should correctly return empty list of Customers when table is empty" +
            "after executing method from controller")
    void getAllCustomersEmpty() throws Exception {
        // when
        ResultActions result =
                mockMvc.perform(get(
                        "/api/v1/customers"));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().json("[]"));

    }

    @Test
    @DisplayName("should save Customer to database after executing method" +
            " from controller")
    void createCustomer() throws Exception {
        // given
        Customer customer = prepareCustomer();

        // when
        ResultActions result = mockMvc
                .perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").isNotEmpty())
                .andExpect(jsonPath("$.firstName").value(customer.getFirstName()));
    }

    @Test
    @DisplayName("should delete Customer from database after executing" +
            " method from controller")
    void deleteCustomerExisting() throws Exception {
        // given
        UUID customerUuid = UUID_CUSTOMER_1;
        int sizeBeforeDeleting = customerService.findAll(PAGE_NUMBER).getContent().size();

        // when
        ResultActions result =
                mockMvc.perform(delete(
                        "/api/v1/customers/" + customerUuid));

        // then
        result.andExpect(status().isNoContent());

        // and
        int sizeAfterDeleting = customerService.findAll(PAGE_NUMBER).getContent().size();
        assertThat(sizeAfterDeleting).isEqualTo(sizeBeforeDeleting - 1);

    }

    @Test
    @DisplayName("should return ResponseEntity<ApiError> with correct json data" +
            " when provided customer uuid is not existing in database for deleting")
    void deleteCustomerNonExisting() throws Exception {
        // given
        UUID customerUuid = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(delete(
                "/api/v1/customers/" + customerUuid));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("httpStatus", equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("timestamp", notNullValue()))
                .andExpect(jsonPath("message", equalTo("Searching for customer failed. Unrecognized uuid " + customerUuid)));
    }

    @Test
    @DisplayName("should save edited Customer to database after executing method" +
            " from controller")
    void updateCustomer() throws Exception {
        // given
        UUID customerUuid = UUID_CUSTOMER_1;
        Customer customerBeforeEdit = customerService.findByUuid(customerUuid);
        String newFirstName = "newFirstName";
        String newLastName = "newLastName";


        // and
        customerBeforeEdit.setFirstName(newFirstName);
        customerBeforeEdit.setLastName(newLastName);

        // when
        ResultActions result = mockMvc
                .perform(put("/api/v1/customers/" + customerUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerBeforeEdit)));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(customerBeforeEdit.getUuid().toString()))
                .andExpect(jsonPath("$.firstName").value(newFirstName))
                .andExpect(jsonPath("$.lastName").value(newLastName));

        // and
        Customer customerAfterEdit = customerService.findByUuid(customerUuid);
        assertThat(customerAfterEdit.getFirstName()).isEqualTo(newFirstName);
        assertThat(customerAfterEdit.getLastName()).isEqualTo(newLastName);
    }
}