package com.kapusniak.tomasz.mapper;

import com.kapusniak.tomasz.entity.CourierEntity;
import com.kapusniak.tomasz.entity.CustomerEntity;
import com.kapusniak.tomasz.entity.DeliveryEntity;
import com.kapusniak.tomasz.entity.OrderEntity;
import com.kapusniak.tomasz.openapi.model.Delivery;
import com.kapusniak.tomasz.openapi.model.DeliveryStatus;
import com.kapusniak.tomasz.openapi.model.PackageSize;
import com.kapusniak.tomasz.openapi.model.PackageType;
import com.kapusniak.tomasz.service.CourierService;
import com.kapusniak.tomasz.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static com.kapusniak.tomasz.openapi.model.CourierCompany.DPD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class DeliveryEntityMapperTest {

    public static final UUID DELIVERY_UUID = UUID.fromString("fe362772-17c3-4547-b559-ceb13e164e6f");
    public static final UUID ORDER_UUID = UUID.fromString("fe362772-17c3-4547-b559-ceb13e164e6f");
    public static final UUID COURIER_UUID = UUID.fromString("fe362772-17c3-4547-b559-ceb13e164e6f");

    @Autowired
    private DeliveryEntityMapper deliveryEntityMapper;

    @MockBean
    private CourierService courierService;

    @MockBean
    private OrderService orderService;

    public Delivery prepareDelivery() {
        LocalDateTime localDeliveryTime =
                LocalDateTime.of(2023, 5, 28, 12, 0, 0);
        OffsetDateTime offsetDeliveryTime =
                localDeliveryTime.atOffset(ZoneOffset.UTC);
        Delivery delivery = new Delivery();
        delivery.setId(1L);
        delivery.setPrice(20.50d);
        delivery.setDeliveryStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveryTime(offsetDeliveryTime);

        delivery.setCourier(COURIER_UUID);
        delivery.setOrder(ORDER_UUID);
        delivery.setUuid(DELIVERY_UUID);
        delivery.setVersion(0L);

        return delivery;
    }

    public DeliveryEntity prepareDeliveryEntity() {
        LocalDateTime localDeliveryTime =
                LocalDateTime.of(2023, 5, 28, 12, 0, 0);
        DeliveryEntity deliveryEntity = new DeliveryEntity();
        deliveryEntity.setId(1L);
        deliveryEntity.setPrice(BigDecimal.valueOf(20.50d));
        deliveryEntity.setDeliveryStatus(DeliveryStatus.DELIVERED);
        deliveryEntity.setDeliveryTime(localDeliveryTime);
        deliveryEntity.setCourier(prepareCourierEntity());
        deliveryEntity.setOrder(prepareOrderEntity());
        deliveryEntity.setUuid(DELIVERY_UUID);
        deliveryEntity.setVersion(0L);

        return deliveryEntity;
    }

    public CourierEntity prepareCourierEntity() {
        CourierEntity courierEntity = new CourierEntity();
        courierEntity.setId(1L);
        courierEntity.setFirstName("testFirstName");
        courierEntity.setLastName("testLastName");
        courierEntity.setDeliveryList(List.of(new DeliveryEntity(), new DeliveryEntity()));
        courierEntity.setCourierCompany(DPD);
        courierEntity.setUuid(COURIER_UUID);
        courierEntity.setVersion(0L);

        return courierEntity;
    }

    public OrderEntity prepareOrderEntity() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(1L);
        orderEntity.setSenderAddress("testSenderAddress");
        orderEntity.setReceiverAddress("testReceiverAddress");
        orderEntity.setPackageType(PackageType.PARCEL);
        orderEntity.setPackageSize(PackageSize.SMALL);
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setOrders(List.of(new OrderEntity(), new OrderEntity()));
        orderEntity.setCustomer(customerEntity);
        orderEntity.setVersion(0L);
        orderEntity.setUuid(ORDER_UUID);

        return orderEntity;
    }

    @Test
    @DisplayName("should map Delivery to DeliveryEntity")
    void mapToEntity() {
        // given
        Delivery delivery = prepareDelivery();

        // and
        when(courierService.convertUuidToEntity(any(UUID.class)))
                .thenReturn(prepareCourierEntity());

        when(orderService.convertUuidToEntity(any(UUID.class)))
                .thenReturn(prepareOrderEntity());

        // when
        DeliveryEntity deliveryEntity = deliveryEntityMapper.mapToEntity(delivery);

        // then
        assertThat(deliveryEntity.getUuid()).isEqualTo(delivery.getUuid());
        assertThat(deliveryEntity.getPrice()).isEqualTo(BigDecimal.valueOf(delivery.getPrice()));
        assertThat(deliveryEntity.getDeliveryStatus()).isEqualTo(delivery.getDeliveryStatus());

        assertThat(deliveryEntity.getCourier().getUuid()).isNotNull();
        assertThat(deliveryEntity.getCourier().getUuid()).isEqualTo(delivery.getCourier());

        assertThat(deliveryEntity.getOrder().getUuid()).isNotNull();
        assertThat(deliveryEntity.getOrder().getUuid()).isEqualTo(delivery.getCourier());

        //verify
        verify(orderService, times(1))
                .convertUuidToEntity(ORDER_UUID);
        verify(courierService, times(1))
                .convertUuidToEntity(COURIER_UUID);
    }

    @Test
    @DisplayName("should map DeliveryEntity to Delivery")
    void mapToApiModel() {
        // given
        DeliveryEntity deliveryEntity = prepareDeliveryEntity();

        // when
        Delivery delivery = deliveryEntityMapper.mapToApiModel(deliveryEntity);

        // then
        assertThat(delivery.getUuid()).isEqualTo(deliveryEntity.getUuid());
        assertThat(delivery.getPrice()).isEqualTo(deliveryEntity.getPrice().doubleValue());
        assertThat(delivery.getDeliveryStatus()).isEqualTo(deliveryEntity.getDeliveryStatus());

        assertThat(delivery.getCourier()).isNotNull();
        assertThat(delivery.getCourier()).isEqualTo(deliveryEntity.getCourier().getUuid());

        assertThat(delivery.getOrder()).isNotNull();
        assertThat(delivery.getOrder()).isEqualTo(deliveryEntity.getOrder().getUuid());
    }

}