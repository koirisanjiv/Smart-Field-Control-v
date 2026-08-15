package com.qaverse.smart.FieldAccessControl.Builder;

public enum BuilderMessage {

    PAGE_NULL("Page cannot be null"),
    FIELD_CONDITION_NULL("Field condition cannot be null"),
    DEPENDENT_FIELD_REQUIRED("At least one dependent field is required"),

    OPERATION_MODE_NULL("Operation mode cannot be null"),
    EXECUTION_MODE_NULL("Execution mode cannot be null"),
    CURRENT_USER_NULL("Current user cannot be null"),
    FIELD_VALUES_NULL("Field values cannot be null");

    private final String message;

    BuilderMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}