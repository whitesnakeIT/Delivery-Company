package com.kapusniak.tomasz.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapusniak.tomasz.openapi.model.Order;
import com.kapusniak.tomasz.openapi.model.PackageType;
import com.kapusniak.tomasz.service.CustomerService;
import com.kapusniak.tomasz.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.kapusniak.tomasz.openapi.model.PackageSize.LARGE;
import static com.kapusniak.tomasz.openapi.model.PackageType.DOCUMENT;
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
public class OrderTest {

    private static final UUID ORDER_UUID_1 = UUID.fromString("29755321-c483-4a12-9f64-30a132038b70");
    private static final UUID CUSTOMER_UUID_1 = UUID.fromString("28f60dc1-993a-4d08-ac54-850a1fefb6a3");

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Order prepareOrder() {
        Order order = new Order();
        order.setPreferredDeliveryDate(LocalDate.of(2023, 6, 7));
        order.setPackageSize(LARGE);
        order.setPackageType(DOCUMENT);
        order.setSenderAddress("test sender address");
        order.setReceiverAddress("test receiver address");
        order.setUuid(ORDER_UUID_1);

        order.setCustomer(CUSTOMER_UUID_1);

        return order;
    }

    @Test
    @DisplayName("should return http status 403 unauthorized when user is anonymous" +
            " (test shouldn't return 401 cause of RFC 7231)")
    @WithAnonymousUser
    public void getAllOrdersAnonymous() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/orders"));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should return http status 403 forbidden when user not have ADMIN authority")
    @WithMockUser(authorities = "USER")
    public void getAllOrdersForbidden() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/orders"));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should correctly get Order from database and verify" +
            " properties with Order from controller method")
    void getOrderExisting() throws Exception {
        // given
        UUID orderUuid = ORDER_UUID_1;
        Order order = orderService.findByUuid(orderUuid);

        // when
        ResultActions result =
                mockMvc.perform(get(
                        "/api/v1/orders/" + orderUuid));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(order.getUuid().toString()));
    }

    @Test
    @DisplayName("should return ResponseEntity<ApiError> with correct json data" +
            " when provided order uuid is not existing in database for searching")
    void getOrderNonExisting() throws Exception {
        // given
        UUID orderUuid = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(get(
                "/api/v1/orders/" + orderUuid));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("httpStatus", equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("timestamp", notNullValue()))
                .andExpect(jsonPath("message", equalTo("Searching for order failed. Unrecognized uuid " + orderUuid)));
    }

    @Test
    @DisplayName("should correctly return list Orders from database after executing" +
            " method from controller")
    void getAllOrders() throws Exception {
        // given
        List<Order> orderList = orderService.findAll();

        // when
        ResultActions result =
                mockMvc.perform(get(
                        "/api/v1/orders"));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].uuid")
                        .value(orderList.get(0).getUuid().toString()))
                .andExpect(jsonPath("$[1].uuid")
                        .value(orderList.get(1).getUuid().toString()));


    }

    @Sql("classpath:integration-test-scripts/cleanup.sql")
    @Test
    @DisplayName("should correctly return empty list of Orders when table is empty" +
            "after executing method from controller")
    void getAllOrdersEmpty() throws Exception {
        // when
        ResultActions result =
                mockMvc.perform(get(
                        "/api/v1/orders"));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().json("[]"));

    }

    @Test
    @DisplayName("should save Order to database after executing method" +
            " from controller")
    void createOrder() throws Exception {
        // given
        Order order = prepareOrder();

        // when
        ResultActions result = mockMvc
                .perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").isNotEmpty())
                .andExpect(jsonPath("$.packageType").value(order.getPackageType().toString()));
    }

    @Test
    @DisplayName("should delete Order from database after executing" +
            " method from controller")
    void deleteOrderExisting() throws Exception {
        // given
        UUID orderUuid = ORDER_UUID_1;
        int sizeBeforeDeleting = orderService.findAll().size();

        // when
        ResultActions result =
                mockMvc.perform(delete(
                        "/api/v1/orders/" + orderUuid));

        // then
        result.andExpect(status().isNoContent());

        // and
        int sizeAfterDeleting = orderService.findAll().size();
        assertThat(sizeAfterDeleting).isEqualTo(sizeBeforeDeleting - 1);

    }

    @Test
    @DisplayName("should return ResponseEntity<ApiError> with correct json data" +
            " when provided order uuid is not existing in database for deleting")
    void deleteOrderNonExisting() throws Exception {
        // given
        UUID orderUuid = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(delete(
                "/api/v1/orders/" + orderUuid));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("httpStatus", equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("timestamp", notNullValue()))
                .andExpect(jsonPath("message", equalTo("Searching for order failed. Unrecognized uuid " + orderUuid)));
    }

    @Test
    @DisplayName("should save edited Order to database after executing method" +
            " from controller")
    void updateOrder() throws Exception {
        // given
        UUID orderUuid = ORDER_UUID_1;
        Order orderBeforeEdit = orderService.findByUuid(orderUuid);
        PackageType newPackageType = PackageType.DOCUMENT;

        // and
        orderBeforeEdit.setPackageType(PackageType.DOCUMENT);

        // when
        ResultActions result = mockMvc
                .perform(put("/api/v1/orders/" + orderUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderBeforeEdit)));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(orderBeforeEdit.getUuid().toString()))
                .andExpect(jsonPath("$.packageType").value(newPackageType.toString()));

        // and
        Order orderAfterEdit = orderService.findByUuid(orderUuid);
        assertThat(orderAfterEdit.getPackageType()).isEqualTo(newPackageType);
    }
}