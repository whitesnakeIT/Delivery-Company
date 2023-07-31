package com.kapusniak.tomasz.controller;

import com.kapusniak.tomasz.openapi.api.DeliveriesApi;
import com.kapusniak.tomasz.openapi.model.Delivery;
import com.kapusniak.tomasz.service.model.DeliveryService;
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
public class DeliveryController implements DeliveriesApi {

    private final DeliveryService deliveryService;

    @Override
    public ResponseEntity<Delivery> createDelivery(@RequestBody @Valid Delivery delivery) {
        Delivery save = deliveryService.save(delivery);

        return ResponseEntity.status(201).body(save);
    }

    @Override
    public ResponseEntity<Void> deleteDelivery(@PathVariable("uuid") UUID deliveryUuid) {
        deliveryService.delete(deliveryUuid);

        return ResponseEntity
                .noContent()
                .build();
    }

    @Override
    public ResponseEntity<List<Delivery>> getAllDeliveries(@RequestParam(defaultValue = "0") Integer pageNumber) {
        Page<Delivery> deliveryList = deliveryService.findAll(pageNumber);

        return ResponseEntity.ok(deliveryList.getContent());
    }

    @Override
    public ResponseEntity<Delivery> getDelivery(@PathVariable("uuid") UUID deliveryUuid) {
        Delivery delivery = deliveryService.findByUuid(deliveryUuid);

        return ResponseEntity.ok(delivery);
    }

    @Override
    public ResponseEntity<Delivery> updateDelivery(@PathVariable("uuid") UUID deliveryUuid, @RequestBody @Valid Delivery delivery) {
        Delivery update = deliveryService.update(deliveryUuid, delivery);

        return ResponseEntity.ok(update);
    }
}
