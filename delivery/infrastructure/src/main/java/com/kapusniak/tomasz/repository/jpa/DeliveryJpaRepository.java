package com.kapusniak.tomasz.repository.jpa;

import com.kapusniak.tomasz.entity.DeliveryEntity;
import com.kapusniak.tomasz.openapi.model.DeliveryStatus;
import com.kapusniak.tomasz.repository.UuidRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface DeliveryJpaRepository extends UuidRepository<DeliveryEntity, UUID> {

    List<DeliveryEntity> findAllByDeliveryStatus(DeliveryStatus deliveryStatus);

    List<DeliveryEntity> findAllByDeliveryStatusAndDeliveryTimeBefore(DeliveryStatus deliveryStatus, LocalDateTime deliveryTime);

}