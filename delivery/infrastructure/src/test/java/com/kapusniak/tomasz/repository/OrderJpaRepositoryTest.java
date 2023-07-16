package com.kapusniak.tomasz.repository;

import com.kapusniak.tomasz.entity.CustomerEntity;
import com.kapusniak.tomasz.entity.OrderEntity;
import com.kapusniak.tomasz.openapi.model.PackageSize;
import com.kapusniak.tomasz.openapi.model.PackageType;
import com.kapusniak.tomasz.repository.jpa.CustomerJpaRepository;
import com.kapusniak.tomasz.repository.jpa.DeliveryJpaRepository;
import com.kapusniak.tomasz.repository.jpa.OrderJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.kapusniak.tomasz.openapi.model.PackageSize.LARGE;
import static com.kapusniak.tomasz.openapi.model.PackageType.DOCUMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "/application-test.properties")
@SqlGroup(
        @Sql(
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
                scripts = {
                        "classpath:h2-scripts/cleanup.sql",
                        "classpath:h2-scripts/insert-data.sql"
                }
        )
)
class OrderJpaRepositoryTest {

    @Autowired
    private OrderJpaRepository orderRepository;

    @Autowired
    private DeliveryJpaRepository deliveryRepository;

    @Autowired
    private CustomerJpaRepository customerRepository;

    CustomerEntity prepareCustomerEntity() {
        Optional<CustomerEntity> customerEntity = customerRepository.findById(1L);


        return customerEntity.orElse(null);
    }

    OrderEntity prepareOrderEntity() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setPreferredDeliveryDate(LocalDate.of(2023, 5, 4));
        orderEntity.setPackageSize(LARGE);
        orderEntity.setPackageType(DOCUMENT);
        orderEntity.setSenderAddress("test sender address");
        orderEntity.setReceiverAddress("test receiver address");

        CustomerEntity customerEntity = prepareCustomerEntity();

        orderEntity.setCustomer(customerEntity);

        return orderEntity;
    }

    @Test
    @DisplayName("should return list of orders with correct size based on package type")
    void findByPackageType() {

        //when
        List<OrderEntity> ordersByPackageType = orderRepository.findByPackageType(PackageType.DOCUMENT);

        //then
        then(ordersByPackageType.size())
                .isGreaterThan(0);

        then(ordersByPackageType)
                .extracting(OrderEntity::getPackageType)
                .containsOnly(PackageType.DOCUMENT);
    }

    @Test
    @DisplayName("should return empty list of orders based on package type")
    void findByPackageTypeEmpty() {

        // given
        deliveryRepository.deleteAll();
        orderRepository.deleteAll();

        // when
        List<OrderEntity> ordersByPackageType = orderRepository.findByPackageType(PackageType.DOCUMENT);

        // then
        then(ordersByPackageType)
                .isEmpty();
    }

    @Test
    @DisplayName("should return list of orders with correct size based on package size")
    void findByPackageSize() {

        //when
        List<OrderEntity> ordersByPackageSize = orderRepository.findByPackageSize(PackageSize.SMALL);

        //then
        then(ordersByPackageSize.size())
                .isGreaterThan(0);

        then(ordersByPackageSize)
                .extracting(OrderEntity::getPackageSize)
                .containsOnly(PackageSize.SMALL);
    }

    @Test
    @DisplayName("should return empty list of orders based on package size")
    void findByPackageSizeEmpty() {

        // given
        deliveryRepository.deleteAll();
        orderRepository.deleteAll();

        // when
        List<OrderEntity> ordersByPackageSize = orderRepository.findByPackageSize(PackageSize.EXTRA_LARGE);

        // then
        then(ordersByPackageSize)
                .isEmpty();
    }

    @Test
    @DisplayName("should return list of orders with correct size based on customer uuid")
    void findAllByCustomerUuidExisting() {
        // given
        CustomerEntity customerEntity = prepareCustomerEntity();

        // when
        List<OrderEntity> ordersByCustomerUuid = orderRepository.findAllByCustomerUuid(customerEntity.getUuid());

        // then
        then(ordersByCustomerUuid.size())
                .isGreaterThan(0);
    }

    @Test
    @DisplayName("should return empty list of orders based on customer uuid")
    void findAllByCustomerUuidNotExisting() {

        // given
        deliveryRepository.deleteAll();
        orderRepository.deleteAll();

        // and
        CustomerEntity customerEntity = prepareCustomerEntity();

        // when
        List<OrderEntity> ordersByCustomerUuid = orderRepository.findAllByCustomerUuid(customerEntity.getUuid());

        // then
        then(ordersByCustomerUuid)
                .isEmpty();
    }

    @Test
    @DisplayName("should correctly set version number after saving " +
            "or editing an Order entity")
    @Transactional(propagation = NOT_SUPPORTED)
    void versionChecking() {
        // given
        OrderEntity orderEntity = prepareOrderEntity();

        // when
        OrderEntity savedOrder = orderRepository.save(orderEntity);

        // and
        savedOrder.setSenderAddress("new sender address");
        savedOrder.setReceiverAddress("new receiver address");

        OrderEntity editedOrder = orderRepository.save(savedOrder);

        // then
        assertThat(orderEntity.getVersion()).isEqualTo(0);

        assertThat(savedOrder.getUuid()).isNotNull();
        assertThat(savedOrder.getVersion()).isEqualTo(0);

        assertThat(editedOrder.getUuid()).isEqualTo(savedOrder.getUuid());
        assertThat(editedOrder.getVersion()).isEqualTo(savedOrder.getVersion() + 1);
    }
}