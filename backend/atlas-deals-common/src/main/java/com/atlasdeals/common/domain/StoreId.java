package com.atlasdeals.common.domain;

import java.util.Objects;
import java.util.UUID;

public final class StoreId extends ValueObject {
    private final UUID value;

    private StoreId(UUID value) {
        this.value = Objects.requireNonNull(value, "StoreId must not be null");
    }

    public static StoreId of(UUID value) {
        return new StoreId(value);
    }

    public static StoreId generate() {
        return new StoreId(UUID.randomUUID());
    }

    public static StoreId fromString(String value) {
        return new StoreId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreId storeId = (StoreId) o;
        return value.equals(storeId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
