package com.qaverse.smart.FieldAccessControl.API;

import java.util.EnumMap;
import java.util.Objects;

import com.qaverse.smart.FieldAccessControl.Configuration.ExecutionMode;
import com.qaverse.smart.FieldAccessControl.Configuration.OperationMode;
import com.qaverse.smart.FieldAccessControl.Configuration.UserType;
import com.qaverse.smart.logger.SmartLog;

public final class FieldControlRequest<E extends Enum<E>> {

    private final Class<E> page;
    private final OperationMode operationMode;
    private final ExecutionMode executionMode;
    private final UserType currentUser;
    private final EnumMap<E, Object> fieldValues;

    private FieldControlRequest(
            Class<E> page,
            OperationMode operationMode,
            ExecutionMode executionMode,
            UserType currentUser,
            EnumMap<E, Object> fieldValues) {

        this.page = Objects.requireNonNull(
                page,
                FieldControlMessage.PAGE_NULL.getMessage());

        this.operationMode = Objects.requireNonNull(
                operationMode,
                FieldControlMessage.OPERATION_MODE_NULL.getMessage());

        this.executionMode = Objects.requireNonNull(
                executionMode,
                FieldControlMessage.EXECUTION_MODE_NULL.getMessage());

        this.currentUser = Objects.requireNonNull(
                currentUser,
                FieldControlMessage.CURRENT_USER_NULL.getMessage());

        Objects.requireNonNull(
                fieldValues,
                FieldControlMessage.FIELD_VALUES_NULL.getMessage());

        this.fieldValues = new EnumMap<>(fieldValues);
    }

    public static <E extends Enum<E>> Builder<E> builder(Class<E> page) {

        return new Builder<>(page);
    }

    public Class<E> getPage() {
        return page;
    }

    public OperationMode getOperationMode() {
        return operationMode;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public UserType getCurrentUser() {
        return currentUser;
    }

    public EnumMap<E, Object> getFieldValues() {
        return new EnumMap<>(fieldValues);
    }

    public static final class Builder<E extends Enum<E>> {

        private final Class<E> page;

        private OperationMode operationMode;

        private ExecutionMode executionMode = ExecutionMode.ALL_FIELDS;

        private UserType currentUser;

        private EnumMap<E, Object> fieldValues;

        private Builder(Class<E> page) {

            this.page = Objects.requireNonNull(
                    page,
                    FieldControlMessage.PAGE_NULL.getMessage());
        }

        public Builder<E> operationMode(OperationMode operationMode) {

            this.operationMode = Objects.requireNonNull(
                    operationMode,
                    FieldControlMessage.OPERATION_MODE_NULL.getMessage());

            return this;
        }

        public Builder<E> executionMode(ExecutionMode executionMode) {

            this.executionMode = Objects.requireNonNull(
                    executionMode,
                    FieldControlMessage.EXECUTION_MODE_NULL.getMessage());

            return this;
        }

        public Builder<E> currentUser(UserType currentUser) {

            this.currentUser = Objects.requireNonNull(
                    currentUser,
                    FieldControlMessage.CURRENT_USER_NULL.getMessage());

            return this;
        }

        public Builder<E> fieldValues(EnumMap<E, Object> fieldValues) {

            this.fieldValues = Objects.requireNonNull(
                    fieldValues,
                    FieldControlMessage.FIELD_VALUES_NULL.getMessage());

            return this;
        }

        public FieldControlRequest<E> build() {

            Objects.requireNonNull(
                    operationMode,
                    FieldControlMessage.OPERATION_MODE_REQUIRED.getMessage());

            Objects.requireNonNull(
                    currentUser,
                    FieldControlMessage.CURRENT_USER_REQUIRED.getMessage());

            Objects.requireNonNull(
                    fieldValues,
                    FieldControlMessage.FIELD_VALUES_REQUIRED.getMessage());

            return new FieldControlRequest<>(
                    page,
                    operationMode,
                    executionMode,
                    currentUser,
                    fieldValues);
        }
    }
}