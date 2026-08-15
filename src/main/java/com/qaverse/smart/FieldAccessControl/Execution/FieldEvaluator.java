package com.qaverse.smart.FieldAccessControl.Execution;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.qaverse.smart.FieldAccessControl.Condition.ConditionEvaluator;
import com.qaverse.smart.FieldAccessControl.Condition.FieldCondition;
import com.qaverse.smart.FieldAccessControl.Configuration.FieldBehavior;
import com.qaverse.smart.FieldAccessControl.Model.FieldContext;
import com.qaverse.smart.FieldAccessControl.Model.FieldDecision;
import com.qaverse.smart.FieldAccessControl.Model.FieldDefinition;
import com.qaverse.smart.FieldAccessControl.Model.FieldExecutionContext;
import com.qaverse.smart.FieldAccessControl.Model.FieldExecutionPlan;
import com.qaverse.smart.FieldAccessControl.Registry.FieldConditionRegistry;
import com.qaverse.smart.FieldAccessControl.Registry.FieldDefinitionRegistry;
import com.qaverse.smart.FieldAccessControl.Registry.FieldRegistry;
import com.qaverse.smart.logger.SmartLog;

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
     * It only determines whether each field is allowed to proceed and provides the
     * reason when it is skipped.
     * </p>
     */
    public static <E extends Enum<E>> FieldExecutionPlan<E> evaluate(
            FieldExecutionContext<E> context) {

        Class<E> page = context.getPage();

        EnumMap<E, Object> fieldValues =
                context.getFieldValues();

        SmartLog.debug(() ->
                "Field control evaluation started"
                + " | page=" + page.getSimpleName()
                + " | operationMode=" + context.getOperationMode()
                + " | executionMode=" + context.getExecutionMode()
                + " | currentUser=" + context.getCurrentUser()
                + " | fieldCount=" + fieldValues.size()
        );

        List<FieldDecision<E>> decisions =
                new ArrayList<>();

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

            SmartLog.debug(() ->
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

        SmartLog.debug(() ->
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

            SmartLog.debug(() ->
                    "Field evaluation skipped"
                    + " | field=" + field
                    + " | reason="
                    + ExecutionMessage.FIELD_VALUE_INVALID.getMessage()
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

            SmartLog.debug(() ->
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

            SmartLog.debug(() ->
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

            SmartLog.debug(() ->
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
         * 8. Check conditional dependency
         * =====================================================
         */

        if (!isConditionSatisfied(
                page,
                field,
                fieldValues)) {

            SmartLog.debug(() ->
                    "Field evaluation skipped"
                    + " | field=" + field
                    + " | reason="
                    + ExecutionMessage.FIELD_CONDITION_NOT_SATISFIED
                            .getMessage()
            );

            return FieldEvaluationResult.skipped(
                    field,
                    value,
                    FieldEvaluationStatus.SKIPPED_CONDITION,
                    behavior,
                    ExecutionMessage.FIELD_CONDITION_NOT_SATISFIED
                            .getMessage()
            );
        }

        /*
         * =====================================================
         * 9. Field passed all rules
         * =====================================================
         */

        SmartLog.debug(() ->
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
    // CONDITIONAL FIELD CONTROL
    // =========================================================

    private static <E extends Enum<E>>
    boolean isConditionSatisfied(
            Class<E> page,
            E field,
            EnumMap<E, Object> fieldValues) {

        FieldCondition<E> condition =
                FieldConditionRegistry.get(
                        page,
                        field
                );

        /*
         * No condition means field is allowed from the
         * condition perspective.
         */

        if (condition == null) {

            return true;
        }

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

        SmartLog.debug(() ->
                "Field condition evaluated"
                + " | page=" + page.getSimpleName()
                + " | field=" + field
                + " | controller=" + controllerField
                + " | operator=" + condition.getOperator()
                + " | result=" + result
        );

        return result;
    }
}