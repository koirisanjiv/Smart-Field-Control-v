package com.qaverse.smart.FieldAccessControl.Execution;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.qaverse.smart.FieldAccessControl.Condition.ConditionEvaluator;
import com.qaverse.smart.FieldAccessControl.Condition.FieldCondition;
import com.qaverse.smart.FieldAccessControl.Configuration.FieldBehavior;
import com.qaverse.smart.FieldAccessControl.Logging.FieldControlLogger;
import com.qaverse.smart.FieldAccessControl.Model.FieldContext;
import com.qaverse.smart.FieldAccessControl.Model.FieldDecision;
import com.qaverse.smart.FieldAccessControl.Model.FieldDefinition;
import com.qaverse.smart.FieldAccessControl.Model.FieldExecutionContext;
import com.qaverse.smart.FieldAccessControl.Model.FieldExecutionPlan;
import com.qaverse.smart.FieldAccessControl.Registry.FieldConditionRegistry;
import com.qaverse.smart.FieldAccessControl.Registry.FieldDefinitionRegistry;
import com.qaverse.smart.FieldAccessControl.Registry.FieldGroupRegistry;
import com.qaverse.smart.FieldAccessControl.Registry.FieldRegistry;

public final class FieldEvaluator {

    private FieldEvaluator() {
    }

    /**
     * Evaluates all supplied fields against Smart Field Control rules.
     *
     * <p>
     * This class does NOT execute any UI action.
     * </p>
     *
     * <p>
     * It only determines whether each field is allowed to proceed and provides
     * the reason when it is skipped.
     * </p>
     */
    public static <E extends Enum<E>> FieldExecutionPlan<E> evaluate(
            FieldExecutionContext<E> context) {

        Class<E> page = context.getPage();

        EnumMap<E, Object> fieldValues =
                context.getFieldValues();

        FieldControlLogger.debug(() ->
                "Field control evaluation started"
                + " | page=" + page.getSimpleName()
                + " | operationMode=" + context.getOperationMode()
                + " | executionMode=" + context.getExecutionMode()
                + " | currentUser=" + context.getCurrentUser()
                + " | fieldCount=" + fieldValues.size()
        );

        List<FieldDecision<E>> decisions =
                new ArrayList<>();

        /*
         * EnumMap iteration follows enum declaration order.
         */
        for (Map.Entry<E, Object> entry :
                fieldValues.entrySet()) {

            E field = entry.getKey();

            Object value = entry.getValue();

            FieldEvaluationResult<E> result =
                    evaluateField(
                            context,
                            page,
                            field,
                            value,
                            fieldValues
                    );

            decisions.add(result.toDecision());

            FieldControlLogger.debug(() ->
                    "Field evaluation completed"
                    + " | page=" + page.getSimpleName()
                    + " | field=" + field
                    + " | status=" + result.getStatus()
                    + " | behavior=" + result.getBehavior()
                    + " | reason=" + result.getReason()
            );
        }

        FieldExecutionPlan<E> plan =
                new FieldExecutionPlan<>(
                        page,
                        decisions
                );

        FieldControlLogger.debug(() ->
                "Field control evaluation completed"
                + " | page=" + page.getSimpleName()
                + " | decisions=" + decisions.size()
        );

        return plan;
    }

