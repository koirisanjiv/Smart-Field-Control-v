package com.qaverse.smart.FieldAccessControl.Builder;

import com.qaverse.smart.FieldAccessControl.Model.FieldDefinition;
import com.qaverse.smart.FieldAccessControl.Registry.FieldDefinitionRegistry;
import com.qaverse.smart.logger.SmartLog;

public final class FieldDefinitionBuilder<E extends Enum<E>> {

    private final Class<E> page;

    public FieldDefinitionBuilder(Class<E> page) {
        this.page = page;

        SmartLog.debug(() ->
                "Field definition builder created | page="
                + (page != null ? page.getSimpleName() : "null"));
    }

    public void mandatory(E... fields) {

        SmartLog.debug(() ->
                "Registering mandatory fields | page="
                + page.getSimpleName()
                + " | fieldCount="
                + fields.length);

        for (E field : fields) {

            FieldDefinitionRegistry.register(
                    page,
                    field,
                    FieldDefinition.mandatory()
            );
        }

        SmartLog.debug(() ->
                "Mandatory fields registered | page="
                + page.getSimpleName()
                + " | fieldCount="
                + fields.length);
    }

    public void optional(E... fields) {

        SmartLog.debug(() ->
                "Registering optional fields | page="
                + page.getSimpleName()
                + " | fieldCount="
                + fields.length);

        for (E field : fields) {

            FieldDefinitionRegistry.register(
                    page,
                    field,
                    FieldDefinition.optional()
            );
        }

        SmartLog.debug(() ->
                "Optional fields registered | page="
                + page.getSimpleName()
                + " | fieldCount="
                + fields.length);
    }
}