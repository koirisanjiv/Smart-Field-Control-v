package com.qaverse.smart.FieldAccessControl.Execution;

public enum ExecutionMessage {

    FIELD_NULL(
            "Field cannot be null"
    ),

    EVALUATION_STATUS_NULL(
            "Evaluation status cannot be null"
    ),

    FIELD_VALUE_INVALID(
            "Field value is null, empty, or NA"
    ),

    FIELD_NOT_REGISTERED(
            "Field is not registered"
    ),

    FIELD_NOT_EDITABLE(
            "Field behavior is not EDITABLE"
    ),

    FIELD_CONDITION_NOT_SATISFIED(
            "Field condition is not satisfied"
    ),

    FIELD_PASSED_RULES(
            "Field passed all field-control rules"
    ),

    FIELD_NO_MANDATORY_DEFINITION(
            "Field has no mandatory definition"
    ),

    FIELD_NOT_MANDATORY(
            "Field is not mandatory"
    ),

    MANDATORY_FIELD_EXCLUDED_FROM_OPTIONAL(
            "Mandatory field is excluded from OPTIONAL_FIELDS"
    ),

    FIELD_NOT_ALLOWED_EXECUTION_MODE(
            "Field is not allowed by execution mode"
    ),

    CUSTOM_FIELDS_NOT_IMPLEMENTED(
            "CUSTOM_FIELDS is not implemented yet"
    ),

    UNSUPPORTED_EXECUTION_MODE(
            "Unsupported execution mode"
    );

    private final String message;

    ExecutionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}