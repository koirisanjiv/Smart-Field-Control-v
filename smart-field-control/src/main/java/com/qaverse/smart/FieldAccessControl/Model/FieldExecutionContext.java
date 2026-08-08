package com.qaverse.smart.FieldAccessControl.Model;

import java.util.EnumMap;

import com.qaverse.smart.FieldAccessControl.Configuration.ExecutionMode;
import com.qaverse.smart.FieldAccessControl.Configuration.OperationMode;
import com.qaverse.smart.FieldAccessControl.Configuration.UserType;

public final class FieldExecutionContext<E extends Enum<E>> {

    private final Class<E> page;

    private final OperationMode operationMode;

    private final ExecutionMode executionMode;

    private final UserType currentUser;

    private final EnumMap<E, Object> fieldValues;

    private final EnumMap<E, Runnable> fieldActions;

    public FieldExecutionContext(
            Class<E> page,
            OperationMode operationMode,
            ExecutionMode executionMode,
            UserType currentUser,
            EnumMap<E, Object> fieldValues,
            EnumMap<E, Runnable> fieldActions) {

        this.page = page;
        this.operationMode = operationMode;
        this.executionMode = executionMode;
        this.currentUser = currentUser;
        this.fieldValues = fieldValues;
        this.fieldActions = fieldActions;
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
        return fieldValues;
    }

    public EnumMap<E, Runnable> getFieldActions() {
        return fieldActions;
    }
}