    /**
     * Evaluates a single field.
     */
    private static <E extends Enum<E>> FieldEvaluationResult<E> evaluateField(
            FieldExecutionContext<E> context,
            Class<E> page,
            E field,
            Object value,
            EnumMap<E, Object> fieldValues) {

        /*
         * =====================================================
         * 1. Validate value
         * =====================================================
         */

        if (!isValidValue(value)) {

            FieldControlLogger.debug(() ->
                    "Field evaluation skipped"
                    + " | field=" + field
                    + " | reason="
                    + ExecutionMessage.FIELD_VALUE_INVALID
                            .getMessage()
            );

            return FieldEvaluationResult.skipped(
                    field,
                    value,
                    FieldEvaluationStatus.SKIPPED_INVALID_VALUE,
                    null,
                    ExecutionMessage.FIELD_VALUE_INVALID
                            .getMessage()
            );
        }

        /*
         * =====================================================
         * 2. Create field context
         * =====================================================
         */

        FieldContext<E> fieldContext =
                new FieldContext<>(
                        page,
                        context.getOperationMode(),
                        context.getCurrentUser(),
                        field
                );

        /*
         * =====================================================
         * 3. Field must be registered
         * =====================================================
         */

        if (!FieldRegistry.isRegistered(fieldContext)) {

            FieldControlLogger.debug(() ->
                    "Field evaluation skipped"
                    + " | field=" + field
                    + " | reason="
                    + ExecutionMessage.FIELD_NOT_REGISTERED
                            .getMessage()
            );

            return FieldEvaluationResult.skipped(
                    field,
                    value,
                    FieldEvaluationStatus.SKIPPED_NOT_REGISTERED,
                    null,
                    ExecutionMessage.FIELD_NOT_REGISTERED
                            .getMessage()
            );
        }

        /*
         * =====================================================
         * 4. Resolve field behavior
         * =====================================================
         */

        FieldBehavior behavior =
                FieldRegistry.getBehavior(fieldContext);

        /*
         * =====================================================
         * 5. Field must be editable for execution
         * =====================================================
         */

        if (behavior != FieldBehavior.EDITABLE) {

            String reason =
                    ExecutionMessage.FIELD_NOT_EDITABLE
                            .getMessage()
                    + " | behavior=" + behavior;

            FieldControlLogger.debug(() ->
                    "Field evaluation skipped"
                    + " | field=" + field
                    + " | reason=" + reason
            );

            return FieldEvaluationResult.skipped(
                    field,
                    value,
                    FieldEvaluationStatus.SKIPPED_NOT_EDITABLE,
                    behavior,
                    reason
            );
        }

        /*
         * =====================================================
         * 6. Resolve field definition
         * =====================================================
         */

        FieldDefinition definition =
                FieldDefinitionRegistry.get(
                        page,
                        field
                );

        /*
         * =====================================================
         * 7. Check execution mode
         * =====================================================
         */

        FieldEvaluationStatus executionStatus =
                evaluateExecutionMode(
                        context,
                        definition
                );

        if (executionStatus != null) {

            String reason =
                    getExecutionModeReason(
                            context,
                            definition
                    );

            FieldControlLogger.debug(() ->
                    "Field evaluation skipped"
                    + " | field=" + field
                    + " | executionMode="
                    + context.getExecutionMode()
                    + " | reason=" + reason
            );

            return FieldEvaluationResult.skipped(
                    field,
                    value,
                    executionStatus,
                    behavior,
                    reason
            );
        }

        /*
         * =====================================================
         * 8. Check parent field group
         * =====================================================
         *
         * If this field belongs to a parent group:
         *
         *     Parent TRUE  -> continue
         *     Parent FALSE -> SKIPPED_PARENT
         *
         * For multiple controllers:
         *
         *     Controller A TRUE
         *              OR
         *     Controller B TRUE
         *              OR
         *     Controller C TRUE
         *
         * Any TRUE means the child is active.
         */

        List<E> parentControllers =
                FieldGroupRegistry.getControllers(
                        page,
                        field
                );

        if (!parentControllers.isEmpty()) {

            boolean parentActive =
                    isParentGroupActive(
                            page,
                            field,
                            parentControllers,
                            fieldValues
                    );

            if (!parentActive) {

                String reason =
                        "Parent controller is not active"
                        + " | controllers="
                        + parentControllers;

                FieldControlLogger.debug(() ->
                        "Field evaluation skipped"
                        + " | field=" + field
                        + " | status="
                        + FieldEvaluationStatus.SKIPPED_PARENT
                        + " | reason=" + reason
                );

                return FieldEvaluationResult.skipped(
                        field,
                        value,
                        FieldEvaluationStatus.SKIPPED_PARENT,
                        behavior,
                        reason
                );
            }
        }

        /*
         * =====================================================
         * 9. Check conditional dependency
         * =====================================================
         */

        if (!isConditionSatisfied(
                page,
                field,
                fieldValues)) {

            String reason =
                    ExecutionMessage
                            .FIELD_CONDITION_NOT_SATISFIED
                            .getMessage();

            FieldControlLogger.debug(() ->
                    "Field evaluation skipped"
                    + " | field=" + field
                    + " | status="
                    + FieldEvaluationStatus.SKIPPED_CONDITION
                    + " | reason=" + reason
            );

            return FieldEvaluationResult.skipped(
                    field,
                    value,
                    FieldEvaluationStatus.SKIPPED_CONDITION,
                    behavior,
                    reason
            );
        }

        /*
         * =====================================================
         * 10. Field passed all rules
         * =====================================================
         */

        FieldControlLogger.debug(() ->
                "Field passed all field-control rules"
                + " | page=" + page.getSimpleName()
                + " | field=" + field
        );

        return FieldEvaluationResult.allowed(
                field,
                value,
                behavior
        );
    }

