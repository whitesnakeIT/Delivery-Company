package com.kapusniak.tomasz.repository;

public enum PageSize {
    EXTRA_SMALL(2),

    SMALL(5),
    NORMAL(10),
    BIG(15),
    EXTRA_BIG(20);

    private final int value;

    PageSize(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}