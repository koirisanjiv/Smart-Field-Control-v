package com.qaverse.smart.FieldAccessControl.Execution;

public enum FieldEvaluationStatus {

    ALLOWED,

    SKIPPED_INVALID_VALUE,

    SKIPPED_NOT_REGISTERED,

    SKIPPED_NOT_EDITABLE,

    SKIPPED_EXECUTION_MODE,

    SKIPPED_CONDITION,

    SKIPPED_CUSTOM_FIELDS,
    
    SKIPPED_PARENT
}