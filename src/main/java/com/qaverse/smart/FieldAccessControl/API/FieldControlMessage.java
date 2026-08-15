package com.qaverse.smart.FieldAccessControl.API;

public enum FieldControlMessage {

    REQUEST_NULL(
            "Field control request cannot be null"),

    PAGE_NULL(
            "Page cannot be null"),

    OPERATION_MODE_NULL(
            "Operation mode cannot be null"),

    EXECUTION_MODE_NULL(
            "Execution mode cannot be null"),

    CURRENT_USER_NULL(
            "Current user cannot be null"),

    FIELD_VALUES_NULL(
            "Field values cannot be null"),

    OPERATION_MODE_REQUIRED(
            "Operation mode must be specified"),

    CURRENT_USER_REQUIRED(
            "Current user must be specified"),

    FIELD_VALUES_REQUIRED(
            "Field values must be specified"),

    EVALUATION_STARTED(
            "Field control evaluation started"),

    EVALUATION_COMPLETED(
            "Field control evaluation completed"),

    EVALUATION_FAILED(
            "Field control evaluation failed"),

    REGISTRY_CLEANUP_STARTED(
            "Clearing Field Control registries"),

    REGISTRY_CLEANUP_COMPLETED(
            "Field Control registries cleared");

    private final String message;

    FieldControlMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}