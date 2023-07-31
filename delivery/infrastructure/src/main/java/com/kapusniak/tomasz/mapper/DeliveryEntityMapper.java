package com.kapusniak.tomasz.mapper;

import com.kapusniak.tomasz.entity.DeliveryEntity;
import com.kapusniak.tomasz.openapi.model.Delivery;
import com.kapusniak.tomasz.service.model.CourierService;
import com.kapusniak.tomasz.service.model.OrderService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;


@Mapper(
        componentModel = "spring",
        uses = {
                TimeMapper.class,
                UuidMapper.class
        }
)
public abstract class DeliveryEntityMapper {

    @Autowired
    private CourierService courierService;

    @Autowired
    private OrderService orderService;

    @Mapping(
            target = "uuid",
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(
            target = "version",
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(
            target = "courier",
            ignore = true
    )
    @Mapping(
            target = "order",
            ignore = true
    )
    public abstract DeliveryEntity mapToEntity(Delivery delivery);

    public abstract Delivery mapToApiModel(DeliveryEntity deliveryEntity);

    @AfterMapping
    public void convertUuidToDeliveryEntity(@MappingTarget DeliveryEntity deliveryEntity, Delivery delivery) {
        UUID order = delivery.getOrder();
        deliveryEntity.setOrder(orderService.convertUuidToEntity(order));
        UUID courier = delivery.getCourier();
        deliveryEntity.setCourier(courierService.convertUuidToEntity(courier));
    }
}
