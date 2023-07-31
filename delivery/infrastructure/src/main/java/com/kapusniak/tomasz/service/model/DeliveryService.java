package com.kapusniak.tomasz.service.model;

import com.kapusniak.tomasz.entity.DeliveryEntity;
import com.kapusniak.tomasz.mapper.DeliveryEntityMapper;
import com.kapusniak.tomasz.openapi.model.Delivery;
import com.kapusniak.tomasz.openapi.model.DeliveryStatus;
import com.kapusniak.tomasz.repository.PageSize;
import com.kapusniak.tomasz.repository.jpa.DeliveryJpaRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryService implements BaseEntityService<DeliveryEntity>, BaseModelService<Delivery> {

    private final DeliveryJpaRepository deliveryRepository;

    private final DeliveryEntityMapper deliveryEntityMapper;

    @Override
    @Transactional
    @CachePut(value = "delivery", key = "#delivery.uuid")
    public Delivery save(Delivery delivery) {
        if (delivery == null) {
            throw new IllegalArgumentException("Saving delivery failed. Delivery is null.");
        }
        DeliveryEntity deliveryEntity = deliveryEntityMapper.mapToEntity(delivery);
        DeliveryEntity savedEntity = deliveryRepository.save(deliveryEntity);

        return deliveryEntityMapper.mapToApiModel(savedEntity);
    }

    @Override
    @Cacheable(value = "delivery", key = "#deliveryUuid")
    public Delivery findByUuid(UUID deliveryUuid) {
        if (deliveryUuid == null) {
            throw new EntityNotFoundException("Searching for delivery failed. Delivery uuid is null.");
        }
        return deliveryEntityMapper.mapToApiModel(deliveryRepository.findByUuid(deliveryUuid)
                .orElseThrow(() ->
                        new EntityNotFoundException("Searching for delivery failed. Unrecognized uuid " + deliveryUuid)));
    }

    @Override
    @Transactional
    @CacheEvict(value = "delivery", key = "#deliveryUuid")
    public void delete(UUID deliveryUuid) {
        if (deliveryUuid == null) {
            throw new IllegalArgumentException("Deleting delivery failed. Delivery uuid is null.");
        }
        Delivery delivery = findByUuid(deliveryUuid);

        deliveryRepository.delete(deliveryEntityMapper.mapToEntity(delivery));
    }

    @Override
    @Transactional
    @CachePut(value = "delivery", key = "#uuid")
    public Delivery update(UUID uuid, Delivery delivery) {
        if (uuid == null) {
            throw new IllegalArgumentException("Updating delivery failed. Delivery uuid is null.");
        }
        if (delivery == null) {
            throw new IllegalArgumentException("Updating delivery failed. Delivery is null.");
        }

        Delivery deliveryFromDb = findByUuid(uuid);

        Delivery updatedDelivery = updateFields(deliveryFromDb, delivery);

        DeliveryEntity updatedDeliveryEntity = deliveryRepository
                .save(deliveryEntityMapper.mapToEntity(updatedDelivery));

        return deliveryEntityMapper.mapToApiModel(updatedDeliveryEntity);
    }

    private Delivery updateFields(Delivery deliveryFromDb, Delivery newDelivery) {
        if (newDelivery.getUuid() == null) {
            newDelivery.setUuid(deliveryFromDb.getUuid());
        }
        if (!newDelivery.getUuid().equals(deliveryFromDb.getUuid())) {
            throw new IllegalArgumentException("Updating delivery fields failed. Different uuid's");
        }
        return newDelivery;
    }

    public List<Delivery> findAllByDeliveryStatus(DeliveryStatus deliveryStatus) {
        if (deliveryStatus == null) {
            throw new EntityNotFoundException("Searching for deliveries failed. Delivery status is null.");
        }
        return deliveryRepository
                .findAllByDeliveryStatus(deliveryStatus)
                .stream()
                .map(deliveryEntityMapper::mapToApiModel)
                .toList();
    }

    public List<Delivery> findAllByDeliveryStatusAndDeliveryTimeBefore(DeliveryStatus deliveryStatus, LocalDateTime deliveryTime) {
        if (deliveryStatus == null) {
            throw new EntityNotFoundException("Searching for deliveries failed. Delivery status is null.");
        }
        if (deliveryTime == null) {
            throw new EntityNotFoundException("Searching for deliveries failed. Delivery time is null.");
        }
        return deliveryRepository
                .findAllByDeliveryStatusAndDeliveryTimeBefore(deliveryStatus, deliveryTime)
                .stream()
                .map(deliveryEntityMapper::mapToApiModel)
                .toList();
    }

    @Override
    public DeliveryEntity convertUuidToEntity(UUID uuid) {
        return deliveryRepository.findByUuid(uuid).orElseThrow();

    }

    @Override
    public List<DeliveryEntity> convertUuidToEntity(List<UUID> uuidList) {
        return deliveryRepository.findAllByUuidIn(uuidList);
    }

    @Override
    @Cacheable(value = "deliveries")
    public Page<Delivery> findAll(Integer page) {
        Integer pageNumber = validatePage(page);
        Page<DeliveryEntity> deliveryPage = deliveryRepository
                .findAll(PageRequest.of(
                        pageNumber,
                        PageSize.EXTRA_SMALL.getValue()));
        List<Delivery> deliveries = deliveryPage
                .getContent()
                .stream()
                .map(deliveryEntityMapper::mapToApiModel)
                .toList();
        Pageable pageable = PageRequest.of(page, PageSize.EXTRA_SMALL.getValue());

        return new PageImpl<>(deliveries, pageable, deliveryPage.getTotalElements());
    }
}
