package com.kapusniak.tomasz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@MappedSuperclass
@EqualsAndHashCode(of = "uuid")
@Getter
@Setter
public abstract class BaseEntity {

    @Column(nullable = false, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @Version
    private Long version;
}