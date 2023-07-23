package com.kapusniak.tomasz.mapper;

import com.kapusniak.tomasz.entity.CustomerEntity;
import com.kapusniak.tomasz.entity.OrderEntity;
import com.kapusniak.tomasz.openapi.model.Customer;
import com.kapusniak.tomasz.service.model.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.anyList;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class CustomerEntityMapperTest {

    public static final UUID CUSTOMER_UUID = UUID.fromString("28f60dc1-993a-4d08-ac54-850a1fefb6a3");
    private static final UUID ORDER_UUID_1 = UUID.randomUUID();
    private static final UUID ORDER_UUID_2 = UUID.randomUUID();

    @Autowired
    private CustomerEntityMapperImpl customerEntityMapper;

    @MockBean
    private OrderService orderService;

    public List<UUID> prepareOrderList() {

        return List.of(ORDER_UUID_1, ORDER_UUID_2);
    }

    public Customer prepareCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("testFirstName");
        customer.setLastName("testLastName");
        customer.setEmail("test@test.com");
        customer.setUuid(CUSTOMER_UUID);
        customer.setVersion(0L);
        customer.setOrders(prepareOrderList());

        return customer;
    }

    public List<OrderEntity> prepareOrderEntityList() {
        OrderEntity orderEntity1 = new OrderEntity();
        OrderEntity orderEntity2 = new OrderEntity();

        orderEntity1.setUuid(ORDER_UUID_1);
        orderEntity2.setUuid(ORDER_UUID_2);

        return List.of(orderEntity1, orderEntity2);
    }

    public CustomerEntity prepareCustomerEntity() {
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setId(1L);
        customerEntity.setFirstName("testFirstName");
        customerEntity.setLastName("testLastName");
        customerEntity.setEmail("test@test.com");
        customerEntity.setUuid(CUSTOMER_UUID);
        customerEntity.setVersion(0L);

        List<OrderEntity> orderEntities = prepareOrderEntityList();
        orderEntities.forEach(orderEntity -> orderEntity.setCustomer(customerEntity));
        customerEntity.setOrders(orderEntities);

        return customerEntity;
    }

    @Test
    @DisplayName("should map from Customer to CustomerEntity")
    void mapToEntity() {
        // given
        Customer customer = prepareCustomer();

        // and
        when(orderService.convertUuidToEntity(anyList()))
                .thenReturn(prepareOrderEntityList());

        // when
        CustomerEntity customerEntity = customerEntityMapper.mapToEntity(customer);

        // then
        assertThat(customerEntity.getUuid()).isEqualTo(customer.getUuid());
        assertThat(customerEntity.getFirstName()).isEqualTo(customer.getFirstName());
        assertThat(customerEntity.getLastName()).isEqualTo(customer.getLastName());
        assertThat(customerEntity.getEmail()).isEqualTo(customer.getEmail());

        assertThat(customerEntity.getOrders()).isNotNull();
        assertThat(customerEntity.getUuid()).isNotNull();

        assertThat(customerEntity.getOrders().get(0).getUuid()).isEqualTo(customer.getOrders().get(0));
        assertThat(customerEntity.getOrders().get(1).getUuid()).isEqualTo(customer.getOrders().get(1));

        // verify
        verify(orderService, times(1))
                .convertUuidToEntity(prepareOrderList());
    }

    @Test
    @DisplayName("should map from CustomerEntity to Customer")
    void mapToApiModel() {
        // given
        CustomerEntity customerEntity = prepareCustomerEntity();

        // when
        Customer customer = customerEntityMapper.mapToApiModel(customerEntity);

        // then
        assertThat(customer.getUuid()).isEqualTo(customerEntity.getUuid());
        assertThat(customer.getFirstName()).isEqualTo(customerEntity.getFirstName());
        assertThat(customer.getLastName()).isEqualTo(customerEntity.getLastName());
        assertThat(customer.getEmail()).isEqualTo(customerEntity.getEmail());

        assertThat(customer.getOrders()).isNotNull();
        assertThat(customer.getOrders().get(0)).isEqualTo(customerEntity.getOrders().get(0).getUuid());
        assertThat(customer.getOrders().get(1)).isEqualTo(customerEntity.getOrders().get(1).getUuid());
    }
}