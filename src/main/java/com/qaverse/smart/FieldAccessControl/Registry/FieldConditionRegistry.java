package com.qaverse.smart.FieldAccessControl.Registry;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.qaverse.smart.FieldAccessControl.Builder.ConditionBuilder;
import com.qaverse.smart.FieldAccessControl.Condition.ConditionOperator;
import com.qaverse.smart.FieldAccessControl.Condition.FieldCondition;
import com.qaverse.smart.FieldAccessControl.Logging.FieldControlLogger;

public final class FieldConditionRegistry {

    private static final Map<Class<?>, Map<?, ?>> CONDITIONS =
            new ConcurrentHashMap<>();

    private FieldConditionRegistry() {
    }

    // =========================================================
    // CONDITION BUILDER
    // =========================================================

    public static <E extends Enum<E>> ConditionBuilder<E> when(
            Class<E> page,
            E controllerField,
            ConditionOperator operator) {

        return new ConditionBuilder<>(
                page,
                new FieldCondition<>(
                        controllerField,
                        operator
                )
        );
    }

    public static <E extends Enum<E>> ConditionBuilder<E> when(
            Class<E> page,
            E controllerField,
            ConditionOperator operator,
            Object expectedValue) {

        return new ConditionBuilder<>(
                page,
                new FieldCondition<>(
                        controllerField,
                        operator,
                        expectedValue
                )
        );
    }

    // =========================================================
    // REGISTER
    // =========================================================

    public static <E extends Enum<E>> void register(
            Class<E> page,
            FieldCondition<E> condition,
            E... dependentFields) {

        Map<E, FieldCondition<E>> map =
                getOrCreateMap(page);

        for (E dependentField : dependentFields) {

            if (dependentField == null) {

                throw new IllegalArgumentException(
                        RegistryMessage.DEPENDENT_FIELD_NULL.getMessage()
                );
            }

            if (dependentField.equals(
                    condition.getControllerField())) {

                throw new IllegalArgumentException(
                        RegistryMessage.FIELD_CANNOT_CONTROL_ITSELF.getMessage()
                        + ": "
                        + dependentField
                );
            }

            map.put(
                    dependentField,
                    condition
            );

            /*
             * Lazy logging:
             *
             * The message is constructed only when DEBUG logging
             * is actually enabled.
             */
            FieldControlLogger.debug(() ->
                    RegistryMessage.CONDITION_REGISTERED.getMessage()
                    + " | page="
                    + page.getSimpleName()
                    + " | controller="
                    + condition.getControllerField()
                    + " | operator="
                    + condition.getOperator()
                    + " | expected="
                    + condition.getExpectedValue()
                    + " | dependent="
                    + dependentField
            );
        }
    }

    // =========================================================
    // GET
    // =========================================================

    public static <E extends Enum<E>> FieldCondition<E> get(
            Class<E> page,
            E field) {

        Map<E, FieldCondition<E>> map =
                getMap(page);

        if (map == null) {
            return null;
        }

        return map.get(field);
    }

    // =========================================================
    // CHECK
    // =========================================================

    public static <E extends Enum<E>> boolean isRegistered(
            Class<E> page,
            E field) {

        return get(page, field) != null;
    }

    // =========================================================
    // INTERNAL MAP
    // =========================================================

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> Map<E, FieldCondition<E>>
    getOrCreateMap(Class<E> page) {

        return (Map<E, FieldCondition<E>>)
                CONDITIONS.computeIfAbsent(
                        page,
                        key -> new EnumMap<E, FieldCondition<E>>(page)
                );
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> Map<E, FieldCondition<E>>
    getMap(Class<E> page) {

        return (Map<E, FieldCondition<E>>)
                CONDITIONS.get(page);
    }

    // =========================================================
    // CLEAR
    // =========================================================

    public static void clear() {

        CONDITIONS.clear();

        FieldControlLogger.debug(() ->
                "Field condition registry cleared"
        );
    }
}