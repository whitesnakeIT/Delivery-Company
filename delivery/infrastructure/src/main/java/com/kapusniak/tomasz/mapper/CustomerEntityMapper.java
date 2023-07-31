package com.kapusniak.tomasz.mapper;

import com.kapusniak.tomasz.entity.CustomerEntity;
import com.kapusniak.tomasz.openapi.model.Customer;
import com.kapusniak.tomasz.service.model.OrderService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.UUID;

@Mapper(
        componentModel = "spring",
        uses = {
                UuidMapper.class,
        }
)
public abstract class CustomerEntityMapper {

    @Autowired
    @Lazy
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
            target = "orders",
            ignore = true
    )
    public abstract CustomerEntity mapToEntity(Customer customer);

    public abstract Customer mapToApiModel(CustomerEntity customerEntity);


    @AfterMapping
    public void convertUuidToOrderEntity(@MappingTarget CustomerEntity customerEntity, Customer customer) {
        List<UUID> orders = customer.getOrders();
        customerEntity.setOrders(orderService.convertUuidToEntity(orders));
    }
}
