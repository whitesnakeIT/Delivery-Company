package com.kapusniak.tomasz.mapper;

import com.kapusniak.tomasz.entity.CourierEntity;
import com.kapusniak.tomasz.openapi.model.Courier;
import com.kapusniak.tomasz.service.DeliveryService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.UUID;

@Mapper(
        componentModel = "spring",
        uses = {
                UuidMapper.class
        }
)
public abstract class CourierEntityMapper {

    @Autowired
    @Lazy
    private DeliveryService deliveryService;

    @Mapping(
            target = "uuid",
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(
            target = "version",
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(
            target = "deliveryList",
            ignore = true
    )
    public abstract CourierEntity mapToEntity(Courier courier);

    public abstract Courier mapToApiModel(CourierEntity courierEntity);

    @AfterMapping
    public void convertUuidToDeliveryEntity(@MappingTarget CourierEntity courierEntity, Courier courier) {
        List<UUID> deliveries = courier.getDeliveryList();
        courierEntity.setDeliveryList(deliveryService.convertUuidToEntity(deliveries));
    }
}
