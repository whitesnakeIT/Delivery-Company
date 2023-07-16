package com.kapusniak.tomasz.service;

import com.kapusniak.tomasz.entity.OrderEntity;
import com.kapusniak.tomasz.mapper.OrderEntityMapper;
import com.kapusniak.tomasz.openapi.model.Order;
import com.kapusniak.tomasz.openapi.model.PackageSize;
import com.kapusniak.tomasz.openapi.model.PackageType;
import com.kapusniak.tomasz.repository.jpa.OrderJpaRepository;
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
public class OrderService implements BaseEntityService<OrderEntity> {

    private final OrderJpaRepository orderRepository;

    private final OrderEntityMapper orderEntityMapper;

    @Transactional
    @CachePut(value = "orders", key = "#result.uuid")
    public Order save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Saving order failed. Order is null.");
        }
        OrderEntity orderEntity = orderEntityMapper.mapToEntity(order);
        OrderEntity savedEntity = orderRepository.save(orderEntity);

        return orderEntityMapper.mapToApiModel(savedEntity);
    }

    @Cacheable(value = "orders")
    public List<Order> findAll() {
        return orderRepository
                .findAll()
                .stream()
                .map(orderEntityMapper::mapToApiModel)
                .toList();
    }

    @Cacheable(value = "orders", key = "#orderUuid")
    public Order findByUuid(UUID orderUuid) {
        if (orderUuid == null) {
            throw new EntityNotFoundException("Searching for order failed. Order uuid is null.");
        }
        return orderEntityMapper.mapToApiModel(orderRepository.findByUuid(orderUuid)
                .orElseThrow(() ->
                        new EntityNotFoundException("Searching for order failed. Unrecognized uuid " + orderUuid)));
    }

    @CacheEvict(value = "orders", key = "#orderUuid")
    @Transactional
    public void delete(UUID orderUuid) {
        if (orderUuid == null) {
            throw new IllegalArgumentException("Deleting order failed. Order uuid is null.");
        }
        Order order = findByUuid(orderUuid);

        orderRepository.delete(orderEntityMapper.mapToEntity(order));
    }

    @CachePut(value = "orders", key = "#uuid")
    @Transactional
    public Order update(UUID uuid, Order order) {
        if (uuid == null) {
            throw new IllegalArgumentException("Updating order failed. Order uuid is null.");
        }
        if (order == null) {
            throw new IllegalArgumentException("Updating order failed. Order is null.");
        }

        Order orderFromDb = findByUuid(uuid);

        Order updatedOrder = updateFields(orderFromDb, order);

        OrderEntity updatedOrderEntity = orderRepository
                .save(orderEntityMapper.mapToEntity(updatedOrder));

        return orderEntityMapper.mapToApiModel(updatedOrderEntity);
    }

    private Order updateFields(Order orderFromDb, Order newOrder) {
        if (newOrder.getUuid() == null) {
            newOrder.setUuid(orderFromDb.getUuid());
        }
        if (!newOrder.getUuid().equals(orderFromDb.getUuid())) {
            throw new IllegalArgumentException("Updating order fields failed. Different uuid's");
        }
        return newOrder;
    }

    @Cacheable(value = "orders")
    public List<Order> findByPackageType(PackageType packageType) {
        if (packageType == null) {
            throw new EntityNotFoundException("Searching for order failed. Package type is null.");
        }

        return orderRepository
                .findByPackageType(packageType)
                .stream().map(orderEntityMapper::mapToApiModel)
                .toList();
    }

    @Cacheable(value = "orders")
    public List<Order> findByPackageSize(PackageSize packageSize) {
        if (packageSize == null) {
            throw new EntityNotFoundException("Searching for order failed. Package size is null.");
        }

        return orderRepository
                .findByPackageSize(packageSize)
                .stream().map(orderEntityMapper::mapToApiModel)
                .toList();
    }

    @Cacheable(value = "order")
    public List<Order> findAllByCustomerUuid(UUID customerUuid) {
        if (customerUuid == null) {
            throw new EntityNotFoundException("Searching for customer orders failed. Customer uuid is null.");
        }

        return orderRepository
                .findAllByCustomerUuid(customerUuid)
                .stream().map(orderEntityMapper::mapToApiModel)
                .toList();
    }

    @Override
    public OrderEntity convertUuidToEntity(UUID uuid) {
        return orderRepository.findByUuid(uuid).orElseThrow();
    }

    @Override
    public List<OrderEntity> convertUuidToEntity(List<UUID> uuidList) {
        return orderRepository.findAllByUuidIn(uuidList);
    }
}
