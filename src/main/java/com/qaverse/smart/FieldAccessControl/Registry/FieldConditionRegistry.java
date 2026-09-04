//package com.qaverse.smart.FieldAccessControl.Registry;
//
//import java.util.EnumMap;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//import com.qaverse.smart.FieldAccessControl.Builder.ConditionBuilder;
//import com.qaverse.smart.FieldAccessControl.Condition.ConditionOperator;
//import com.qaverse.smart.FieldAccessControl.Condition.FieldCondition;
//import com.qaverse.smart.FieldAccessControl.Logging.FieldControlLogger;
//
//public final class FieldConditionRegistry {
//
//    private static final Map<Class<?>, Map<?, ?>> CONDITIONS =
//            new ConcurrentHashMap<>();
//
//    private FieldConditionRegistry() {
//    }
//
//    // =========================================================
//    // CONDITION BUILDER
//    // =========================================================
//
//    public static <E extends Enum<E>> ConditionBuilder<E> when(
//            Class<E> page,
//            E controllerField,
//            ConditionOperator operator) {
//
//        return new ConditionBuilder<>(
//                page,
//                new FieldCondition<>(
//                        controllerField,
//                        operator
//                )
//        );
//    }
//
//    public static <E extends Enum<E>> ConditionBuilder<E> when(
//            Class<E> page,
//            E controllerField,
//            ConditionOperator operator,
//            Object expectedValue) {
//
//        return new ConditionBuilder<>(
//                page,
//                new FieldCondition<>(
//                        controllerField,
//                        operator,
//                        expectedValue
//                )
//        );
//    }
//
//    // =========================================================
//    // REGISTER
//    // =========================================================
//
//    public static <E extends Enum<E>> void register(
//            Class<E> page,
//            FieldCondition<E> condition,
//            E... dependentFields) {
//
//        Map<E, FieldCondition<E>> map =
//                getOrCreateMap(page);
//
//        for (E dependentField : dependentFields) {
//
//            if (dependentField == null) {
//
//                throw new IllegalArgumentException(
//                        RegistryMessage.DEPENDENT_FIELD_NULL.getMessage()
//                );
//            }
//
//            if (dependentField.equals(
//                    condition.getControllerField())) {
//
//                throw new IllegalArgumentException(
//                        RegistryMessage.FIELD_CANNOT_CONTROL_ITSELF.getMessage()
//                        + ": "
//                        + dependentField
//                );
//            }
//
//            map.put(
//                    dependentField,
//                    condition
//            );
//
//            /*
//             * Lazy logging:
//             *
//             * The message is constructed only when DEBUG logging
//             * is actually enabled.
//             */
//            FieldControlLogger.debug(() ->
//                    RegistryMessage.CONDITION_REGISTERED.getMessage()
//                    + " | page="
//                    + page.getSimpleName()
//                    + " | controller="
//                    + condition.getControllerField()
//                    + " | operator="
//                    + condition.getOperator()
//                    + " | expected="
//                    + condition.getExpectedValue()
//                    + " | dependent="
//                    + dependentField
//            );
//        }
//    }
//
//    // =========================================================
//    // GET
//    // =========================================================
//
//    public static <E extends Enum<E>> FieldCondition<E> get(
//            Class<E> page,
//            E field) {
//
//        Map<E, FieldCondition<E>> map =
//                getMap(page);
//
//        if (map == null) {
//            return null;
//        }
//
//        return map.get(field);
//    }
//
//    // =========================================================
//    // CHECK
//    // =========================================================
//
//    public static <E extends Enum<E>> boolean isRegistered(
//            Class<E> page,
//            E field) {
//
//        return get(page, field) != null;
//    }
//
//    // =========================================================
//    // INTERNAL MAP
//    // =========================================================
//
//    @SuppressWarnings("unchecked")
//    private static <E extends Enum<E>> Map<E, FieldCondition<E>>
//    getOrCreateMap(Class<E> page) {
//
//        return (Map<E, FieldCondition<E>>)
//                CONDITIONS.computeIfAbsent(
//                        page,
//                        key -> new EnumMap<E, FieldCondition<E>>(page)
//                );
//    }
//
//    @SuppressWarnings("unchecked")
//    private static <E extends Enum<E>> Map<E, FieldCondition<E>>
//    getMap(Class<E> page) {
//
//        return (Map<E, FieldCondition<E>>)
//                CONDITIONS.get(page);
//    }
//
//    // =========================================================
//    // CLEAR
//    // =========================================================
//
//    public static void clear() {
//
//        CONDITIONS.clear();
//
//        FieldControlLogger.debug(() ->
//                "Field condition registry cleared"
//        );
//    }
//}




