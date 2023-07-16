package com.kapusniak.tomasz.mapper;

import com.kapusniak.tomasz.entity.TrackingEntity;
import com.kapusniak.tomasz.openapi.model.Tracking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(
        componentModel = "spring",
        uses = {
                UuidMapper.class
        }
)
public interface TrackingEntityMapper {

    @Mapping(
            target = "uuid",
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(
            target = "version",
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    TrackingEntity mapToEntity(Tracking tracking);

    Tracking mapToApiModel(TrackingEntity trackingEntity);
}
