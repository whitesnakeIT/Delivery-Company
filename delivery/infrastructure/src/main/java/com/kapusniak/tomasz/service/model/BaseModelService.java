package com.kapusniak.tomasz.service.model;


import org.springframework.data.domain.Page;

import java.util.UUID;

public interface BaseModelService<T> {

    T save(T model);

    Page<T> findAll(Integer pageNumber);

    T findByUuid(UUID uuid);

    void delete(UUID uuid);

    T update(UUID uuid, T model);

    default Integer validatePage(Integer page) {
        return (page == null || page < 0) ? 0 : page;
    }

}
