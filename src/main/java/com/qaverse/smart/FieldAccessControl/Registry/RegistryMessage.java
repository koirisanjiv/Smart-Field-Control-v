package com.qaverse.smart.FieldAccessControl.Registry;

public enum RegistryMessage {

    DEPENDENT_FIELD_NULL(
            "Dependent field cannot be null"
    ),

    FIELD_CANNOT_CONTROL_ITSELF(
            "A field cannot control itself"
    ),

    CONDITION_REGISTERED(
            "Registered field condition"
    );

    private final String message;

    RegistryMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}