package com.kapusniak.tomasz.repository;

import com.kapusniak.tomasz.entity.CourierEntity;
import com.kapusniak.tomasz.entity.DeliveryEntity;
import com.kapusniak.tomasz.entity.OrderEntity;
import com.kapusniak.tomasz.openapi.model.DeliveryStatus;
import com.kapusniak.tomasz.repository.jpa.CourierJpaRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
public class DeliveryJpaRepositoryTest {

    @Autowired
    private DeliveryJpaRepository deliveryRepository;

    @Autowired
    private CourierJpaRepository courierRepository;

    @Autowired
    private OrderJpaRepository orderRepository;

    DeliveryEntity prepareDeliveryEntity() {
        DeliveryEntity deliveryEntity = new DeliveryEntity();
        deliveryEntity.setPrice(BigDecimal.valueOf(40.00));
        deliveryEntity.setDeliveryTime(LocalDateTime.of(2020, 6, 5, 20, 20, 0));
        deliveryEntity.setDeliveryStatus(DeliveryStatus.CREATED);

        Optional<CourierEntity> courierEntity = courierRepository.findById(1L);
        Optional<OrderEntity> orderEntity = orderRepository.findById(1L);

        courierEntity.ifPresent(deliveryEntity::setCourier);
        orderEntity.ifPresent(deliveryEntity::setOrder);


        return deliveryEntity;
    }

    @Test
    @DisplayName("should correctly set version number after saving " +
            "or editing an Order entity")
    @Transactional(propagation = NOT_SUPPORTED)
    void versionChecking() {
        // given
        DeliveryEntity deliveryEntity = prepareDeliveryEntity();

        // when
        DeliveryEntity savedDelivery = deliveryRepository.save(deliveryEntity);

        // and
        savedDelivery.setPrice(BigDecimal.valueOf(150.00));

        DeliveryEntity editedDelivery = deliveryRepository.save(savedDelivery);

        // then
        assertThat(deliveryEntity.getVersion()).isEqualTo(0);

        assertThat(savedDelivery.getUuid()).isNotNull();
        assertThat(savedDelivery.getVersion()).isEqualTo(0);

        assertThat(editedDelivery.getUuid()).isEqualTo(savedDelivery.getUuid());
        assertThat(editedDelivery.getVersion()).isEqualTo(savedDelivery.getVersion() + 1);
    }
}