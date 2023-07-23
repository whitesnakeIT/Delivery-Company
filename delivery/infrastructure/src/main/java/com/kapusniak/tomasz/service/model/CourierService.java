package com.kapusniak.tomasz.service.model;

import com.kapusniak.tomasz.entity.CourierEntity;
import com.kapusniak.tomasz.mapper.CourierEntityMapper;
import com.kapusniak.tomasz.openapi.model.Courier;
import com.kapusniak.tomasz.repository.PageSize;
import com.kapusniak.tomasz.repository.jpa.CourierJpaRepository;
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
public class CourierService implements BaseEntityService<CourierEntity>, BaseModelService<Courier> {

    private final CourierJpaRepository courierRepository;

    private final CourierEntityMapper courierEntityMapper;

    @Override
    @Transactional
    @CachePut(value = "courier", key = "#courier.uuid")
    public Courier save(Courier courier) {
        if (courier == null) {
            throw new IllegalArgumentException("Saving courier failed. Courier is null.");
        }
        CourierEntity courierEntity = courierEntityMapper.mapToEntity(courier);
        CourierEntity savedEntity = courierRepository.save(courierEntity);

        return courierEntityMapper.mapToApiModel(savedEntity);
    }

    @Override
    @Cacheable(value = "courier", key = "#courierUuid")
    public Courier findByUuid(UUID courierUuid) {
        if (courierUuid == null) {
            throw new EntityNotFoundException("Searching for courier failed. Courier uuid is null.");
        }
        return courierEntityMapper.mapToApiModel(courierRepository.findByUuid(courierUuid)
                .orElseThrow(() ->
                        new EntityNotFoundException("Searching for courier failed. Unrecognized uuid " + courierUuid)));
    }

    @Override
    @Transactional
    @CacheEvict(value = "courier", key = "#courierUuid")
    public void delete(UUID courierUuid) {
        if (courierUuid == null) {
            throw new IllegalArgumentException("Deleting courier failed. Courier uuid is null.");
        }
        Courier courier = findByUuid(courierUuid);

        courierRepository.delete(courierEntityMapper.mapToEntity(courier));
    }

    @Override
    @Transactional
    @CachePut(value = "courier", key = "#uuid")
    public Courier update(UUID uuid, Courier courier) {
        if (uuid == null) {
            throw new IllegalArgumentException("Updating courier failed. Courier uuid is null.");
        }
        if (courier == null) {
            throw new IllegalArgumentException("Updating courier failed. Courier is null.");
        }

        Courier courierFromDb = findByUuid(uuid);

        Courier updatedCourier = updateFields(courierFromDb, courier);

        CourierEntity updatedCourierEntity = courierRepository
                .save(courierEntityMapper.mapToEntity(updatedCourier));

        return courierEntityMapper.mapToApiModel(updatedCourierEntity);
    }

    private Courier updateFields(Courier courierFromDb, Courier newCourier) {
        if (newCourier.getUuid() == null) {
            newCourier.setUuid(courierFromDb.getUuid());
        }
        if (!newCourier.getUuid().equals(courierFromDb.getUuid())) {
            throw new IllegalArgumentException("Updating courier fields failed. Different uuid's");
        }
        return newCourier;
    }

    @Override
    public CourierEntity convertUuidToEntity(UUID uuid) {
        return courierRepository.findByUuid(uuid).orElseThrow();

    }

    @Override
    public List<CourierEntity> convertUuidToEntity(List<UUID> uuidList) {
        return courierRepository.findAllByUuidIn(uuidList);
    }

    @Override
    @Cacheable(value = "couriers")
    public Page<Courier> findAll(Integer page) {
        Integer pageNumber = validatePage(page);
        Page<CourierEntity> courierPage = courierRepository
                .findAll(PageRequest.of(
                        pageNumber,
                        PageSize.EXTRA_SMALL.getValue()));
        List<Courier> couriers = courierPage
                .getContent()
                .stream()
                .map(courierEntityMapper::mapToApiModel)
                .toList();
        Pageable pageable = PageRequest.of(page, PageSize.EXTRA_SMALL.getValue());

        return new PageImpl<>(couriers, pageable, courierPage.getTotalElements());
    }

}
