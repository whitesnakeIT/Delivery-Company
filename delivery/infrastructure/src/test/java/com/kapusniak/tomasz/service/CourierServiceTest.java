package com.kapusniak.tomasz.service;

import com.kapusniak.tomasz.entity.CourierEntity;
import com.kapusniak.tomasz.entity.DeliveryEntity;
import com.kapusniak.tomasz.mapper.CourierEntityMapper;
import com.kapusniak.tomasz.openapi.model.Courier;
import com.kapusniak.tomasz.openapi.model.CourierCompany;
import com.kapusniak.tomasz.repository.jpa.CourierJpaRepository;
import com.kapusniak.tomasz.service.model.CourierService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.kapusniak.tomasz.openapi.model.CourierCompany.DHL;
import static com.kapusniak.tomasz.openapi.model.CourierCompany.FEDEX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@SpringBootTest
@ActiveProfiles("test")
class CourierServiceTest {

    private static final UUID COURIER_UUID_1 = UUID.fromString("fe362772-17c3-4547-b559-ceb13e164e6f");
    @Mock
    private CourierJpaRepository courierRepository;
    @Mock
    private CourierEntityMapper courierEntityMapper;

    @InjectMocks
    private CourierService courierService;

    private Courier prepareCourier() {
        Courier courier = new Courier();
        courier.setId(1L);
        courier.setCourierCompany(DHL);
        courier.setFirstName("testFirstName");
        courier.setLastName("testLastName");
        courier.setUuid(COURIER_UUID_1);

        return courier;
    }

    private CourierEntity prepareCourierEntity() {
        CourierEntity courierEntity = new CourierEntity();
        courierEntity.setId(1L);
        courierEntity.setCourierCompany(DHL);
        courierEntity.setFirstName("testFirstName");
        courierEntity.setLastName("testLastName");
        courierEntity.setUuid(COURIER_UUID_1);


        return courierEntity;
    }

    private List<Courier> prepareCourierList() {

        return List.of(prepareCourier(), prepareCourier());
    }

    private List<CourierEntity> prepareCourierEntityList() {

        return List.of(prepareCourierEntity(), prepareCourierEntity());
    }

    @BeforeEach
    void setup() {

        when(courierEntityMapper
                .mapToEntity(any(Courier.class)))
                .thenReturn(prepareCourierEntity());
        when(courierEntityMapper
                .mapToApiModel(any(CourierEntity.class)))
                .thenReturn(prepareCourier());
    }

    @Test
    @DisplayName("should correctly save an Courier entity exactly once")
    void save() {

        // given
        Courier courier = prepareCourier();
        CourierEntity courierEntity = prepareCourierEntity();

        // and
        when(courierRepository
                .save(any(CourierEntity.class)))
                .thenReturn(courierEntity);

        // when
        Courier result = courierService.save(courier);

        // then
        then(courierRepository)
                .should(times(1))
                .save(courierEntityMapper.mapToEntity(courier));

        // verify
        assertThat(courier).isEqualTo(result);
    }

