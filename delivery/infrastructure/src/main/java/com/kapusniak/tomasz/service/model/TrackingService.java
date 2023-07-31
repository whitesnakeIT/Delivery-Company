package com.kapusniak.tomasz.service.model;

import com.kapusniak.tomasz.entity.TrackingEntity;
import com.kapusniak.tomasz.mapper.TrackingEntityMapper;
import com.kapusniak.tomasz.openapi.model.Tracking;
import com.kapusniak.tomasz.repository.PageSize;
import com.kapusniak.tomasz.repository.jpa.TrackingJpaRepository;
import com.kapusniak.tomasz.service.BaseEntityService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackingService implements BaseEntityService<TrackingEntity>, BaseModelService<Tracking> {

    private final TrackingJpaRepository trackingRepository;

    private final TrackingEntityMapper trackingEntityMapper;

    @Override
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

    @Override
    @Cacheable(value = "tracking", key = "#trackingUuid")
    public Tracking findByUuid(UUID trackingUuid) {
        if (trackingUuid == null) {
            throw new EntityNotFoundException("Searching for tracking failed. Tracking uuid is null.");
        }
        return trackingEntityMapper.mapToApiModel(trackingRepository.findByUuid(trackingUuid)
                .orElseThrow(() ->
                        new EntityNotFoundException("Searching for tracking failed. Unrecognized uuid " + trackingUuid)));
    }

    @Override
    @Transactional
    @CacheEvict(value = "tracking", key = "#trackingUuid")
    public void delete(UUID trackingUuid) {
        if (trackingUuid == null) {
            throw new IllegalArgumentException("Deleting tracking failed. Tracking uuid is null.");
        }
        Tracking tracking = findByUuid(trackingUuid);

        trackingRepository.delete(trackingEntityMapper.mapToEntity(tracking));
    }

    @Override
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

    @Override
    @Cacheable(value = "tracking")
    public Page<Tracking> findAll(Integer page) {
        Integer pageNumber = validatePage(page);
        Page<TrackingEntity> trackingPage = trackingRepository
                .findAll(PageRequest.of(
                        pageNumber,
                        PageSize.EXTRA_SMALL.getValue()));
        List<Tracking> tracking = trackingPage
                .getContent()
                .stream()
                .map(trackingEntityMapper::mapToApiModel)
                .toList();
        Pageable pageable = PageRequest.of(page, PageSize.EXTRA_SMALL.getValue());

        return new PageImpl<>(tracking, pageable, trackingPage.getTotalElements());
    }
}
