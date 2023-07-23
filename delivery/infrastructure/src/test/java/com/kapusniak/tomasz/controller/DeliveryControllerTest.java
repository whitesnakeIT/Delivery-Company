package com.kapusniak.tomasz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapusniak.tomasz.openapi.model.Delivery;
import com.kapusniak.tomasz.service.model.DeliveryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.kapusniak.tomasz.openapi.model.DeliveryStatus.CREATED;
import static com.kapusniak.tomasz.openapi.model.DeliveryStatus.DELIVERED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DeliveryControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeliveryService deliveryService;

    public Delivery getDelivery() {
        Delivery delivery1 = new Delivery();
        delivery1.setId(1L);
        delivery1.setDeliveryStatus(CREATED);
        delivery1.setUuid(UUID.randomUUID());
        LocalDateTime deliveryLocalDateTime = LocalDateTime.of(2023, 6, 7, 22, 30, 0);
        OffsetDateTime deliveryOffsetDateTime = deliveryLocalDateTime.atOffset(ZoneOffset.UTC);
        delivery1.setDeliveryTime(deliveryOffsetDateTime);
        delivery1.setPrice(50.55);

        return delivery1;
    }

    public List<Delivery> getDeliveryList() {
        List<Delivery> deliveries = new ArrayList<>();
        Delivery delivery1 = new Delivery();
        delivery1.setId(1L);
        delivery1.setDeliveryStatus(CREATED);
        Delivery delivery2 = new Delivery();
        delivery2.setId(2L);
        delivery2.setDeliveryStatus(DELIVERED);
        deliveries.add(delivery1);
        deliveries.add(delivery2);

        return deliveries;
    }

    @Test
    @DisplayName("should correctly return list of deliveries")
    public void getAllDeliveries() throws Exception {
        // given
        List<Delivery> deliveries = getDeliveryList();
        when(deliveryService.findAll()).thenReturn(deliveries);

        // when
        mockMvc.perform(get("/api/v1/deliveries"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].deliveryStatus").value(CREATED.toString()))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].deliveryStatus").value(DELIVERED.toString()));

        // then
        verify(deliveryService,
                times(1))
                .findAll();
        verifyNoMoreInteractions(deliveryService);
    }

    @Test
    @DisplayName("should correctly return delivery based on delivery uuid")
    public void getDeliveryByUuid() throws Exception {
        // given
        Delivery delivery = getDelivery();
        UUID deliveryUuid = delivery.getUuid();

        when(deliveryService.findByUuid(deliveryUuid)).thenReturn(delivery);

        // when
        mockMvc.perform(get("/api/v1/deliveries/" + deliveryUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.deliveryStatus").value(CREATED.toString()));

        // then
        verify(deliveryService,
                times(1))
                .findByUuid(deliveryUuid);
        verifyNoMoreInteractions(deliveryService);
    }

    @Test
    @DisplayName("should return created delivery")
    public void createDelivery() throws Exception {
        // given
        Delivery delivery = getDelivery();
        when(deliveryService
                .save(any(Delivery.class)))
                .thenReturn(getDelivery());

        // when
        mockMvc.perform(post("/api/v1/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(delivery)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.deliveryStatus").value(CREATED.toString()));

        // then
        verify(deliveryService,
                times(1))
                .save(any(Delivery.class));
        verifyNoMoreInteractions(deliveryService);
    }

    @Test
    @DisplayName("should return updated delivery")
    public void updateDelivery() throws Exception {
        // given
        Delivery delivery = getDelivery();
        UUID deliveryUuid = delivery.getUuid();

        when(deliveryService.update(any(UUID.class),
                any(Delivery.class))).thenReturn(delivery);

        // when
        mockMvc.perform(put("/api/v1/deliveries/" + deliveryUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(delivery)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.deliveryStatus").value(CREATED.toString()));

        // then
        verify(deliveryService,
                times(1))
                .update(eq(deliveryUuid), any(Delivery.class));
        verifyNoMoreInteractions(deliveryService);
    }

    @Test
    @DisplayName("should return no content after deleting delivery")
    public void deleteDelivery() throws Exception {
        // given
        Delivery delivery = getDelivery();
        UUID deliveryUuid = delivery.getUuid();

        // when
        mockMvc.perform(delete("/api/v1/deliveries/" + deliveryUuid))
                .andExpect(status().isNoContent());

        // then
        verify(deliveryService,
                times(1))
                .delete(deliveryUuid);
        verifyNoMoreInteractions(deliveryService);
    }
}