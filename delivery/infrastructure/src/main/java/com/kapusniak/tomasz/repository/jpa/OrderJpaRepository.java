package com.kapusniak.tomasz.repository.jpa;

import com.kapusniak.tomasz.entity.OrderEntity;
import com.kapusniak.tomasz.openapi.model.PackageSize;
import com.kapusniak.tomasz.openapi.model.PackageType;
import com.kapusniak.tomasz.repository.UuidRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface OrderJpaRepository extends UuidRepository<OrderEntity, UUID> {

    Page<OrderEntity> findByPackageType(PackageType packageType, Pageable pageable);

    Page<OrderEntity> findByPackageSize(PackageSize packageSize, Pageable pageable);

    Page<OrderEntity> findAllByCustomerUuid(UUID customerId, Pageable pageable);

    List<OrderEntity> findAllByPreferredDeliveryDateBetween(LocalDate startDate, LocalDate endDate);

}
