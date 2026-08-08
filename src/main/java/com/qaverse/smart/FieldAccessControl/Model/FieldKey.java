package com.qaverse.smart.FieldAccessControl.Model;

import java.util.Objects;

public final class FieldKey<E extends Enum<E>> {

    private final Class<E> page;

    private final E field;

    public FieldKey(
            Class<E> page,
            E field) {

        this.page =
                Objects.requireNonNull(page);

        this.field =
                Objects.requireNonNull(field);
    }

    public Class<E> getPage() {
        return page;
    }

    public E getField() {
        return field;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof FieldKey<?> other)) {
            return false;
        }

        return page.equals(other.page)
                && field == other.field;
    }

    @Override
    public int hashCode() {

        return Objects.hash(
                page,
                field
        );
    }
}