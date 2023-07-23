package com.kapusniak.tomasz.mapper;

import com.kapusniak.tomasz.entity.CourierEntity;
import com.kapusniak.tomasz.entity.DeliveryEntity;
import com.kapusniak.tomasz.openapi.model.Courier;
import com.kapusniak.tomasz.service.model.DeliveryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static com.kapusniak.tomasz.openapi.model.CourierCompany.DPD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class CourierEntityMapperTest {

    public static final UUID COURIER_UUID = UUID.fromString("fe362772-17c3-4547-b559-ceb13e164e6f");
    private static final UUID DELIVERY_UUID_1 = UUID.randomUUID();
    private static final UUID DELIVERY_UUID_2 = UUID.randomUUID();

    @Autowired
    private CourierEntityMapper courierEntityMapper;

    @MockBean
    private DeliveryService deliveryService;

    public Courier prepareCourier() {
        Courier courier = new Courier();
        courier.setId(1L);
        courier.setFirstName("testFirstName");
        courier.setLastName("testLastName");
        courier.setDeliveryList(prepareDeliveryList());
        courier.setCourierCompany(DPD);
        courier.setUuid(COURIER_UUID);
        courier.setVersion(0L);

        return courier;
    }

    public List<UUID> prepareDeliveryList() {
        return List.of(DELIVERY_UUID_1, DELIVERY_UUID_2);

    }

    public List<DeliveryEntity> prepareDeliveryEntityList() {
        DeliveryEntity deliveryEntity1 = new DeliveryEntity();
        DeliveryEntity deliveryEntity2 = new DeliveryEntity();

        deliveryEntity1.setUuid(DELIVERY_UUID_1);
        deliveryEntity2.setUuid(DELIVERY_UUID_2);

        return List.of(deliveryEntity1, deliveryEntity2);
    }

    public CourierEntity prepareCourierEntity() {
        CourierEntity courierEntity = new CourierEntity();
        courierEntity.setId(1L);
        courierEntity.setFirstName("testFirstName");
        courierEntity.setLastName("testLastName");
        courierEntity.setDeliveryList(prepareDeliveryEntityList());
        courierEntity.setCourierCompany(DPD);
        courierEntity.setUuid(COURIER_UUID);
        courierEntity.setVersion(0L);

        return courierEntity;
    }

    @Test
    @DisplayName("should map from Courier to CourierEntity")
    void mapToEntity() {
        // given
        Courier courier = prepareCourier();

        // and
        when(deliveryService.convertUuidToEntity(anyList()))
                .thenReturn(prepareDeliveryEntityList());

        // when
        CourierEntity courierEntity = courierEntityMapper.mapToEntity(courier);

        // then
        assertThat(courierEntity.getUuid()).isEqualTo(courier.getUuid());
        assertThat(courierEntity.getFirstName()).isEqualTo(courier.getFirstName());
        assertThat(courierEntity.getLastName()).isEqualTo(courier.getLastName());
        assertThat(courierEntity.getCourierCompany()).isEqualTo(courier.getCourierCompany());

        assertThat(courierEntity.getUuid()).isNotNull();
        assertThat(courierEntity.getDeliveryList()).isNotNull();

        assertThat(courierEntity.getDeliveryList().get(0).getUuid()).isEqualTo(courier.getDeliveryList().get(0));
        assertThat(courierEntity.getDeliveryList().get(1).getUuid()).isEqualTo(courier.getDeliveryList().get(1));

        // verify
        verify(deliveryService, times(1))
                .convertUuidToEntity(prepareDeliveryList());
    }

    @Test
    @DisplayName("should map from CourierEntity to Courier")
    void mapToApiModel() {
        // given
        CourierEntity courierEntity = prepareCourierEntity();

        // when
        Courier courier = courierEntityMapper.mapToApiModel(courierEntity);

        // then
        assertThat(courier.getUuid()).isEqualTo(courierEntity.getUuid());
        assertThat(courier.getFirstName()).isEqualTo(courierEntity.getFirstName());
        assertThat(courier.getLastName()).isEqualTo(courierEntity.getLastName());
        assertThat(courier.getCourierCompany()).isEqualTo(courierEntity.getCourierCompany());

        assertThat(courier.getDeliveryList()).isNotNull();

        assertThat(courier.getDeliveryList().get(0)).isEqualTo(courierEntity.getDeliveryList().get(0).getUuid());
        assertThat(courier.getDeliveryList().get(1)).isEqualTo(courierEntity.getDeliveryList().get(1).getUuid());
    }
}