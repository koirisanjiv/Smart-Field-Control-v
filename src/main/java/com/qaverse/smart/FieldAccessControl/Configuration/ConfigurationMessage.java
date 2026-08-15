package com.qaverse.smart.FieldAccessControl.Configuration;

public enum ConfigurationMessage {

    USER_TYPE_NULL_OR_EMPTY("User type cannot be null or empty"),
    UNSUPPORTED_USER_TYPE("Unsupported user type: ");

    private final String message;

    ConfigurationMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}