package com.qaverse.smart.FieldAccessControl.Condition;

public enum ConditionMessage {

    CONTROLLER_FIELD_NULL("Controller field cannot be null"),
    CONDITION_OPERATOR_NULL("Condition operator cannot be null");

    private final String message;

    ConditionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}