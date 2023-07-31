package com.kapusniak.tomasz.service.model;

import com.kapusniak.tomasz.entity.OrderEntity;
import com.kapusniak.tomasz.mapper.OrderEntityMapper;
import com.kapusniak.tomasz.openapi.model.Order;
import com.kapusniak.tomasz.openapi.model.PackageSize;
import com.kapusniak.tomasz.openapi.model.PackageType;
import com.kapusniak.tomasz.repository.PageSize;
import com.kapusniak.tomasz.repository.jpa.OrderJpaRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService implements BaseEntityService<OrderEntity>, BaseModelService<Order> {

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

    @Cacheable(value = "order", key = "#orderUuid")
    public Order findByUuid(UUID orderUuid) {
        if (orderUuid == null) {
            throw new EntityNotFoundException("Searching for order failed. Order uuid is null.");
        }
        return orderEntityMapper.mapToApiModel(orderRepository.findByUuid(orderUuid)
                .orElseThrow(() ->
                        new EntityNotFoundException("Searching for order failed. Unrecognized uuid " + orderUuid)));
    }

    @CacheEvict(value = "order", key = "#orderUuid")
    @Transactional
    public void delete(UUID orderUuid) {
        if (orderUuid == null) {
            throw new IllegalArgumentException("Deleting order failed. Order uuid is null.");
        }
        Order order = findByUuid(orderUuid);

        orderRepository.delete(orderEntityMapper.mapToEntity(order));
    }

    @CachePut(value = "order", key = "#uuid")
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

    @Cacheable(value = "ordersByPackageType")
    public Page<Order> findByPackageType(PackageType packageType, Integer page) {
        if (packageType == null) {
            throw new EntityNotFoundException("Searching for order failed. Package type is null.");
        }
        Integer pageNumber = validatePage(page);
        Page<OrderEntity> orderPage = orderRepository
                .findByPackageType(packageType, PageRequest.of(
                        pageNumber,
                        PageSize.EXTRA_SMALL.getValue()));

        List<Order> orders = orderPage
                .getContent()
                .stream()
                .map(orderEntityMapper::mapToApiModel)
                .toList();
        Pageable pageable = PageRequest.of(page, PageSize.EXTRA_SMALL.getValue());

        return new PageImpl<>(orders, pageable, orderPage.getTotalElements());
    }

    @Cacheable(value = "ordersByPackageSize")
    public Page<Order> findByPackageSize(PackageSize packageSize, Integer page) {
        if (packageSize == null) {
            throw new EntityNotFoundException("Searching for order failed. Package size is null.");
        }
        Integer pageNumber = validatePage(page);
        Page<OrderEntity> orderPage = orderRepository
                .findByPackageSize(packageSize, PageRequest.of(
                        pageNumber,
                        PageSize.EXTRA_SMALL.getValue()));

        List<Order> orders = orderPage
                .getContent()
                .stream()
                .map(orderEntityMapper::mapToApiModel)
                .toList();
        Pageable pageable = PageRequest.of(page, PageSize.EXTRA_SMALL.getValue());

        return new PageImpl<>(orders, pageable, orderPage.getTotalElements());
    }

    @Cacheable(value = "ordersByCustomerUuid")
    public Page<Order> findAllByCustomerUuid(UUID customerUuid, Integer page) {
        if (customerUuid == null) {
            throw new EntityNotFoundException("Searching for customer orders failed. Customer uuid is null.");
        }
        Integer pageNumber = validatePage(page);
        Page<OrderEntity> orderPage = orderRepository
                .findAllByCustomerUuid(customerUuid, PageRequest.of(
                        pageNumber,
                        PageSize.EXTRA_SMALL.getValue()));

        List<Order> orders = orderPage
                .getContent()
                .stream()
                .map(orderEntityMapper::mapToApiModel)
                .toList();
        Pageable pageable = PageRequest.of(page, PageSize.EXTRA_SMALL.getValue());

        return new PageImpl<>(orders, pageable, orderPage.getTotalElements());
    }

    @Override
    public OrderEntity convertUuidToEntity(UUID uuid) {
        return orderRepository.findByUuid(uuid).orElseThrow();
    }

    @Override
    public List<OrderEntity> convertUuidToEntity(List<UUID> uuidList) {
        return orderRepository.findAllByUuidIn(uuidList);
    }

    @Override
    @Cacheable(value = "orders")
    public Page<Order> findAll(Integer page) {
        Integer pageNumber = validatePage(page);
        Page<OrderEntity> orderPage = orderRepository
                .findAll(PageRequest.of(
                        pageNumber,
                        PageSize.EXTRA_SMALL.getValue()));
        List<Order> orders = orderPage
                .getContent()
                .stream()
                .map(orderEntityMapper::mapToApiModel)
                .toList();
        Pageable pageable = PageRequest.of(page, PageSize.EXTRA_SMALL.getValue());

        return new PageImpl<>(orders, pageable, orderPage.getTotalElements());
    }

    public List<Order> findAllByPreferredDeliveryDateBetween(LocalDate startDate, LocalDate endDate) {
        return orderRepository
                .findAllByPreferredDeliveryDateBetween(startDate, endDate)
                .stream()
                .map(orderEntityMapper::mapToApiModel)
                .toList();
    }

}
