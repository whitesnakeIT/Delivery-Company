package com.kapusniak.tomasz.controller;

import com.kapusniak.tomasz.openapi.api.TrackingApi;
import com.kapusniak.tomasz.openapi.model.Tracking;
import com.kapusniak.tomasz.service.model.TrackingService;
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
public class TrackingController implements TrackingApi {

    private final TrackingService trackingService;

    @Override
    public ResponseEntity<Tracking> createTracking(@RequestBody @Valid Tracking tracking) {
        Tracking save = trackingService.save(tracking);

        return ResponseEntity.status(201).body(save);
    }

    @Override
    public ResponseEntity<Void> deleteTracking(@PathVariable("uuid") UUID trackingUuid) {
        trackingService.delete(trackingUuid);

        return ResponseEntity
                .noContent()
                .build();
    }

    @Override
    public ResponseEntity<List<Tracking>> getAllTracking(@RequestParam(defaultValue = "0") Integer pageNumber) {
        Page<Tracking> trackingList = trackingService.findAll(pageNumber);

        return ResponseEntity.ok(trackingList.getContent());
    }

    @Override
    public ResponseEntity<Tracking> getTracking(@PathVariable("uuid") UUID trackingUuid) {
        Tracking tracking = trackingService.findByUuid(trackingUuid);

        return ResponseEntity.ok(tracking);
    }

    @Override
    public ResponseEntity<Tracking> updateTracking(@PathVariable("uuid") UUID trackingUuid, @RequestBody @Valid Tracking tracking) {
        Tracking update = trackingService.update(trackingUuid, tracking);

        return ResponseEntity.ok(update);
    }
}
