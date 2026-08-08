package com.qaverse.smart.FieldAccessControl.Builder;

import com.qaverse.smart.FieldAccessControl.Condition.FieldCondition;
import com.qaverse.smart.FieldAccessControl.Registry.FieldConditionRegistry;

public final class ConditionBuilder<E extends Enum<E>> {

    private final Class<E> page;
    private final FieldCondition<E> condition;

    public ConditionBuilder(
            Class<E> page,
            FieldCondition<E> condition) {

        if (page == null) {
            throw new IllegalArgumentException(
                    "Page cannot be null"
            );
        }

        if (condition == null) {
            throw new IllegalArgumentException(
                    "Field condition cannot be null"
            );
        }

        this.page = page;
        this.condition = condition;
    }

    @SafeVarargs
    public final void controls(E... dependentFields) {

        if (dependentFields == null
                || dependentFields.length == 0) {

            throw new IllegalArgumentException(
                    "At least one dependent field is required"
            );
        }

        FieldConditionRegistry.register(
                page,
                condition,
                dependentFields
        );
    }
}