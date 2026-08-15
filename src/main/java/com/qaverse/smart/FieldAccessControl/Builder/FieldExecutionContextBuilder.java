package com.qaverse.smart.FieldAccessControl.Builder;

import java.util.EnumMap;
import java.util.Objects;

import com.qaverse.smart.FieldAccessControl.Configuration.ExecutionMode;
import com.qaverse.smart.FieldAccessControl.Configuration.OperationMode;
import com.qaverse.smart.FieldAccessControl.Configuration.UserType;
import com.qaverse.smart.FieldAccessControl.Model.FieldExecutionContext;
import com.qaverse.smart.logger.SmartLog;

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

        SmartLog.debug("Creating field execution context builder");

        return new FieldExecutionContextBuilder<>();
    }

    public FieldExecutionContextBuilder<E> page(
            Class<E> page) {

        this.page =
                Objects.requireNonNull(
                        page,
                        BuilderMessage.PAGE_NULL.getMessage()
                );

        SmartLog.debug(() ->
                "Field execution context page configured | page="
                + page.getSimpleName());

        return this;
    }

    public FieldExecutionContextBuilder<E> operationMode(
            OperationMode operationMode) {

        this.operationMode =
                Objects.requireNonNull(
                        operationMode,
                        BuilderMessage.OPERATION_MODE_NULL.getMessage()
                );

        SmartLog.debug(() ->
                "Field execution context operation mode configured | mode="
                + operationMode);

        return this;
    }

    public FieldExecutionContextBuilder<E> executionMode(
            ExecutionMode executionMode) {

        this.executionMode =
                Objects.requireNonNull(
                        executionMode,
                        BuilderMessage.EXECUTION_MODE_NULL.getMessage()
                );

        SmartLog.debug(() ->
                "Field execution context execution mode configured | mode="
                + executionMode);

        return this;
    }

    public FieldExecutionContextBuilder<E> currentUser(
            UserType currentUser) {

        this.currentUser =
                Objects.requireNonNull(
                        currentUser,
                        BuilderMessage.CURRENT_USER_NULL.getMessage()
                );

        SmartLog.debug(() ->
                "Field execution context user configured | user="
                + currentUser);

        return this;
    }

    public FieldExecutionContextBuilder<E> fieldValues(
            EnumMap<E, Object> fieldValues) {

        this.fieldValues =
                Objects.requireNonNull(
                        fieldValues,
                        BuilderMessage.FIELD_VALUES_NULL.getMessage()
                );

        SmartLog.debug(() ->
                "Field execution context values configured | fieldCount="
                + fieldValues.size());

        return this;
    }

    public FieldExecutionContext<E> build() {

        SmartLog.debug(() ->
                "Building field execution context | page="
                + (page != null ? page.getSimpleName() : "null")
                + " | operationMode="
                + operationMode
                + " | executionMode="
                + executionMode
                + " | currentUser="
                + currentUser
                + " | fieldCount="
                + (fieldValues != null ? fieldValues.size() : 0));

        return new FieldExecutionContext<>(
                page,
                operationMode,
                executionMode,
                currentUser,
                fieldValues
        );
    }
}