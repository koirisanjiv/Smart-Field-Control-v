package com.qaverse.smart.FieldAccessControl.Builder;

import com.qaverse.smart.FieldAccessControl.Model.FieldDefinition;
import com.qaverse.smart.FieldAccessControl.Registry.FieldDefinitionRegistry;

public final class FieldDefinitionBuilder<E extends Enum<E>> {

    private final Class<E> page;

    public FieldDefinitionBuilder(Class<E> page) {
        this.page = page;
    }

    public void mandatory(E... fields) {

        for (E field : fields) {

            FieldDefinitionRegistry.register(
                    page,
                    field,
                    FieldDefinition.mandatory()
            );
        }
    }

    public void optional(E... fields) {

        for (E field : fields) {

            FieldDefinitionRegistry.register(
                    page,
                    field,
                    FieldDefinition.optional()
            );
        }
    }
}