    // =========================================================
    // VALUE VALIDATION
    // =========================================================

    private static boolean isValidValue(Object value) {

        if (value == null) {
            return false;
        }

        String text =
                value.toString().trim();

        if (text.isEmpty()) {
            return false;
        }

        return !"NA".equalsIgnoreCase(text);
    }

    // =========================================================
    // EXECUTION MODE
    // =========================================================

    private static <E extends Enum<E>>
    FieldEvaluationStatus evaluateExecutionMode(
            FieldExecutionContext<E> context,
            FieldDefinition definition) {

        switch (context.getExecutionMode()) {

        case ALL_FIELDS:

            return null;

        case MANDATORY_FIELDS:

            if (definition == null
                    || !definition.isMandatory()) {

                return FieldEvaluationStatus
                        .SKIPPED_EXECUTION_MODE;
            }

            return null;

        case OPTIONAL_FIELDS:

            if (definition != null
                    && definition.isMandatory()) {

                return FieldEvaluationStatus
                        .SKIPPED_EXECUTION_MODE;
            }

            return null;

        case CUSTOM_FIELDS:

            return FieldEvaluationStatus
                    .SKIPPED_CUSTOM_FIELDS;

        default:

            return FieldEvaluationStatus
                    .SKIPPED_EXECUTION_MODE;
        }
    }

    private static <E extends Enum<E>>
    String getExecutionModeReason(
            FieldExecutionContext<E> context,
            FieldDefinition definition) {

        switch (context.getExecutionMode()) {

        case MANDATORY_FIELDS:

            return definition == null
                    ? ExecutionMessage
                            .FIELD_NO_MANDATORY_DEFINITION
                            .getMessage()
                    : ExecutionMessage
                            .FIELD_NOT_MANDATORY
                            .getMessage();

        case OPTIONAL_FIELDS:

            return definition != null
                    && definition.isMandatory()
                    ? ExecutionMessage
                            .MANDATORY_FIELD_EXCLUDED_FROM_OPTIONAL
                            .getMessage()
                    : ExecutionMessage
                            .FIELD_NOT_ALLOWED_EXECUTION_MODE
                            .getMessage();

        case CUSTOM_FIELDS:

            return ExecutionMessage
                    .CUSTOM_FIELDS_NOT_IMPLEMENTED
                    .getMessage();

        case ALL_FIELDS:

            return ExecutionMessage
                    .FIELD_NOT_ALLOWED_EXECUTION_MODE
                    .getMessage();

        default:

            return ExecutionMessage
                    .UNSUPPORTED_EXECUTION_MODE
                    .getMessage();
        }
    }

    // =========================================================
    // PARENT FIELD GROUP CONTROL
    // =========================================================

