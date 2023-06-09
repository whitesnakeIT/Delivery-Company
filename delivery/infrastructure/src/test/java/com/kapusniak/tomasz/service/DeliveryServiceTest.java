package com.kapusniak.tomasz.service;

import com.kapusniak.tomasz.entity.CourierEntity;
import com.kapusniak.tomasz.entity.DeliveryEntity;
import com.kapusniak.tomasz.entity.OrderEntity;
import com.kapusniak.tomasz.mapper.DeliveryEntityMapper;
import com.kapusniak.tomasz.openapi.model.Courier;
import com.kapusniak.tomasz.openapi.model.Delivery;
import com.kapusniak.tomasz.openapi.model.DeliveryStatus;
import com.kapusniak.tomasz.openapi.model.Order;
import com.kapusniak.tomasz.repository.jpa.DeliveryJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.kapusniak.tomasz.openapi.model.DeliveryStatus.CREATED;
import static com.kapusniak.tomasz.openapi.model.DeliveryStatus.IN_TRANSIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class DeliveryServiceTest {
    private static final UUID DELIVERY_UUID_1 = UUID.fromString("fe362772-17c3-4547-b559-ceb13e164e6f");
    @Mock
    private DeliveryJpaRepository deliveryRepository;

    @Mock
    private DeliveryEntityMapper deliveryEntityMapper;
    @InjectMocks
    private DeliveryService deliveryService;

    private Delivery prepareDelivery() {
        Delivery delivery = new Delivery();
        delivery.setDeliveryTime(LocalDateTime
                .of(2023, 5, 3,
                        12, 0, 0).atOffset(ZoneOffset.UTC));
        delivery.setDeliveryStatus(CREATED);
        delivery.setPrice(50.00);
        delivery.setUuid(DELIVERY_UUID_1);

        return delivery;
    }

    private DeliveryEntity prepareDeliveryEntity() {
        DeliveryEntity deliveryEntity = new DeliveryEntity();
        deliveryEntity.setId(1L);
        deliveryEntity.setDeliveryTime(LocalDateTime
                .of(2023, 5, 3,
                        12, 0, 0));
        deliveryEntity.setDeliveryStatus(CREATED);
        deliveryEntity.setPrice(BigDecimal.valueOf(50.00));
        deliveryEntity.setUuid(DELIVERY_UUID_1);


        return deliveryEntity;
    }

    private List<Delivery> prepareDeliveryList() {

        return List.of(prepareDelivery(), prepareDelivery());
    }

    private List<DeliveryEntity> prepareDeliveryEntityList() {

        return List.of(prepareDeliveryEntity(), prepareDeliveryEntity());
    }

    @BeforeEach
    void setup() {

        when(deliveryEntityMapper
                .mapToEntity(any(Delivery.class)))
                .thenReturn(prepareDeliveryEntity());
        when(deliveryEntityMapper
                .mapToApiModel(any(DeliveryEntity.class)))
                .thenReturn(prepareDelivery());
    }

    @Test
    @DisplayName("should correctly save an Delivery entity exactly once")
    void save() {

        // given
        DeliveryEntity deliveryEntity = prepareDeliveryEntity();
        Delivery delivery = prepareDelivery();

        // and
        when(deliveryRepository
                .save(any(DeliveryEntity.class)))
                .thenReturn(deliveryEntity);

        // when
        Delivery result = deliveryService.save(delivery);

        // then
        then(deliveryRepository)
                .should(times(1))
                .save(deliveryEntityMapper.mapToEntity(delivery));

        // verify
        assertThat(delivery).isEqualTo(result);

    }

    @Test
    @DisplayName("should throw an exception when Delivery entity is null and check method invocations")
    void saveNull() {

        // given
        Delivery delivery = null;

        // when
        Throwable throwable = catchThrowable(() ->
                deliveryService.save(delivery));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Saving delivery failed. Delivery is null.");

        // verify
        then(deliveryRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("should return list of all delivery with correct size and check method invocations")
    void findAll() {

        // given
        List<DeliveryEntity> deliveryEntityList = prepareDeliveryEntityList();

        // and
        given(deliveryRepository.findAll())
                .willReturn(deliveryEntityList);

        // when
        List<Delivery> allDelivery = deliveryService.findAll();

        // then
        assertThat(allDelivery.size())
                .isEqualTo(2);

        // verify
        then(deliveryRepository)
                .should(times(1))
                .findAll();
    }

    @Test
    @DisplayName("should return delivery based on delivery uuid")
    void findByUuid() {

        // given
        DeliveryEntity deliveryEntity = prepareDeliveryEntity();
        UUID deliveryUuid = DELIVERY_UUID_1;

        // and
        given(deliveryRepository.findByUuid(
                any(UUID.class)))
                .willReturn(Optional.of(deliveryEntity));

        // when
        Delivery deliveryByUuid = deliveryService.findByUuid(deliveryUuid);

        // then
        assertThat(deliveryByUuid.getUuid())
                .isNotNull();
    }

    @Test
    @DisplayName("should throw an exception when delivery uuid is null")
    void findByUuidNull() {

        // given
        UUID deliveryUuid = null;

        // when
        Throwable throwable = catchThrowable(() ->
                deliveryService.findByUuid(deliveryUuid));

        // then
        assertThat(throwable)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Searching for delivery failed. Delivery uuid is null.");

        // verify
        then(deliveryRepository)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("should delete an delivery based on delivery uuid")
    void delete() {

        // given
        DeliveryEntity deliveryEntity = prepareDeliveryEntity();
        UUID deliveryUuid = DELIVERY_UUID_1;

        // and
        given(deliveryRepository.findByUuid(
                any(UUID.class)))
                .willReturn(Optional.of(deliveryEntity));

        // when
        deliveryService.delete(deliveryUuid);

        // then
        then(deliveryRepository)
                .should(times(1))
                .delete(deliveryEntity);
    }

    @Test
    @DisplayName("should throw an exception when delivery uuid is null")
    void deleteNull() {
        // given
        UUID deliveryUuid = null;

        // when
        Throwable throwable = catchThrowable(() ->
                deliveryService.delete(deliveryUuid));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Deleting delivery failed. Delivery uuid is null.");
    }

    @Test
    @DisplayName("should throw an exception when uuid is null")
    void updateNullUuid() {
        // given
        Delivery delivery = prepareDelivery();
        UUID deliveryUuid = null;

        // when
        Throwable throwable = catchThrowable(() ->
                deliveryService.update(deliveryUuid, delivery));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Updating delivery failed. Delivery uuid is null.");
    }

    @Test
    @DisplayName("should throw an exception when delivery is null")
    void updateNullDelivery() {
        // given
        UUID deliveryUuid = DELIVERY_UUID_1;
        Delivery delivery = null;

        // when
        Throwable thrown = catchThrowable(() ->
                deliveryService.update(deliveryUuid, delivery));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Updating delivery failed. Delivery is null.");
    }

    @Test
    @DisplayName("should throw an exception when newDelivery's uuid doesn't match deliveryFromDb's uuid")
    void updateUuidMissMatch() {
        // given
        DeliveryEntity deliveryEntity = prepareDeliveryEntity();
        UUID oldUuid = DELIVERY_UUID_1;

        Delivery newDelivery = new Delivery();
        UUID newUuid = UUID.randomUUID();
        newDelivery.setUuid(newUuid);

        // and
        when(deliveryRepository.findByUuid(any(UUID.class)))
                .thenReturn(Optional.of(deliveryEntity));
        // when
        Throwable throwable = catchThrowable(() ->
                deliveryService.update(oldUuid, newDelivery));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Updating delivery fields failed. Different uuid's");
    }

    @Test
    @DisplayName("should correctly update delivery when valid uuid and delivery are provided")
    void shouldUpdateDelivery() {
        // given
        DeliveryEntity deliveryEntity = prepareDeliveryEntity();
        UUID deliveryUuid = DELIVERY_UUID_1;

        // and
        Delivery changedDelivery = prepareDeliveryForEdit();
        DeliveryEntity changedDeliveryEntity = prepareDeliveryEntityForEdit();

        // and
        when(deliveryRepository.findByUuid(any(UUID.class)))
                .thenReturn(Optional.of(deliveryEntity));
        when(deliveryRepository.save(any(DeliveryEntity.class)))
                .thenReturn(changedDeliveryEntity);

        //and
        when(deliveryEntityMapper
                .mapToApiModel(changedDeliveryEntity))
                .thenReturn(changedDelivery);

        // when
        Delivery updatedDelivery = deliveryService.update(deliveryUuid, changedDelivery);

        // then
        assertThat(updatedDelivery).isNotNull();
        assertThat(updatedDelivery.getId()).isEqualTo(changedDelivery.getId());
        assertThat(updatedDelivery.getUuid()).isEqualTo(changedDelivery.getUuid());
        assertThat(updatedDelivery.getDeliveryTime()).isEqualTo(changedDelivery.getDeliveryTime());
        assertThat(updatedDelivery.getPrice()).isEqualTo(changedDelivery.getPrice());
        assertThat(updatedDelivery.getDeliveryStatus()).isEqualTo(changedDelivery.getDeliveryStatus());

        assertThat(updatedDelivery.getOrder().getUuid()).isEqualTo(changedDelivery.getOrder().getUuid());
        assertThat(updatedDelivery.getCourier().getUuid()).isEqualTo(changedDelivery.getCourier().getUuid());

        // verify
        then(deliveryRepository)
                .should(times(1))
                .save(deliveryEntity);
    }

    private Delivery prepareDeliveryForEdit() {
        Long deliveryId = 1L;
        UUID deliveryUuid = DELIVERY_UUID_1;
        Double newPrice = 40.50D;
        LocalDateTime newDeliveryLocalDateTime = LocalDateTime.of(2023, 5, 28, 20, 30, 0);
        OffsetDateTime newDeliveryOffsetDateTime = newDeliveryLocalDateTime.atOffset(ZoneOffset.UTC);
        DeliveryStatus newDeliveryStatus = IN_TRANSIT;
        UUID newOrderUuid = UUID.randomUUID();
        UUID newCourierUuid = UUID.randomUUID();

        Long newOrderId = 3L;
        Long newCourierId = 3L;
        Delivery changedDelivery = new Delivery();
        changedDelivery.setId(deliveryId);
        changedDelivery.setPrice(newPrice);
        changedDelivery.setDeliveryTime(newDeliveryOffsetDateTime);
        changedDelivery.setDeliveryStatus(newDeliveryStatus);
        changedDelivery.setUuid(deliveryUuid);

        Order newOrder = new Order();
        newOrder.setId(newOrderId);
        newOrder.setUuid(newOrderUuid);

        Courier newCourier = new Courier();
        newCourier.setId(newCourierId);
        newCourier.setUuid(newCourierUuid);

        changedDelivery.setOrder(newOrder);
        changedDelivery.setCourier(newCourier);

        return changedDelivery;
    }

    private DeliveryEntity prepareDeliveryEntityForEdit() {
        UUID deliveryUuid = DELIVERY_UUID_1;
        BigDecimal newPrice = BigDecimal.valueOf(40.50D);
        LocalDateTime newDeliveryLocalDateTime = LocalDateTime.of(2023, 5, 28, 20, 30, 0);
        DeliveryStatus newDeliveryStatus = IN_TRANSIT;
        UUID newOrderUuid = UUID.randomUUID();
        UUID newCourierUuid = UUID.randomUUID();

        DeliveryEntity changedDeliveryEntity = new DeliveryEntity();
        changedDeliveryEntity.setUuid(deliveryUuid);
        changedDeliveryEntity.setPrice(newPrice);
        changedDeliveryEntity.setDeliveryTime(newDeliveryLocalDateTime);
        changedDeliveryEntity.setDeliveryStatus(newDeliveryStatus);

        OrderEntity newOrderEntity = new OrderEntity();
        newOrderEntity.setUuid(newOrderUuid);
        CourierEntity newCourierEntity = new CourierEntity();
        newCourierEntity.setUuid(newCourierUuid);

        changedDeliveryEntity.setOrder(newOrderEntity);
        changedDeliveryEntity.setCourier(newCourierEntity);

        return changedDeliveryEntity;
    }
}