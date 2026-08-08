package com.qaverse.smart.FieldAccessControl.Registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.qaverse.smart.FieldAccessControl.Model.FieldDefinition;

public final class FieldDefinitionRegistry {

    private FieldDefinitionRegistry() {
    }

    private static final Map<
            Class<? extends Enum<?>>,
            Map<Enum<?>, FieldDefinition>
            > DEFINITIONS = new ConcurrentHashMap<>();

    public static <E extends Enum<E>> void register(
            Class<E> page,
            E field,
            FieldDefinition definition) {

        DEFINITIONS
                .computeIfAbsent(
                        page,
                        p -> new ConcurrentHashMap<>()
                )
                .put(field, definition);
    }

    public static <E extends Enum<E>> FieldDefinition get(
            Class<E> page,
            E field) {

        Map<Enum<?>, FieldDefinition> pageDefinitions =
                DEFINITIONS.get(page);

        if (pageDefinitions == null) {
            return FieldDefinition.DEFAULT;
        }

        return pageDefinitions.getOrDefault(
                field,
                FieldDefinition.DEFAULT
        );
    }

    public static void clear() {
        DEFINITIONS.clear();
    }
}