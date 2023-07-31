package com.kapusniak.tomasz.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface BaseEntityService<T> {

    T convertUuidToEntity(UUID uuid);

    List<T> convertUuidToEntity(List<UUID> uuidList);

}
