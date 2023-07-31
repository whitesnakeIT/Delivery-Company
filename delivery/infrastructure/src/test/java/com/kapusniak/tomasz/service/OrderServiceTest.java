package com.kapusniak.tomasz.service;

import com.kapusniak.tomasz.entity.CustomerEntity;
import com.kapusniak.tomasz.entity.OrderEntity;
import com.kapusniak.tomasz.mapper.OrderEntityMapper;
import com.kapusniak.tomasz.openapi.model.Order;
import com.kapusniak.tomasz.openapi.model.PackageSize;
import com.kapusniak.tomasz.openapi.model.PackageType;
import com.kapusniak.tomasz.repository.PageSize;
import com.kapusniak.tomasz.repository.jpa.OrderJpaRepository;
import com.kapusniak.tomasz.service.model.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.kapusniak.tomasz.openapi.model.PackageSize.EXTRA_LARGE;
import static com.kapusniak.tomasz.openapi.model.PackageSize.LARGE;
import static com.kapusniak.tomasz.openapi.model.PackageType.DOCUMENT;
import static com.kapusniak.tomasz.openapi.model.PackageType.PARCEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class OrderServiceTest {

    private static final UUID ORDER_UUID_1 = UUID.fromString("29755321-c483-4a12-9f64-30a132038b70");
    private static final UUID CUSTOMER_UUID = UUID.randomUUID();
    private static final Integer PAGE_NUMBER = 0;
    private static final PageRequest PAGEABLE = PageRequest.of(PAGE_NUMBER, PageSize.EXTRA_SMALL.getValue());


    @Mock
    private OrderJpaRepository orderRepository;
    @Mock
    private OrderEntityMapper orderEntityMapper;

    @InjectMocks
    private OrderService orderService;

    private Order prepareOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setPreferredDeliveryDate(LocalDate.of(2023, 5, 4));
        order.setPackageSize(LARGE);
        order.setPackageType(DOCUMENT);
        order.setSenderAddress("new sender address");
        order.setReceiverAddress("new receiver address");
        order.setUuid(ORDER_UUID_1);

        order.setCustomer(CUSTOMER_UUID);

        return order;
    }

    private OrderEntity prepareOrderEntity() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(1L);
        orderEntity.setPreferredDeliveryDate(LocalDate.of(2023, 5, 4));
        orderEntity.setPackageSize(LARGE);
        orderEntity.setPackageType(DOCUMENT);
        orderEntity.setSenderAddress("new sender address");
        orderEntity.setReceiverAddress("new receiver address");
        orderEntity.setUuid(ORDER_UUID_1);

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setUuid(CUSTOMER_UUID);
        orderEntity.setCustomer(customerEntity);

        return orderEntity;
    }

    private Page<OrderEntity> prepareOrderEntityList() {
        List<OrderEntity> orderEntities = List.of(prepareOrderEntity(), prepareOrderEntity());

        return new PageImpl<>(orderEntities);

    }

    @BeforeEach
    void setup() {

        when(orderEntityMapper
                .mapToEntity(any(Order.class)))
                .thenReturn(prepareOrderEntity());
        when(orderEntityMapper
                .mapToApiModel(any(OrderEntity.class)))
                .thenReturn(prepareOrder());

    }

    @Test
    @DisplayName("should correctly save an Order entity exactly once")
    void save() {

        // given
        Order order = prepareOrder();

        // and
        when(orderRepository
                .save(any(OrderEntity.class)))
                .thenReturn(prepareOrderEntity());

        // when
        Order result = orderService.save(order);

        // then
        then(orderRepository)
                .should(times(1))
                .save(orderEntityMapper.mapToEntity(order));

        // verify
        assertThat(order).isEqualTo(result);
    }

    @Test
    @DisplayName("should throw an exception when Order entity is null and check method invocations")
    void saveNull() {

        // given
        Order order = null;

        // when
        Throwable throwable = catchThrowable(() ->
                orderService.save(order));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Saving order failed. Order is null.");

        // verify
        then(orderRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("should return page of orders with correct size and check method invocations")
    void findAll() {

        // given
        Page<OrderEntity> orderEntityPage = prepareOrderEntityList();

        given(orderRepository.findAll(any(Pageable.class)))
                .willReturn(orderEntityPage);

        // when
        Page<Order> orderPage = orderService.findAll(PAGE_NUMBER);

        // then
        assertThat(orderPage.getContent().size())
                .isEqualTo(2);

        // verify
        then(orderRepository)
                .should(times(1))
                .findAll(PAGEABLE);
    }

    @Test
    @DisplayName("should return order based on order uuid")
    void findByUuid() {
        // given
        OrderEntity orderEntity = prepareOrderEntity();
        UUID orderUuid = ORDER_UUID_1;

        // and
        given(orderRepository.findByUuid(
                any(UUID.class)))
                .willReturn(Optional.of(orderEntity));


        // when
        Order orderByUuid = orderService.findByUuid(orderUuid);

        // then
        assertThat(orderByUuid)
                .isNotNull();
    }

    @Test
    @DisplayName("should throw an exception when order uuid is null")
    void findByUuidNull() {
        // given
        UUID orderUuid = null;

        // when
        Throwable throwable = catchThrowable(() ->
                orderService.findByUuid(orderUuid));

        // then
        assertThat(throwable)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Searching for order failed. Order uuid is null.");

        // verify
        then(orderRepository)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("should return list of orders with correct size based by package type")
    void findByPackageType() {

        // given
        Page<OrderEntity> orderEntityList = prepareOrderEntityList();
        PackageType packageType = DOCUMENT;

        // and
        given(orderRepository.findByPackageType(any(), any(Pageable.class)))
                .willReturn(orderEntityList);

        // when
        Page<Order> ordersByPackageType = orderService.findByPackageType(packageType, PAGE_NUMBER);

        // then
        assertThat(ordersByPackageType.getContent().size())
                .isGreaterThan(0);
        // verify
        then(orderRepository)
                .should(times(1))
                .findByPackageType(packageType, PAGEABLE);
    }

    @Test
    @DisplayName("should throw an exception when package type is null")
    void findByPackageTypeNull() {

        // given
        PackageType packageTypeNull = null;

        // when
        Throwable throwable = catchThrowable(() ->
                orderService.findByPackageType(packageTypeNull, PAGE_NUMBER));

        // then
        assertThat(throwable)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Searching for order failed. Package type is null.");
    }

    @Test
    @DisplayName("should return list of orders with correct size based by package size")
    void findByPackageSize() {

        // given
        Page<OrderEntity> orderEntityList = prepareOrderEntityList();
        PackageSize packageSize = EXTRA_LARGE;

        // and
        given(orderRepository.findByPackageSize(any(), any(Pageable.class)))
                .willReturn(orderEntityList);

        // when
        Page<Order> ordersByPackageSize = orderService.findByPackageSize(packageSize, PAGE_NUMBER);

        // then
        assertThat(ordersByPackageSize.getContent().size())
                .isGreaterThan(0);

        // verify
        then(orderRepository)
                .should(times(1))
                .findByPackageSize(packageSize, PAGEABLE);
    }

    @Test
    @DisplayName("should throw an exception when package size is null")
    void findByPackageSizeNull() {

        // given
        PackageSize packageSizeNull = null;

        // when
        Throwable throwable = catchThrowable(() ->
                orderService.findByPackageSize(packageSizeNull, PAGE_NUMBER));

        // then
        assertThat(throwable)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Searching for order failed. Package size is null.");
    }

    @Test
    @DisplayName("should return list of all customer orders based on customer uuid")
    void findAllByCustomerUuid() {

        // given
        Page<OrderEntity> orderEntityList = prepareOrderEntityList();
        UUID customerUuid = ORDER_UUID_1;

        // and
        given(orderRepository.findAllByCustomerUuid(any(), any(Pageable.class)))
                .willReturn(orderEntityList);

        // when
        Page<Order> ordersByCustomerUuid = orderService.findAllByCustomerUuid(customerUuid, PAGE_NUMBER);

        // then
        assertThat(ordersByCustomerUuid.getContent().size())
                .isGreaterThan(0);

        // verify
        then(orderRepository)
                .should(times(1))
                .findAllByCustomerUuid(customerUuid, PAGEABLE);
    }

    @Test
    @DisplayName("should throw an exception when customer uuid is null")
    void findAllByCustomerIdNull() {

        // given
        UUID customerUuid = null;

        // when
        Throwable throwable = catchThrowable(() ->
                orderService.findAllByCustomerUuid(customerUuid, PAGE_NUMBER));

        // then
        assertThat(throwable)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Searching for customer orders failed. Customer uuid is null.");
    }

    @Test
    @DisplayName("should delete an order based on order uuid")
    void delete() {

        // given
        OrderEntity orderEntity = prepareOrderEntity();
        UUID orderUuid = ORDER_UUID_1;

        // and
        given(orderRepository.findByUuid(
                any(UUID.class)))
                .willReturn(Optional.of(orderEntity));

        // when
        orderService.delete(orderUuid);

        // then
        Order order = prepareOrder();
        then(orderRepository)
                .should(times(1))
                .delete(orderEntityMapper.mapToEntity(order));
    }

    @Test
    @DisplayName("should throw an exception when order uuid is null")
    void deleteNull() {

        // given
        UUID orderUuid = null;

        // when
        Throwable throwable = catchThrowable(() ->
                orderService.delete(orderUuid));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Deleting order failed. Order uuid is null.");
    }

    @Test
    @DisplayName("should throw an exception when uuid is null")
    void updateNullUuid() {
        // given
        Order order = new Order();
        UUID orderUuid = null;

        // when
        Throwable throwable = catchThrowable(() ->
                orderService.update(orderUuid, order));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Updating order failed. Order uuid is null.");
    }

    @Test
    @DisplayName("should throw an exception when order is null")
    void updateNullOrder() {
        // given
        UUID orderUuid = ORDER_UUID_1;
        Order order = null;

        // when
        Throwable thrown = catchThrowable(() ->
                orderService.update(orderUuid, order));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Updating order failed. Order is null.");
    }

    @Test
    @DisplayName("should throw an exception when newOrder's uuid doesn't match orderFromDb's uuid")
    void updateUuidMissMatch() {
        // given
        UUID oldUuid = ORDER_UUID_1;

        Order newOrder = new Order();
        UUID newUuid = CUSTOMER_UUID;
        newOrder.setUuid(newUuid);

        OrderEntity orderEntity = prepareOrderEntity();

        // and
        when(orderRepository.findByUuid(any(UUID.class)))
                .thenReturn(Optional.of(orderEntity));
        // when
        Throwable throwable = catchThrowable(() ->
                orderService.update(oldUuid, newOrder));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Updating order fields failed. Different uuid's");
    }

    @Test
    @DisplayName("should correctly update order when valid uuid and order are provided")
    void shouldUpdateOrder() {
        // given
        OrderEntity orderEntity = prepareOrderEntity();
        UUID orderUuid = ORDER_UUID_1;
        Order changedOrder = prepareOrderForEdit();
        OrderEntity changedOrderEntity = prepareOrderEntityForEdit();

        // and
        when(orderRepository.findByUuid(any(UUID.class)))
                .thenReturn(Optional.of(orderEntity));
        when(orderRepository.save(any(OrderEntity.class)))
                .thenReturn(changedOrderEntity);

        //and
        when(orderEntityMapper
                .mapToApiModel(changedOrderEntity))
                .thenReturn(changedOrder);

        // when
        Order updatedOrder = orderService.update(orderUuid, changedOrder);

        // then
        assertThat(updatedOrder).isNotNull();
        assertThat(updatedOrder.getUuid()).isEqualTo(changedOrder.getUuid());
        assertThat(updatedOrder.getId()).isEqualTo(changedOrder.getId());
        assertThat(updatedOrder.getSenderAddress()).isEqualTo(changedOrder.getSenderAddress());
        assertThat(updatedOrder.getReceiverAddress()).isEqualTo(changedOrder.getReceiverAddress());
        assertThat(updatedOrder.getPackageSize()).isEqualTo(changedOrder.getPackageSize());
        assertThat(updatedOrder.getPackageType()).isEqualTo(changedOrder.getPackageType());
        assertThat(updatedOrder.getPreferredDeliveryDate()).isEqualTo(changedOrder.getPreferredDeliveryDate());

        assertThat(updatedOrder.getCustomer()).isEqualTo(changedOrder.getCustomer());

        // verify
        then(orderRepository)
                .should(times(1))
                .save(orderEntity);
    }

    private Order prepareOrderForEdit() {
        UUID orderUuid = ORDER_UUID_1;
        String newSenderAddress = "newSenderAddress";
        String newReceiverAddress = "newReceiverAddress";
        PackageType newPackageType = PARCEL;
        PackageSize newPackageSize = EXTRA_LARGE;
        LocalDate newPreferredDeliveryDate = LocalDate.of(2023, 5, 28);

        UUID newCustomerUuid = CUSTOMER_UUID;
        Order changedOrder = new Order();
        changedOrder.setUuid(orderUuid);
        changedOrder.setSenderAddress(newSenderAddress);
        changedOrder.setReceiverAddress(newReceiverAddress);
        changedOrder.setPackageSize(newPackageSize);
        changedOrder.setPackageType(newPackageType);
        changedOrder.setPreferredDeliveryDate(newPreferredDeliveryDate);

//        Customer customer = new Customer();
//        customer.setUuid(newCustomerUuid);
        changedOrder.setCustomer(UUID.randomUUID());

        return changedOrder;
    }

    private OrderEntity prepareOrderEntityForEdit() {
        UUID orderUuid = ORDER_UUID_1;
        String newSenderAddress = "newSenderAddress";
        String newReceiverAddress = "newReceiverAddress";
        PackageType newPackageType = PARCEL;
        PackageSize newPackageSize = EXTRA_LARGE;
        LocalDate newPreferredDeliveryDate = LocalDate.of(2023, 5, 28);

        UUID newCustomerUuid = UUID.fromString("28f60dc1-993a-4d08-ac54-850a1fefb6a3");
        OrderEntity changedOrderEntity = new OrderEntity();
        changedOrderEntity.setUuid(orderUuid);
        changedOrderEntity.setSenderAddress(newSenderAddress);
        changedOrderEntity.setReceiverAddress(newReceiverAddress);
        changedOrderEntity.setPackageSize(newPackageSize);
        changedOrderEntity.setPackageType(newPackageType);
        changedOrderEntity.setPreferredDeliveryDate(newPreferredDeliveryDate);

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setUuid(newCustomerUuid);
        changedOrderEntity.setCustomer(customerEntity);

        return changedOrderEntity;
    }
}