    @Test
    @DisplayName("should throw an exception when Courier entity is null and check method invocations")
    void saveNull() {

        // given
        Courier courier = null;

        // when
        Throwable throwable = catchThrowable(() ->
                courierService.save(courier));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Saving courier failed. Courier is null.");

        // verify
        then(courierRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("should return list of all courier with correct size and check method invocations")
    void findAll() {

        // given
        List<CourierEntity> courierEntityList = prepareCourierEntityList();

        given(courierRepository.findAll())
                .willReturn(courierEntityList);

        // when
        List<Courier> allCouriers = courierService.findAll();

        // then
        assertThat(allCouriers.size())
                .isEqualTo(2);

        // verify
        then(courierRepository)
                .should(times(1))
                .findAll();
    }

    @Test
    @DisplayName("should return courier based on courier uuid")
    void findByUuid() {

        // given
        CourierEntity courierEntity = prepareCourierEntity();
        UUID courierUuid = COURIER_UUID_1;

        // and
        given(courierRepository.findByUuid(
                any(UUID.class)))
                .willReturn(Optional.of(courierEntity));

        // when
        Courier courierByUuid = courierService.findByUuid(courierUuid);

        // then
        assertThat(courierByUuid.getUuid())
                .isNotNull();
    }

    @Test
    @DisplayName("should throw an exception when courier uuid is null")
    void findByUuidNull() {

        // given
        UUID courierUuid = null;

        // when
        Throwable throwable = catchThrowable(() ->
                courierService.findByUuid(courierUuid));

        // then
        assertThat(throwable)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Searching for courier failed. Courier uuid is null.");

        // verify
        then(courierRepository)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("should delete an courier based on courier uuid")
    void delete() {

        // given
        CourierEntity courierEntity = prepareCourierEntity();
        UUID courierUuid = COURIER_UUID_1;

        // and
        given(courierRepository.findByUuid(
                any(UUID.class)))
                .willReturn(Optional.of(courierEntity));

        // when
        courierService.delete(courierUuid);

        // then
        then(courierRepository)
                .should(times(1))
                .delete(courierEntity);
    }

    @Test
    @DisplayName("should throw an exception when courier uuid is null")
    void deleteNull() {         // given
        UUID courierUuid = null;

        // when
        Throwable throwable = catchThrowable(() ->
                courierService.delete(courierUuid));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Deleting courier failed. Courier uuid is null.");
    }

    @Test
    @DisplayName("should throw an exception when uuid is null")
    void updateNullUuid() {
        // given
        Courier courier = prepareCourier();
        UUID courierUuid = null;

        // when
        Throwable throwable = catchThrowable(() ->
                courierService.update(courierUuid, courier));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Updating courier failed. Courier uuid is null.");
    }

    @Test
    @DisplayName("should throw an exception when courier is null")
    void updateNullCourier() {
        // given
        UUID courierUuid = COURIER_UUID_1;
        Courier courier = null;

        // when
        Throwable thrown = catchThrowable(() ->
                courierService.update(courierUuid, courier));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Updating courier failed. Courier is null.");
    }

    @Test
    @DisplayName("should throw an exception when newCourier's uuid doesn't match courierFromDb's uuid")
    void updateUuidMissMatch() {
        // given
        CourierEntity courierEntity = prepareCourierEntity();
        UUID oldUuid = COURIER_UUID_1;

        Courier newCourier = new Courier();
        UUID newUuid = UUID.randomUUID();
        newCourier.setUuid(newUuid);

        // and
        when(courierRepository.findByUuid(any(UUID.class)))
                .thenReturn(Optional.of(courierEntity));
        // when
        Throwable throwable = catchThrowable(() ->
                courierService.update(oldUuid, newCourier));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Updating courier fields failed. Different uuid's");
    }

    @Test
    @DisplayName("should correctly update courier when valid uuid and courier are provided")
    void shouldUpdateCourier() {
        // given
        UUID courierUuid = COURIER_UUID_1;
        Courier changedCourier = prepareCourierToEdit();
        CourierEntity courierEntity = prepareCourierEntity();
        CourierEntity changedCourierEntity = prepareCourierEntityToEdit();

        // and
        when(courierRepository.findByUuid(any(UUID.class)))
                .thenReturn(Optional.of(courierEntity));
        when(courierRepository.save(any(CourierEntity.class)))
                .thenReturn(changedCourierEntity);

        //and
        when(courierEntityMapper
                .mapToApiModel(changedCourierEntity))
                .thenReturn(changedCourier);

        // when
        Courier updatedCourier = courierService.update(courierUuid, changedCourier);

        // then
        assertThat(updatedCourier).isNotNull();
        assertThat(updatedCourier.getId()).isEqualTo(changedCourier.getId());
        assertThat(updatedCourier.getUuid()).isEqualTo(changedCourier.getUuid());
        assertThat(updatedCourier.getFirstName()).isEqualTo(changedCourier.getFirstName());
        assertThat(updatedCourier.getLastName()).isEqualTo(changedCourier.getLastName());
        assertThat(updatedCourier.getCourierCompany()).isEqualTo(changedCourier.getCourierCompany());
        assertThat(updatedCourier.getDeliveryList().get(0)).isEqualTo(changedCourier.getDeliveryList().get(0));
        assertThat(updatedCourier.getDeliveryList().get(1)).isEqualTo(changedCourier.getDeliveryList().get(1));

        // verify
        then(courierRepository)
                .should(times(1))
                .save(courierEntity);
    }

    private CourierEntity prepareCourierEntityToEdit() {
        UUID courierUuid = COURIER_UUID_1;
        String newFirstName = "newFirstName";
        String newLastName = "newLastName";
        CourierCompany newCourierCompany = FEDEX;

        UUID newDelivery1Uuid = UUID.randomUUID();
        UUID newDelivery2Uuid = UUID.randomUUID();

        Long newDelivery1Id = 3L;
        Long newDelivery2Id = 4L;

        CourierEntity changedCourierEntity = new CourierEntity();
        changedCourierEntity.setUuid(courierUuid);
        changedCourierEntity.setFirstName(newFirstName);
        changedCourierEntity.setLastName(newLastName);
        changedCourierEntity.setCourierCompany(newCourierCompany);

        DeliveryEntity newDeliveryEntity1 = new DeliveryEntity();
        newDeliveryEntity1.setId(newDelivery1Id);
        newDeliveryEntity1.setUuid(newDelivery1Uuid);
        DeliveryEntity newDeliveryEntity2 = new DeliveryEntity();
        newDeliveryEntity2.setId(newDelivery2Id);
        newDeliveryEntity2.setUuid(newDelivery2Uuid);

        changedCourierEntity.setDeliveryList(List.of(newDeliveryEntity1, newDeliveryEntity2));

        return changedCourierEntity;
    }

    private Courier prepareCourierToEdit() {
        UUID courierUuid = COURIER_UUID_1;
        String newFirstName = "newFirstName";
        String newLastName = "newLastName";
        CourierCompany newCourierCompany = FEDEX;

        UUID newDelivery1Uuid = UUID.fromString("31822712-94b3-43ed-9aac-24613948ca79");
        UUID newDelivery2Uuid = UUID.fromString("1f263424-a92a-49a6-b38f-eaa2861ab332");

        Courier changedCourier = new Courier();
        changedCourier.setUuid(courierUuid);
        changedCourier.setFirstName(newFirstName);
        changedCourier.setLastName(newLastName);
        changedCourier.setCourierCompany(newCourierCompany);

        changedCourier.setDeliveryList(List.of(newDelivery1Uuid, newDelivery2Uuid));

        return changedCourier;
    }
}