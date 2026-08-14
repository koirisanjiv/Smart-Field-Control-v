package com.qaverse.smart.FieldAccessControl.Builder;

import java.util.EnumMap;
import java.util.Objects;

import com.qaverse.smart.FieldAccessControl.Configuration.ExecutionMode;
import com.qaverse.smart.FieldAccessControl.Configuration.OperationMode;
import com.qaverse.smart.FieldAccessControl.Configuration.UserType;
import com.qaverse.smart.FieldAccessControl.Model.FieldExecutionContext;

public final class FieldExecutionContextBuilder<E extends Enum<E>> {

    private Class<E> page;

    private OperationMode operationMode;

    private ExecutionMode executionMode =
            ExecutionMode.ALL_FIELDS;

    private UserType currentUser;

    private EnumMap<E, Object> fieldValues;

    private FieldExecutionContextBuilder() {
    }

    public static <E extends Enum<E>>
    FieldExecutionContextBuilder<E> builder() {

        return new FieldExecutionContextBuilder<>();
    }

    public FieldExecutionContextBuilder<E> page(
            Class<E> page) {

        this.page =
                Objects.requireNonNull(
                        page,
                        "Page cannot be null"
                );

        return this;
    }

    public FieldExecutionContextBuilder<E> operationMode(
            OperationMode operationMode) {

        this.operationMode =
                Objects.requireNonNull(
                        operationMode,
                        "Operation mode cannot be null"
                );

        return this;
    }

    public FieldExecutionContextBuilder<E> executionMode(
            ExecutionMode executionMode) {

        this.executionMode =
                Objects.requireNonNull(
                        executionMode,
                        "Execution mode cannot be null"
                );

        return this;
    }

    public FieldExecutionContextBuilder<E> currentUser(
            UserType currentUser) {

        this.currentUser =
                Objects.requireNonNull(
                        currentUser,
                        "Current user cannot be null"
                );

        return this;
    }

    public FieldExecutionContextBuilder<E> fieldValues(
            EnumMap<E, Object> fieldValues) {

        this.fieldValues =
                Objects.requireNonNull(
                        fieldValues,
                        "Field values cannot be null"
                );

        return this;
    }

    public FieldExecutionContext<E> build() {

        return new FieldExecutionContext<>(
                page,
                operationMode,
                executionMode,
                currentUser,
                fieldValues
        );
    }
}