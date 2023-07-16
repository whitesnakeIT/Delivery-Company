package com.kapusniak.tomasz.mapper;

import com.kapusniak.tomasz.entity.TrackingEntity;
import com.kapusniak.tomasz.openapi.model.Tracking;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TrackingEntityMapperTest {

    @Autowired
    private TrackingEntityMapper trackingEntityMapper;

    private Tracking prepareTracking() {
        Tracking tracking = new Tracking();
        tracking.setId(1L);
        tracking.setLocalization("testLocalization");
        tracking.setUuid(UUID.fromString("97e37668-b355-4ecd-83be-dbc9cf56d8c0"));
        tracking.setVersion(0L);

        return tracking;
    }

    private TrackingEntity prepareTrackingEntity() {
        TrackingEntity trackingEntity = new TrackingEntity();
        trackingEntity.setId(1L);
        trackingEntity.setLocalization("123456789");
        trackingEntity.setUuid(UUID.fromString("97e37668-b355-4ecd-83be-dbc9cf56d8c0"));
        trackingEntity.setVersion(0L);

        return trackingEntity;
    }

    @Test
    @DisplayName("should map Tracking to TrackingEntity")
    public void mapToEntity() {
        // given
        Tracking tracking = prepareTracking();

        // when
        TrackingEntity trackingEntity = trackingEntityMapper.mapToEntity(tracking);

        // then
        assertThat(trackingEntity).isNotNull();
        assertThat(tracking.getUuid()).isEqualTo(trackingEntity.getUuid());
        assertThat(tracking.getLocalization()).isEqualTo(trackingEntity.getLocalization());
        assertThat(trackingEntity.getUuid()).isNotNull();
    }

    @Test
    @DisplayName("should map TrackingEntity to Tracking")
    public void mapToApiModel() {
        // given
        TrackingEntity trackingEntity = prepareTrackingEntity();

        // when
        Tracking tracking = trackingEntityMapper.mapToApiModel(trackingEntity);

        // then
        assertThat(tracking).isNotNull();
        assertThat(trackingEntity.getUuid()).isEqualTo(tracking.getUuid());
        assertThat(trackingEntity.getLocalization()).isEqualTo(tracking.getLocalization());
    }
}