package com.qaverse.smart.FieldAccessControl.Registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.qaverse.smart.FieldAccessControl.Builder.ConditionBuilder;
import com.qaverse.smart.FieldAccessControl.Condition.ConditionOperator;
import com.qaverse.smart.FieldAccessControl.Condition.FieldCondition;
import com.qaverse.smart.FieldAccessControl.Logging.FieldControlLogger;

public final class FieldConditionRegistry {

    /*
     * =========================================================
     * CONDITION STORAGE
     *
     * OR conditions:
     *
     *   condition1 OR condition2 OR condition3
     *
     * AND groups:
     *
     *   (condition1 AND condition2)
     *
     * A field may have multiple AND groups.
     * Different AND groups are OR'ed with each other.
     * =========================================================
     */

	private static final Map<Class<?>, Object> CONDITIONS =
	        new ConcurrentHashMap<>();

    private FieldConditionRegistry() {
    }

    // =========================================================
    // CONDITION BUILDER - OR
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
    // REGISTER - OR
    // =========================================================

    public static <E extends Enum<E>> void register(
            Class<E> page,
            FieldCondition<E> condition,
            E... dependentFields) {

        Map<E, ConditionSet<E>> map =
                getOrCreateMap(page);

        for (E dependentField : dependentFields) {

            validateDependentField(
                    dependentField,
                    condition
            );

            ConditionSet<E> conditionSet =
                    map.computeIfAbsent(
                            dependentField,
                            key -> new ConditionSet<>()
                    );

            conditionSet.addAny(condition);

            FieldControlLogger.debug(() ->
                    RegistryMessage.CONDITION_REGISTERED.getMessage()
                    + " | page="
                    + page.getSimpleName()
                    + " | type=OR"
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
    // REGISTER - AND
    // =========================================================

    /**
     * Registers multiple conditions that must ALL be satisfied
     * for the dependent field to be allowed.
     *
     * Example:
     *
     *   WANT_TO_ADD_START_CHECKPOINT == TRUE
     *   AND
     *   SEARCH_LOCATION == Address Book
     *
     *       ->
     *
     *   START_CHECKPOINT_POI
     */
    @SafeVarargs
    public static <E extends Enum<E>> void registerAll(
            Class<E> page,
            E dependentField,
            FieldCondition<E>... conditions) {

        if (dependentField == null) {

            throw new IllegalArgumentException(
                    RegistryMessage.DEPENDENT_FIELD_NULL.getMessage()
            );
        }

        if (conditions == null || conditions.length == 0) {

            throw new IllegalArgumentException(
                    "At least one condition is required"
            );
        }

        for (FieldCondition<E> condition : conditions) {

            if (condition == null) {

                throw new IllegalArgumentException(
                        "Condition cannot be null"
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
        }

        Map<E, ConditionSet<E>> map =
                getOrCreateMap(page);

        ConditionSet<E> conditionSet =
                map.computeIfAbsent(
                        dependentField,
                        key -> new ConditionSet<>()
                );

        conditionSet.addAll(
                List.of(conditions)
        );

        FieldControlLogger.debug(() ->
                "AND condition group registered"
                + " | page="
                + page.getSimpleName()
                + " | dependent="
                + dependentField
                + " | conditionCount="
                + conditions.length
        );
    }

    // =========================================================
    // GET - BACKWARD COMPATIBILITY
    // =========================================================

    /**
     * Returns the first OR condition for backward compatibility.
     *
     * New evaluator code should use getConditions().
     */
    public static <E extends Enum<E>> FieldCondition<E> get(
            Class<E> page,
            E field) {

        ConditionSet<E> conditionSet =
                getConditionSet(page, field);

        if (conditionSet == null) {
            return null;
        }

        return conditionSet.getFirstAny();
    }

    // =========================================================
    // GET ALL CONDITIONS
    // =========================================================

    public static <E extends Enum<E>> List<FieldCondition<E>> getAnyConditions(
            Class<E> page,
            E field) {

        ConditionSet<E> conditionSet =
                getConditionSet(page, field);

        if (conditionSet == null) {
            return Collections.emptyList();
        }

        return conditionSet.getAnyConditions();
    }

    public static <E extends Enum<E>> List<List<FieldCondition<E>>> getAllConditions(
            Class<E> page,
            E field) {

        ConditionSet<E> conditionSet =
                getConditionSet(page, field);

        if (conditionSet == null) {
            return Collections.emptyList();
        }

        return conditionSet.getAllConditions();
    }

    // =========================================================
    // CHECK
    // =========================================================

    public static <E extends Enum<E>> boolean isRegistered(
            Class<E> page,
            E field) {

        return getConditionSet(page, field) != null;
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private static <E extends Enum<E>> void validateDependentField(
            E dependentField,
            FieldCondition<E> condition) {

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
    }

    // =========================================================
    // INTERNAL MAP
    // =========================================================

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> Map<E, ConditionSet<E>>
    getOrCreateMap(Class<E> page) {

        Object existing = CONDITIONS.get(page);

        if (existing != null) {
            return (Map<E, ConditionSet<E>>) existing;
        }

        Map<E, ConditionSet<E>> newMap =
                new EnumMap<>(page);

        Object previous =
                CONDITIONS.putIfAbsent(page, newMap);

        if (previous != null) {
            return (Map<E, ConditionSet<E>>) previous;
        }

        return newMap;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> ConditionSet<E>
    getConditionSet(
            Class<E> page,
            E field) {

        Object existing =
                CONDITIONS.get(page);

        if (existing == null) {
            return null;
        }

        Map<E, ConditionSet<E>> map =
                (Map<E, ConditionSet<E>>) existing;

        return map.get(field);
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

    // =========================================================
    // CONDITION SET
    // =========================================================

    private static final class ConditionSet<E extends Enum<E>> {

        /*
         * Multiple normal "when()" conditions are OR'ed.
         */
        private final List<FieldCondition<E>> anyConditions =
                new ArrayList<>();

        /*
         * Every group inside this list is AND.
         *
         * Example:
         *
         * [
         *   [A, B],
         *   [C, D]
         * ]
         *
         * means:
         *
         * (A AND B) OR (C AND D)
         */
        private final List<List<FieldCondition<E>>> allConditions =
                new ArrayList<>();

        private void addAny(
                FieldCondition<E> condition) {

            anyConditions.add(condition);
        }

        private void addAll(
                List<FieldCondition<E>> conditions) {

            allConditions.add(
                    Collections.unmodifiableList(
                            new ArrayList<>(conditions)
                    )
            );
        }

        private FieldCondition<E> getFirstAny() {

            if (anyConditions.isEmpty()) {
                return null;
            }

            return anyConditions.get(0);
        }

        private List<FieldCondition<E>> getAnyConditions() {

            return Collections.unmodifiableList(
                    new ArrayList<>(anyConditions)
            );
        }

        private List<List<FieldCondition<E>>> getAllConditions() {

            List<List<FieldCondition<E>>> copy =
                    new ArrayList<>();

            for (List<FieldCondition<E>> group :
                    allConditions) {

                copy.add(
                        Collections.unmodifiableList(
                                new ArrayList<>(group)
                        )
                );
            }

            return Collections.unmodifiableList(copy);
        }
    }
}