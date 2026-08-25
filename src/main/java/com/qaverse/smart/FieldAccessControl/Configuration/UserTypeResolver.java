package com.qaverse.smart.FieldAccessControl.Configuration;

import com.qaverse.smart.FieldAccessControl.Logging.FieldControlLogger;

public final class UserTypeResolver {

    private UserTypeResolver() {
    }

    public static UserType resolve(String value) {

        FieldControlLogger.debug(() ->
                "Resolving user type | value="
                + value);

        if (value == null || value.isBlank()) {

            FieldControlLogger.error(
                    ConfigurationMessage.USER_TYPE_NULL_OR_EMPTY::getMessage);

            throw new IllegalArgumentException(
                    ConfigurationMessage.USER_TYPE_NULL_OR_EMPTY.getMessage()
            );
        }

        for (UserType userType : UserType.values()) {

            if (userType.matches(value)) {

                FieldControlLogger.debug(() ->
                        "User type resolved | value="
                        + value
                        + " | userType="
                        + userType);

                return userType;
            }
        }

        FieldControlLogger.error(() ->
                ConfigurationMessage.UNSUPPORTED_USER_TYPE.getMessage()
                + value);

        throw new IllegalArgumentException(
                ConfigurationMessage.UNSUPPORTED_USER_TYPE.getMessage()
                + value
        );
    }
}