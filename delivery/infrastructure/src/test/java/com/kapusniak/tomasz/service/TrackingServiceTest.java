package com.kapusniak.tomasz.service;

import com.kapusniak.tomasz.entity.TrackingEntity;
import com.kapusniak.tomasz.mapper.TrackingEntityMapper;
import com.kapusniak.tomasz.openapi.model.Tracking;
import com.kapusniak.tomasz.repository.jpa.TrackingJpaRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@SpringBootTest
@ActiveProfiles("test")
class TrackingServiceTest {

    private static final UUID TRACKING_UUID_1 = UUID.fromString("97e37668-b355-4ecd-83be-dbc9cf56d8c0");
    @Mock
    private TrackingJpaRepository trackingRepository;
    @Mock
    private TrackingEntityMapper trackingEntityMapper;

    @InjectMocks
    private TrackingService trackingService;

    private Tracking prepareTracking() {
        Tracking tracking = new Tracking();
        tracking.setId(1L);
        tracking.setLocalization("testLocalization");
        tracking.setUuid(TRACKING_UUID_1);

        return tracking;
    }

    private TrackingEntity prepareTrackingEntity() {
        TrackingEntity trackingEntity = new TrackingEntity();
        trackingEntity.setId(1L);
        trackingEntity.setLocalization("testLocalization");
        trackingEntity.setUuid(TRACKING_UUID_1);


        return trackingEntity;
    }

    private List<Tracking> prepareTrackingList() {

        return List.of(prepareTracking(), prepareTracking());
    }

    private List<TrackingEntity> prepareTrackingEntityList() {

        return List.of(prepareTrackingEntity(), prepareTrackingEntity());
    }

    @BeforeEach
    void setup() {

        when(trackingEntityMapper
                .mapToEntity(any(Tracking.class)))
                .thenReturn(prepareTrackingEntity());
        when(trackingEntityMapper
                .mapToApiModel(any(TrackingEntity.class)))
                .thenReturn(prepareTracking());
    }

    @Test
    @DisplayName("should correctly save an Tracking entity exactly once")
    void save() {

        // given
        TrackingEntity trackingEntity = prepareTrackingEntity();
        Tracking tracking = prepareTracking();


        // and
        when(trackingRepository
                .save(any(TrackingEntity.class)))
                .thenReturn(trackingEntity);

        // when
        Tracking result = trackingService.save(tracking);

        // then
        then(trackingRepository)
                .should(times(1))
                .save(trackingEntityMapper.mapToEntity(tracking));

        // verify
        assertThat(tracking).isEqualTo(result);
    }

