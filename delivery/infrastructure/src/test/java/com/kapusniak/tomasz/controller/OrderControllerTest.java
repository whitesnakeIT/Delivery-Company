package com.kapusniak.tomasz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapusniak.tomasz.openapi.model.Order;
import com.kapusniak.tomasz.service.model.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.kapusniak.tomasz.openapi.model.PackageSize.LARGE;
import static com.kapusniak.tomasz.openapi.model.PackageSize.SMALL;
import static com.kapusniak.tomasz.openapi.model.PackageType.DOCUMENT;
import static com.kapusniak.tomasz.openapi.model.PackageType.PARCEL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrderControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    public Order getOrder() {
        Order order1 = new Order();
        order1.setId(1L);
        order1.setSenderAddress("testSenderAddress1");
        order1.setPreferredDeliveryDate(LocalDate.of(2023, 5, 4));
        order1.setPackageSize(LARGE);
        order1.setPackageType(DOCUMENT);
        order1.setReceiverAddress("testReceiverAddress1");
        order1.setUuid(UUID.randomUUID());

        return order1;
    }

    public List<Order> getOrderList() {
        List<Order> orders = new ArrayList<>();
        Order order1 = new Order();
        order1.setId(1L);
        order1.setSenderAddress("testSenderAddress1");
        order1.setPreferredDeliveryDate(LocalDate.of(2023, 5, 4));
        order1.setPackageSize(LARGE);
        order1.setPackageType(DOCUMENT);
        order1.setReceiverAddress("testReceiverAddress1");
        order1.setUuid(UUID.randomUUID());

        Order order2 = new Order();
        order2.setId(2L);
        order2.setSenderAddress("testSenderAddress2");
        order2.setPreferredDeliveryDate(LocalDate.of(2023, 5, 4));
        order2.setPackageSize(SMALL);
        order2.setPackageType(PARCEL);
        order2.setReceiverAddress("testReceiverAddress2");
        order2.setUuid(UUID.randomUUID());

        orders.add(order1);
        orders.add(order2);
        return orders;
    }

    @Test
    @DisplayName("should correctly return list of orders")
    public void getAllOrders() throws Exception {
        // given
        List<Order> orders = getOrderList();
        when(orderService.findAll()).thenReturn(orders);

        // when
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].senderAddress").value("testSenderAddress1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].senderAddress").value("testSenderAddress2"));

        // then
        verify(orderService,
                times(1))
                .findAll();
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @DisplayName("should correctly return order based on order uuid")
    public void getOrderByUuid() throws Exception {
        // given
        Order order = getOrder();
        UUID orderUuid = order.getUuid();

        when(orderService.findByUuid(any())).thenReturn(order);

        // when
        mockMvc.perform(get("/api/v1/orders/" + orderUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.senderAddress").value("testSenderAddress1"));

        // then
        verify(orderService,
                times(1))
                .findByUuid(orderUuid);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @DisplayName("should return created order")
    public void createOrder() throws Exception {
        // given
        Order order = getOrder();
        when(orderService
                .save(any(Order.class)))
                .thenReturn(getOrder());

        // when
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.senderAddress").value("testSenderAddress1"));

        // then
        verify(orderService,
                times(1))
                .save(any(Order.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @DisplayName("should return updated order")
    public void updateOrder() throws Exception {
        // given
        Order order = getOrder();
        UUID orderUuid = order.getUuid();

        when(orderService.update(any(UUID.class),
                any(Order.class)))
                .thenReturn(order);

        // when
        mockMvc.perform(put("/api/v1/orders/" + orderUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.senderAddress").value("testSenderAddress1"));

        // then
        verify(orderService,
                times(1))
                .update(eq(orderUuid), any(Order.class));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @DisplayName("should return no content after deleting order")
    public void deleteOrder() throws Exception {
        // given
        Order order = getOrder();
        UUID orderUuid = order.getUuid();

        // when
        mockMvc.perform(delete("/api/v1/orders/" + orderUuid))
                .andExpect(status().isNoContent());

        // then
        verify(orderService,
                times(1))
                .delete(orderUuid);
        verifyNoMoreInteractions(orderService);
    }
}