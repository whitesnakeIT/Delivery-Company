package com.kapusniak.tomasz.service;

import com.kapusniak.tomasz.entity.TrackingEntity;
import com.kapusniak.tomasz.mapper.TrackingEntityMapper;
import com.kapusniak.tomasz.openapi.model.Tracking;
import com.kapusniak.tomasz.repository.jpa.TrackingJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackingService implements BaseEntityService<TrackingEntity> {

    private final TrackingJpaRepository trackingRepository;

    private final TrackingEntityMapper trackingEntityMapper;

    @Transactional
    @CachePut(value = "tracking", key = "#result.uuid")
    public Tracking save(Tracking tracking) {
        if (tracking == null) {
            throw new IllegalArgumentException("Saving tracking failed. Tracking is null.");
        }
        TrackingEntity trackingEntity = trackingEntityMapper.mapToEntity(tracking);
        TrackingEntity savedEntity = trackingRepository.save(trackingEntity);

        return trackingEntityMapper.mapToApiModel(savedEntity);
    }

    @Cacheable(value = "tracking")
    public List<Tracking> findAll() {
        return trackingRepository
                .findAll()
                .stream()
                .map(trackingEntityMapper::mapToApiModel)
                .toList();
    }

    @Cacheable(value = "tracking", key = "#trackingUuid")
    public Tracking findByUuid(UUID trackingUuid) {
        if (trackingUuid == null) {
            throw new EntityNotFoundException("Searching for tracking failed. Tracking uuid is null.");
        }
        return trackingEntityMapper.mapToApiModel(trackingRepository.findByUuid(trackingUuid)
                .orElseThrow(() ->
                        new EntityNotFoundException("Searching for tracking failed. Unrecognized uuid " + trackingUuid)));
    }

    @Transactional
    @CacheEvict(value = "tracking", key = "#trackingUuid")
    public void delete(UUID trackingUuid) {
        if (trackingUuid == null) {
            throw new IllegalArgumentException("Deleting tracking failed. Tracking uuid is null.");
        }
        Tracking tracking = findByUuid(trackingUuid);

        trackingRepository.delete(trackingEntityMapper.mapToEntity(tracking));
    }

    @Transactional
    @CachePut(value = "tracking", key = "#uuid")
    public Tracking update(UUID uuid, Tracking tracking) {
        if (uuid == null) {
            throw new IllegalArgumentException("Updating tracking failed. Tracking uuid is null.");
        }
        if (tracking == null) {
            throw new IllegalArgumentException("Updating tracking failed. Tracking is null.");
        }

        Tracking trackingFromDb = findByUuid(uuid);

        Tracking updatedTracking = updateFields(trackingFromDb, tracking);

        TrackingEntity updatedTrackingEntity = trackingRepository
                .save(trackingEntityMapper.mapToEntity(updatedTracking));

        return trackingEntityMapper.mapToApiModel(updatedTrackingEntity);
    }

    private Tracking updateFields(Tracking trackingFromDb, Tracking newTracking) {
        if (newTracking.getUuid() == null) {
            newTracking.setUuid(trackingFromDb.getUuid());
        }
        if (!newTracking.getUuid().equals(trackingFromDb.getUuid())) {
            throw new IllegalArgumentException("Updating tracking fields failed. Different uuid's");
        }
        return newTracking;
    }

    @Override
    public TrackingEntity convertUuidToEntity(UUID uuid) {
        return trackingRepository.findByUuid(uuid).orElseThrow();
    }

    @Override
    public List<TrackingEntity> convertUuidToEntity(List<UUID> uuidList) {
        return trackingRepository.findAllByUuidIn(uuidList);
    }
}
