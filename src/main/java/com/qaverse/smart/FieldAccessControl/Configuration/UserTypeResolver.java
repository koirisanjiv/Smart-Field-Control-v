package com.qaverse.smart.FieldAccessControl.Configuration;

public final class UserTypeResolver {

    private UserTypeResolver() {
    }

    public static UserType resolve(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "User type cannot be null or empty"
            );
        }

        for (UserType userType : UserType.values()) {

            if (userType.matches(value)) {
                return userType;
            }
        }

        throw new IllegalArgumentException(
                "Unsupported user type: " + value
        );
    }
}