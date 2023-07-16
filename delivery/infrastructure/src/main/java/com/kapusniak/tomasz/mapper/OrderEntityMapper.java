package com.kapusniak.tomasz.mapper;

import com.kapusniak.tomasz.entity.OrderEntity;
import com.kapusniak.tomasz.openapi.model.Order;
import com.kapusniak.tomasz.service.CustomerService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(
        componentModel = "spring",
        uses = {
                UuidMapper.class
        }
)
public abstract class OrderEntityMapper {

    @Autowired
    private CustomerService customerService;

    @Mapping(
            target = "uuid",
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(
            target = "version",
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(
            target = "customer",
            ignore = true
    )
    public abstract OrderEntity mapToEntity(Order order);

    public abstract Order mapToApiModel(OrderEntity orderEntity);

    @AfterMapping
    public void convertUuidToCustomerEntity(@MappingTarget OrderEntity orderEntity, Order order) {
        UUID customer = order.getCustomer();
        orderEntity.setCustomer(customerService.convertUuidToEntity(customer));
    }
}
