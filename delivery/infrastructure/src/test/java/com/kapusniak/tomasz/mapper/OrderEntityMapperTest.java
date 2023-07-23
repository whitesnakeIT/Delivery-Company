package com.kapusniak.tomasz.mapper;

import com.kapusniak.tomasz.entity.CustomerEntity;
import com.kapusniak.tomasz.entity.OrderEntity;
import com.kapusniak.tomasz.openapi.model.Order;
import com.kapusniak.tomasz.openapi.model.PackageSize;
import com.kapusniak.tomasz.openapi.model.PackageType;
import com.kapusniak.tomasz.service.model.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderEntityMapperTest {

    public static final UUID ORDER_UUID = UUID.fromString("29755321-c483-4a12-9f64-30a132038b70");
    public static final UUID CUSTOMER_UUID = UUID.randomUUID();

    @Autowired
    private OrderEntityMapper orderEntityMapper;

    @MockBean
    private CustomerService customerService;


    public CustomerEntity prepareCustomerEntity() {
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setId(1L);
        customerEntity.setEmail("testEmail");
        customerEntity.setFirstName("testFirstName");
        customerEntity.setLastName("testLastName");
        customerEntity.setVersion(0L);
        customerEntity.setUuid(CUSTOMER_UUID);

        return customerEntity;
    }

    public Order prepareOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setSenderAddress("testSenderAddress");
        order.setReceiverAddress("testReceiverAddress");
        order.setPackageType(PackageType.PARCEL);
        order.setPackageSize(PackageSize.SMALL);
        order.setCustomer(CUSTOMER_UUID);
        order.setUuid(ORDER_UUID);
        order.setVersion(0L);

        return order;
    }

    public OrderEntity prepareOrderEntity() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(1L);
        orderEntity.setSenderAddress("testSenderAddress");
        orderEntity.setReceiverAddress("testReceiverAddress");
        orderEntity.setPackageType(PackageType.PARCEL);
        orderEntity.setPackageSize(PackageSize.SMALL);
        CustomerEntity customerEntity = prepareCustomerEntity();
        orderEntity.setCustomer(customerEntity);
        orderEntity.setUuid(ORDER_UUID);
        orderEntity.setVersion(0L);

        return orderEntity;
    }

    @Test
    @DisplayName("should map Order to OrderEntity")
    public void mapToEntity() {
        // given
        Order order = prepareOrder();

        // and
        when(customerService.convertUuidToEntity(any(UUID.class)))
                .thenReturn(prepareCustomerEntity());

        // when
        OrderEntity orderEntity = orderEntityMapper.mapToEntity(order);

        // then
        assertThat(orderEntity.getUuid()).isEqualTo(order.getUuid());
        assertThat(orderEntity.getSenderAddress()).isEqualTo(order.getSenderAddress());
        assertThat(orderEntity.getReceiverAddress()).isEqualTo(order.getReceiverAddress());
        assertThat(orderEntity.getPackageType()).isEqualTo(order.getPackageType());
        assertThat(orderEntity.getPackageSize()).isEqualTo(order.getPackageSize());
        assertThat(orderEntity.getUuid()).isNotNull();

        assertThat(orderEntity.getCustomer()).isNotNull();
        assertThat(orderEntity.getCustomer().getUuid()).isEqualTo(order.getCustomer());

        // verify
        verify(customerService, times(1))
                .convertUuidToEntity(CUSTOMER_UUID);
    }

    @Test
    @DisplayName("should map OrderEntity to Order")
    public void mapToApiModel() {
        // given
        OrderEntity orderEntity = prepareOrderEntity();

        // when
        Order order = orderEntityMapper.mapToApiModel(orderEntity);

        // then
        assertThat(order.getUuid()).isEqualTo(orderEntity.getUuid());
        assertThat(order.getSenderAddress()).isEqualTo(orderEntity.getSenderAddress());
        assertThat(order.getReceiverAddress()).isEqualTo(orderEntity.getReceiverAddress());
        assertThat(order.getPackageType()).isEqualTo(orderEntity.getPackageType());
        assertThat(order.getPackageSize()).isEqualTo(orderEntity.getPackageSize());

        assertThat(order.getCustomer()).isNotNull();
        assertThat(order.getCustomer()).isEqualTo(orderEntity.getCustomer().getUuid());
    }

}
