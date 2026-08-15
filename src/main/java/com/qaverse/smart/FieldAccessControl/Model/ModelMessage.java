package com.qaverse.smart.FieldAccessControl.Model;

public enum ModelMessage {

    PAGE_NULL(
            "Page cannot be null"
    ),

    OPERATION_MODE_NULL(
            "Operation mode cannot be null"
    ),

    EXECUTION_MODE_NULL(
            "Execution mode cannot be null"
    ),

    CURRENT_USER_NULL(
            "Current user cannot be null"
    ),

    FIELD_VALUES_NULL(
            "Field values cannot be null"
    ),

    DECISIONS_NULL(
            "Decisions cannot be null"
    ),

    FIELD_NULL(
            "Field cannot be null"
    ),

    EVALUATION_STATUS_NULL(
            "Evaluation status cannot be null"
    );

    private final String message;

    ModelMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}