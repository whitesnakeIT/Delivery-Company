package com.kapusniak.tomasz.service;

import java.util.List;
import java.util.UUID;

public interface BaseEntityService<T> {

    T convertUuidToEntity(UUID uuid);

    List<T> convertUuidToEntity(List<UUID> uuidList);
}