    @Test
    @DisplayName("should throw an exception when Tracking entity is null and check method invocations")
    void saveNull() {

        // given
        Tracking tracking = null;

        // when
        Throwable throwable = catchThrowable(() ->
                trackingService.save(tracking));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Saving tracking failed. Tracking is null.");

        // verify
        then(trackingRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("should return list of all tracking with correct size and check method invocations")
    void findAll() {

        // given
        List<TrackingEntity> trackingEntityList = prepareTrackingEntityList();

        given(trackingRepository.findAll())
                .willReturn(trackingEntityList);

        // when
        List<Tracking> allTracking = trackingService.findAll();

        // then
        assertThat(allTracking.size())
                .isEqualTo(2);

        // verify
        then(trackingRepository)
                .should(times(1))
                .findAll();
    }

    @Test
    @DisplayName("should return tracking based on tracking uuid")
    void findByUuid() {

        // given
        TrackingEntity trackingEntity = prepareTrackingEntity();
        UUID trackingUuid = TRACKING_UUID_1;

        // and
        given(trackingRepository.findByUuid(
                any(UUID.class)))
                .willReturn(Optional.of(trackingEntity));

        // when
        Tracking trackingByUuid = trackingService.findByUuid(trackingUuid);

        // then
        assertThat(trackingByUuid.getUuid())
                .isNotNull();
    }

    @Test
    @DisplayName("should throw an exception when tracking uuid is null")
    void findByUuidNull() {

        // given
        UUID trackingUuid = null;

        // when
        Throwable throwable = catchThrowable(() ->
                trackingService.findByUuid(trackingUuid));

        // then
        assertThat(throwable)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Searching for tracking failed. Tracking uuid is null.");

        // verify
        then(trackingRepository)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("should delete an tracking based on tracking uuid")
    void delete() {

        // given
        TrackingEntity trackingEntity = prepareTrackingEntity();
        UUID trackingUuid = TRACKING_UUID_1;

        given(trackingRepository.findByUuid(
                any(UUID.class)))
                .willReturn(Optional.of(trackingEntity));

        // when
        trackingService.delete(trackingUuid);

        // then
        then(trackingRepository)
                .should(times(1))
                .delete(trackingEntity);
    }

    @Test
    @DisplayName("should throw an exception when tracking uuid is null")
    void deleteNull() {
        // given
        UUID trackingUuid = null;

        // when
        Throwable throwable = catchThrowable(() ->
                trackingService.delete(trackingUuid));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Deleting tracking failed. Tracking uuid is null.");
    }

    @Test
    @DisplayName("should throw an exception when uuid is null")
    void updateNullUuid() {
        // given
        Tracking tracking = prepareTracking();
        UUID trackingUuid = null;

        // when
        Throwable throwable = catchThrowable(() ->
                trackingService.update(trackingUuid, tracking));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Updating tracking failed. Tracking uuid is null.");
    }

    @Test
    @DisplayName("should throw an exception when tracking is null")
    void updateNullTracking() {
        // given
        UUID trackingUuid = TRACKING_UUID_1;
        Tracking tracking = null;

        // when
        Throwable thrown = catchThrowable(() ->
                trackingService.update(trackingUuid, tracking));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Updating tracking failed. Tracking is null.");
    }

    @Test
    @DisplayName("should throw an exception when newTracking's uuid doesn't match trackingFromDb's uuid")
    void updateUuidMissMatch() {
        // given
        TrackingEntity trackingEntity = prepareTrackingEntity();
        UUID oldUuid = TRACKING_UUID_1;

        Tracking newTracking = new Tracking();
        UUID newUuid = UUID.randomUUID();
        newTracking.setUuid(newUuid);

        // and
        when(trackingRepository.findByUuid(any(UUID.class)))
                .thenReturn(Optional.of(trackingEntity));
        // when
        Throwable throwable = catchThrowable(() ->
                trackingService.update(oldUuid, newTracking));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Updating tracking fields failed. Different uuid's");
    }

    @Test
    @DisplayName("should correctly update tracking when valid uuid and tracking are provided")
    void shouldUpdateTracking() {
        // given
        TrackingEntity trackingEntity = prepareTrackingEntity();

        UUID trackingUuid = TRACKING_UUID_1;
        Tracking changedTracking = prepareTrackingForEdit();
        TrackingEntity changedTrackingEntity = prepareTrackingEntityForEdit();

        // and
        when(trackingRepository.findByUuid(any(UUID.class)))
                .thenReturn(Optional.of(trackingEntity));
        when(trackingRepository.save(any(TrackingEntity.class)))
                .thenReturn(changedTrackingEntity);

        //and
        when(trackingEntityMapper
                .mapToApiModel(changedTrackingEntity))
                .thenReturn(changedTracking);

        // when
        Tracking updatedTracking = trackingService.update(trackingUuid, changedTracking);

        // then
        assertThat(updatedTracking).isNotNull();
        assertThat(updatedTracking.getId()).isEqualTo(changedTracking.getId());
        assertThat(updatedTracking.getUuid()).isEqualTo(changedTracking.getUuid());
        assertThat(updatedTracking.getLocalization()).isEqualTo(changedTracking.getLocalization());

        // verify
        then(trackingRepository)
                .should(times(1))
                .save(trackingEntity);
    }

    private Tracking prepareTrackingForEdit() {
        Long trackingId = 1L;
        UUID trackingUuid = TRACKING_UUID_1;
        String newLocalization = "newLocalization";

        Tracking changedTracking = new Tracking();
        changedTracking.setId(trackingId);
        changedTracking.setUuid(trackingUuid);
        changedTracking.setLocalization(newLocalization);

        return changedTracking;
    }

    private TrackingEntity prepareTrackingEntityForEdit() {
        Long trackingId = 1L;
        UUID trackingUuid = TRACKING_UUID_1;
        String newLocalization = "newLocalization";

        TrackingEntity changedTrackingEntity = new TrackingEntity();
        changedTrackingEntity.setId(trackingId);
        changedTrackingEntity.setUuid(trackingUuid);
        changedTrackingEntity.setLocalization(newLocalization);

        return changedTrackingEntity;
    }
}