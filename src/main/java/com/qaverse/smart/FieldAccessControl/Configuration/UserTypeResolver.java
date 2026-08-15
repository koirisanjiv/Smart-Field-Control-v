package com.qaverse.smart.FieldAccessControl.Configuration;

import com.qaverse.smart.logger.SmartLog;

public final class UserTypeResolver {

    private UserTypeResolver() {
    }

    public static UserType resolve(String value) {

        SmartLog.debug(() ->
                "Resolving user type | value="
                + value);

        if (value == null || value.isBlank()) {

            SmartLog.error(
                    ConfigurationMessage.USER_TYPE_NULL_OR_EMPTY::getMessage);

            throw new IllegalArgumentException(
                    ConfigurationMessage.USER_TYPE_NULL_OR_EMPTY.getMessage()
            );
        }

        for (UserType userType : UserType.values()) {

            if (userType.matches(value)) {

                SmartLog.debug(() ->
                        "User type resolved | value="
                        + value
                        + " | userType="
                        + userType);

                return userType;
            }
        }

        SmartLog.error(() ->
                ConfigurationMessage.UNSUPPORTED_USER_TYPE.getMessage()
                + value);

        throw new IllegalArgumentException(
                ConfigurationMessage.UNSUPPORTED_USER_TYPE.getMessage()
                + value
        );
    }
}