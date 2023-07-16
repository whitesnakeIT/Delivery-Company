package com.kapusniak.tomasz.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapusniak.tomasz.openapi.model.Delivery;
import com.kapusniak.tomasz.openapi.model.DeliveryStatus;
import com.kapusniak.tomasz.service.DeliveryService;
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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static com.kapusniak.tomasz.openapi.model.DeliveryStatus.IN_TRANSIT;
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
public class DeliveryTest {

    private static final UUID DELIVERY_UUID_1 = UUID.fromString("31822712-94b3-43ed-9aac-24613948ca79");
    private static final UUID ORDER_UUID = UUID.fromString("29755321-c483-4a12-9f64-30a132038b70");
    private static final UUID COURIER_UUID = UUID.fromString("fe362772-17c3-4547-b559-ceb13e164e6f");

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Delivery prepareDelivery() {
        Delivery delivery = new Delivery();
        UUID deliveryUuid = DELIVERY_UUID_1;
        Double price = 40.50D;
        LocalDateTime deliveryLocalDateTime = LocalDateTime.of(2023, 6, 5, 23, 30, 0);
        OffsetDateTime deliveryOffsetDateTime = deliveryLocalDateTime.atOffset(ZoneOffset.UTC);
        DeliveryStatus deliveryStatus = IN_TRANSIT;

        delivery.setUuid(deliveryUuid);
        delivery.setPrice(price);
        delivery.setDeliveryTime(deliveryOffsetDateTime);
        delivery.setDeliveryStatus(deliveryStatus);
        delivery.setUuid(deliveryUuid);

        delivery.setCourier(COURIER_UUID);
        delivery.setOrder(ORDER_UUID);

        return delivery;
    }

    @Test
    @DisplayName("should return http status 403 unauthorized when user is anonymous" +
            " (test shouldn't return 401 cause of RFC 7231)")
    @WithAnonymousUser
    public void getAllDeliveriesAnonymous() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/deliveries"));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should return http status 403 forbidden when user not have ADMIN authority")
    @WithMockUser(authorities = "USER")
    public void getAllDeliveriesForbidden() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/deliveries"));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should correctly get Delivery from database and verify" +
            " properties with Delivery from controller method")
    void getDeliveryExisting() throws Exception {
        // given
        UUID deliveryUuid = DELIVERY_UUID_1;
        Delivery delivery = deliveryService.findByUuid(deliveryUuid);

        // when
        ResultActions result =
                mockMvc.perform(get(
                        "/api/v1/deliveries/" + deliveryUuid));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(delivery.getUuid().toString()));
    }

    @Test
    @DisplayName("should return ResponseEntity<ApiError> with correct json data" +
            " when provided delivery uuid is not existing in database for searching")
    void getDeliveryNonExisting() throws Exception {
        // given
        UUID deliveryUuid = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(get(
                "/api/v1/deliveries/" + deliveryUuid));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("httpStatus", equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("timestamp", notNullValue()))
                .andExpect(jsonPath("message", equalTo("Searching for delivery failed. Unrecognized uuid " + deliveryUuid)));
    }

    @Test
    @DisplayName("should correctly return list Deliveries from database after executing" +
            " method from controller")
    void getAllDeliveries() throws Exception {
        // given
        List<Delivery> deliveryList = deliveryService.findAll();

        // when
        ResultActions result =
                mockMvc.perform(get(
                        "/api/v1/deliveries"));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].uuid")
                        .value(deliveryList.get(0).getUuid().toString()))
                .andExpect(jsonPath("$[1].uuid")
                        .value(deliveryList.get(1).getUuid().toString()));
    }

    @Sql("classpath:integration-test-scripts/cleanup.sql")
    @Test
    @DisplayName("should correctly return empty list of Deliveries when table is empty" +
            "after executing method from controller")
    void getAllDeliveriesEmpty() throws Exception {
        // when
        ResultActions result =
                mockMvc.perform(get(
                        "/api/v1/deliveries"));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().json("[]"));

    }

    @Test
    @DisplayName("should save edited Delivery to database after executing method" +
            " from controller")
    void createDelivery() throws Exception {
        // given
        Delivery delivery = prepareDelivery();


        // when
        ResultActions result = mockMvc
                .perform(post("/api/v1/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(delivery)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").isNotEmpty())
                .andExpect(jsonPath("$.deliveryStatus").value(delivery.getDeliveryStatus().toString()));
    }

    @Test
    @DisplayName("should delete Delivery from database after executing" +
            " method from controller")
    void deleteDeliveryExisting() throws Exception {
        // given
        UUID deliveryUuid = DELIVERY_UUID_1;
        int sizeBeforeDeleting = deliveryService.findAll().size();

        // when
        ResultActions result =
                mockMvc.perform(delete(
                        "/api/v1/deliveries/" + deliveryUuid));

        // then
        result.andExpect(status().isNoContent());

        // and
        int sizeAfterDeleting = deliveryService.findAll().size();
        assertThat(sizeAfterDeleting).isEqualTo(sizeBeforeDeleting - 1);

    }

    @Test
    @DisplayName("should return ResponseEntity<ApiError> with correct json data" +
            "when provided delivery uuid is not existing in database for deleting")
    void deleteDeliveryNonExisting() throws Exception {
        // given
        UUID deliveryUuid = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(delete(
                "/api/v1/deliveries/" + deliveryUuid));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("httpStatus", equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("timestamp", notNullValue()))
                .andExpect(jsonPath("message", equalTo("Searching for delivery failed. Unrecognized uuid " + deliveryUuid)));
    }

    @Test
    @DisplayName("should save Delivery to database after executing method" +
            " from controller")
    void updateDelivery() throws Exception {
        // given
        UUID deliveryUuid = DELIVERY_UUID_1;
        Delivery deliveryBeforeEdit = deliveryService.findByUuid(deliveryUuid);
        DeliveryStatus newDeliveryStatus = DeliveryStatus.FAILED_DELIVERY_ATTEMPT;

        // and
        deliveryBeforeEdit.setDeliveryStatus(newDeliveryStatus);

        // when
        ResultActions result = mockMvc
                .perform(put("/api/v1/deliveries/" + deliveryUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deliveryBeforeEdit)));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(deliveryBeforeEdit.getUuid().toString()))
                .andExpect(jsonPath("$.deliveryStatus").value(newDeliveryStatus.toString()));

        // and
        Delivery deliveryAfterEdit = deliveryService.findByUuid(deliveryUuid);
        assertThat(deliveryAfterEdit.getDeliveryStatus()).isEqualTo(newDeliveryStatus);
    }
}