    /**
     * Determines whether a field's parent group is active.
     *
     * <p>
     * Multiple parent controllers are evaluated using OR logic.
     * </p>
     *
     * <p>
     * Example:
     *
     * <pre>
     * MAIN_CONTROLLER  = false
     * START_CONTROLLER = true
     * END_CONTROLLER   = false
     *
     * Shared field = ALLOWED
     * </pre>
     *
     * because START_CONTROLLER is true.
     */
    private static <E extends Enum<E>> boolean isParentGroupActive(
            Class<E> page,
            E field,
            List<E> parentControllers,
            EnumMap<E, Object> fieldValues) {

        for (E controller : parentControllers) {

            Object controllerValue =
                    fieldValues.get(controller);

            boolean active =
                    isTrueValue(controllerValue);

            FieldControlLogger.debug(() ->
                    "Parent group evaluated"
                    + " | page=" + page.getSimpleName()
                    + " | field=" + field
                    + " | controller=" + controller
                    + " | value=" + controllerValue
                    + " | active=" + active
            );

            /*
             * OR logic:
             * One active controller is enough.
             */
            if (active) {
                return true;
            }
        }

        return false;
    }

    /**
     * Safely evaluates a controller value as TRUE.
     */
    private static boolean isTrueValue(Object value) {

        if (value == null) {
            return false;
        }

        String actualValue =
                value.toString().trim();

        if (actualValue.isEmpty()
                || "NA".equalsIgnoreCase(actualValue)) {

            return false;
        }

        return Boolean.parseBoolean(actualValue);
    }

    // =========================================================
    // CONDITIONAL FIELD CONTROL
    // =========================================================

    private static <E extends Enum<E>>
    boolean isConditionSatisfied(
            Class<E> page,
            E field,
            EnumMap<E, Object> fieldValues) {

        List<FieldCondition<E>> anyConditions =
                FieldConditionRegistry.getAnyConditions(
                        page,
                        field
                );

        List<List<FieldCondition<E>>> allConditionGroups =
                FieldConditionRegistry.getAllConditions(
                        page,
                        field
                );

        /*
         * No condition means the field is allowed
         * from the condition perspective.
         */
        if (anyConditions.isEmpty()
                && allConditionGroups.isEmpty()) {

            return true;
        }

        /*
         * =====================================================
         * OR CONDITIONS
         * =====================================================
         *
         * Any single condition can satisfy the field.
         */

        for (FieldCondition<E> condition :
                anyConditions) {

            if (evaluateCondition(
                    page,
                    field,
                    condition,
                    fieldValues)) {

                return true;
            }
        }

        /*
         * =====================================================
         * AND CONDITION GROUPS
         * =====================================================
         *
         * Every condition inside one group must pass.
         *
         * Multiple AND groups are OR'ed.
         */

        for (List<FieldCondition<E>> group :
                allConditionGroups) {

            boolean groupSatisfied = true;

            for (FieldCondition<E> condition :
                    group) {

                if (!evaluateCondition(
                        page,
                        field,
                        condition,
                        fieldValues)) {

                    groupSatisfied = false;
                    break;
                }
            }

            if (groupSatisfied) {
                return true;
            }
        }

        return false;
    }

    // =========================================================
    // CONDITION EVALUATION
    // =========================================================

    private static <E extends Enum<E>>
    boolean evaluateCondition(
            Class<E> page,
            E field,
            FieldCondition<E> condition,
            EnumMap<E, Object> fieldValues) {

        E controllerField =
                condition.getControllerField();

        Object controllerValue =
                fieldValues.get(controllerField);

        boolean result =
                ConditionEvaluator.evaluate(
                        controllerValue,
                        condition.getOperator(),
                        condition.getExpectedValue()
                );

        FieldControlLogger.debug(() ->
                "Field condition evaluated"
                + " | page=" + page.getSimpleName()
                + " | field=" + field
                + " | controller=" + controllerField
                + " | operator=" + condition.getOperator()
                + " | expected=" + condition.getExpectedValue()
                + " | actual=" + controllerValue
                + " | result=" + result
        );

        return result;
    }
}