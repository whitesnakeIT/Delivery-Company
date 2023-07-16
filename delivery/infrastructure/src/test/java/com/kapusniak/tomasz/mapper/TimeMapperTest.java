package com.kapusniak.tomasz.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TimeMapperTest {

    private final LocalDateTime localDeliveryTime =
            LocalDateTime.of(2023, 5, 28, 12, 0, 0);
    private final OffsetDateTime offsetDeliveryTime =
            localDeliveryTime.atOffset(ZoneOffset.UTC);

    @Autowired
    private TimeMapper timeMapper;

    @Test
    @DisplayName("should return null when LocalDateTime is null, instead of throwing an exception")
    void mapNullLocalDateTimeToOffset() {
        // given
        LocalDateTime localDateTime = null;

        // when
        OffsetDateTime offsetDateTime = timeMapper.mapLocalDateTimeToOffset(localDateTime);

        // then
        assertThat(offsetDateTime).isNull();
    }

    @Test
    @DisplayName("should correctly map from OffsetDateTime to LocalDateTime")
    public void mapOffsetToLocalDateTime() {
        // given
        OffsetDateTime offsetDeliveryTime = this.offsetDeliveryTime;
        LocalDateTime localDeliveryTime = this.localDeliveryTime;

        // when
        LocalDateTime result = timeMapper.mapOffsetToLocalDateTime(offsetDeliveryTime);

        // then
        assertThat(result).isEqualTo(localDeliveryTime);
    }

    @Test
    @DisplayName("should correctly map from LocalDateTime to OffsetDateTime")
    public void mapLocalDateTimeToOffset() {
        // given
        OffsetDateTime offsetDeliveryTime = this.offsetDeliveryTime;
        LocalDateTime localDeliveryTime = this.localDeliveryTime;

        // when
        OffsetDateTime result = timeMapper.mapLocalDateTimeToOffset(localDeliveryTime);

        // then
        assertThat(result).isEqualTo(offsetDeliveryTime);
    }

    @Test
    @DisplayName("should return null when OffsetDateTime is null, instead of throwing an exception")
    void mapNullOffsetToLocalDateTime() {
        // given
        OffsetDateTime offsetDateTime = null;

        // when
        LocalDateTime localDateTime = timeMapper.mapOffsetToLocalDateTime(offsetDateTime);

        // then
        assertThat(localDateTime).isNull();
    }

}