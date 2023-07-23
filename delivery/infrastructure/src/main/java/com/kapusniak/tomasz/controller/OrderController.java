package com.kapusniak.tomasz.controller;

import com.kapusniak.tomasz.openapi.api.OrdersApi;
import com.kapusniak.tomasz.openapi.model.Order;
import com.kapusniak.tomasz.service.model.OrderService;
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
public class OrderController implements OrdersApi {

    private final OrderService orderService;

    @Override
    public ResponseEntity<Order> createOrder(@RequestBody @Valid Order order) {
        Order save = orderService.save(order);

        return ResponseEntity.status(201).body(save);
    }

    @Override
    public ResponseEntity<Void> deleteOrder(@PathVariable("uuid") UUID orderUuid) {
        orderService.delete(orderUuid);

        return ResponseEntity
                .noContent()
                .build();
    }

    @Override
    public ResponseEntity<List<Order>> getAllOrders(@RequestParam(defaultValue = "0") Integer pageNumber) {
        Page<Order> orderList = orderService.findAll(pageNumber);

        return ResponseEntity.ok(orderList.getContent());
    }

    @Override
    public ResponseEntity<Order> getOrder(@PathVariable("uuid") UUID orderUuid) {
        Order order = orderService.findByUuid(orderUuid);

        return ResponseEntity.ok(order);
    }

    @Override
    public ResponseEntity<Order> updateOrder(@PathVariable("uuid") UUID orderUuid, @RequestBody @Valid Order order) {
        Order update = orderService.update(orderUuid, order);

        return ResponseEntity.ok(update);
    }
}
