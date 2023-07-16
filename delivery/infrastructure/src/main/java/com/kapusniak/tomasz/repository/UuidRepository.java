
package com.kapusniak.tomasz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;


@NoRepositoryBean
public interface UuidRepository<T, UUID> extends JpaRepository<T, Long> {
    Optional<T> findByUuid(UUID uuid);

    List<T> findAllByUuidIn(List<UUID> uuidList);
}