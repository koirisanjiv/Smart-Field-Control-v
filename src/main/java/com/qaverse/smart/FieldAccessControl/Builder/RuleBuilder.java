package com.qaverse.smart.FieldAccessControl.Builder;

import com.qaverse.smart.FieldAccessControl.Configuration.FieldBehavior;
import com.qaverse.smart.FieldAccessControl.Configuration.OperationMode;
import com.qaverse.smart.FieldAccessControl.Configuration.UserType;
import com.qaverse.smart.FieldAccessControl.Model.FieldContext;
import com.qaverse.smart.FieldAccessControl.Registry.FieldRegistry;
import com.qaverse.smart.logger.SmartLog;

public final class RuleBuilder<F extends Enum<F>> {

    private final Class<F> page;

    public RuleBuilder(Class<F> page) {
        this.page = page;

        SmartLog.debug(() ->
                "Rule builder created | page="
                + (page != null ? page.getSimpleName() : "null"));
    }

    public void editable(
            OperationMode mode,
            UserType userType,
            F... fields) {

        register(
                mode,
                userType,
                FieldBehavior.EDITABLE,
                fields
        );
    }

    public void readOnly(
            OperationMode mode,
            UserType userType,
            F... fields) {

        register(
                mode,
                userType,
                FieldBehavior.READ_ONLY,
                fields
        );
    }

    public void hidden(
            OperationMode mode,
            UserType userType,
            F... fields) {

        register(
                mode,
                userType,
                FieldBehavior.HIDDEN,
                fields
        );
    }

    private void register(
            OperationMode mode,
            UserType userType,
            FieldBehavior behavior,
            F... fields) {

        SmartLog.debug(() ->
                "Registering field rules | page="
                + page.getSimpleName()
                + " | operationMode="
                + mode
                + " | userType="
                + userType
                + " | behavior="
                + behavior
                + " | fieldCount="
                + fields.length);

        for (F field : fields) {

            FieldRegistry.register(
                    new FieldContext<>(
                            page,
                            mode,
                            userType,
                            field
                    ),
                    behavior
            );
        }

        SmartLog.debug(() ->
                "Field rules registered | page="
                + page.getSimpleName()
                + " | behavior="
                + behavior
                + " | fieldCount="
                + fields.length);
    }
}