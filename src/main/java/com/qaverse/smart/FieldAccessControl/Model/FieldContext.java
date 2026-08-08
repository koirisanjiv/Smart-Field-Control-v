package com.qaverse.smart.FieldAccessControl.Model;

import java.util.Objects;

import com.qaverse.smart.FieldAccessControl.Configuration.OperationMode;
import com.qaverse.smart.FieldAccessControl.Configuration.UserType;

public final class FieldContext<F extends Enum<F>> {

    private final Class<F> page;

    private final OperationMode operationMode;

    private final UserType userType;

    private final F field;

    public FieldContext(
            Class<F> page,
            OperationMode operationMode,
            UserType userType,
            F field) {

        this.page =
                Objects.requireNonNull(page);

        this.operationMode =
                Objects.requireNonNull(operationMode);

        this.userType =
                Objects.requireNonNull(userType);

        this.field =
                Objects.requireNonNull(field);
    }

    public Class<F> getPage() {
        return page;
    }

    public OperationMode getOperationMode() {
        return operationMode;
    }

    public UserType getUserType() {
        return userType;
    }

    public F getField() {
        return field;
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof FieldContext<?> other)) {
            return false;
        }

        return page.equals(other.page)
                && operationMode == other.operationMode
                && userType == other.userType
                && field == other.field;
    }

    @Override
    public int hashCode() {

        return Objects.hash(
                page,
                operationMode,
                userType,
                field
        );
    }
}