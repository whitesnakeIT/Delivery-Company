package com.kapusniak.tomasz.mapper;

import com.kapusniak.tomasz.entity.BaseEntity;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper(
        componentModel = "spring"
)
public abstract class UuidMapper {

//    private UuidRepository<T,UUID> uuidRepository;

//    @Autowired
//    public UuidMapper(UuidRepository<T, UUID> uuidRepository) {
//        this.uuidRepository = uuidRepository;
//    }

//    @Autowired
//    public void setUuidRepository(UuidRepository<T, UUID> uuidRepository) {
//        this.uuidRepository = uuidRepository;
//    }

    public UUID convertEntityToUuid(BaseEntity baseEntity) {
        return baseEntity.getUuid();
    }

    //    @IterableMapping(elementTargetType = UUID.class)
    public <T extends BaseEntity> List<UUID> convertEntityListToUuidList(List<T> baseEntityList) {

        return baseEntityList.stream()
                .map(BaseEntity::getUuid)
                .toList();
    }
//    public T convertUuidToCourierEntity(UUID uuid, UuidRepository<T, UUID> uuidRepository){
//            Optional<T> entity = uuidRepository.findByUuid(uuid);
//        return entity.orElse(null);
//    }

}
