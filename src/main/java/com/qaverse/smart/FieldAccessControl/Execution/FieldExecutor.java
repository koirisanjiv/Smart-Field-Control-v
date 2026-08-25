package com.qaverse.smart.FieldAccessControl.Execution;

import java.util.EnumMap;
import java.util.Map;

import com.qaverse.smart.FieldAccessControl.Condition.ConditionEvaluator;
import com.qaverse.smart.FieldAccessControl.Condition.FieldCondition;
import com.qaverse.smart.FieldAccessControl.Configuration.FieldBehavior;
import com.qaverse.smart.FieldAccessControl.Logging.FieldControlLogger;
import com.qaverse.smart.FieldAccessControl.Model.FieldContext;
import com.qaverse.smart.FieldAccessControl.Model.FieldDefinition;
import com.qaverse.smart.FieldAccessControl.Model.FieldExecutionContext;
import com.qaverse.smart.FieldAccessControl.Registry.FieldConditionRegistry;
import com.qaverse.smart.FieldAccessControl.Registry.FieldDefinitionRegistry;
import com.qaverse.smart.FieldAccessControl.Registry.FieldRegistry;

public final class FieldExecutor {

    private FieldExecutor() {
    }

    /**
     * Evaluates field-control rules for the supplied execution context.
     *
     * <p>
     * Smart Field Control does NOT execute any UI action. It only evaluates whether
     * a field is allowed to be processed.
     * </p>
     *
     * <p>
     * UI execution will be handled by Smart Core.
     * </p>
     */
    public static <E extends Enum<E>> void execute(
            FieldExecutionContext<E> context) {

        Class<E> page =
                context.getPage();

        EnumMap<E, Object> fieldValues =
                context.getFieldValues();

        logExecutionHeader(context);

        for (Map.Entry<E, Object> entry :
                fieldValues.entrySet()) {

            E field =
                    entry.getKey();

            Object value =
                    entry.getValue();

            FieldControlLogger.debug(() ->
                    "Processing field"
                    + " | page=" + page.getSimpleName()
                    + " | field=" + field
            );

            /*
             * ===============================
             * 1. Validate field value
             * ===============================
             */

            if (!isValidValue(field, value)) {
                continue;
            }

            /*
             * ===============================
             * 2. Create field context
             * ===============================
             */

            FieldContext<E> fieldContext =
                    new FieldContext<>(
                            page,
                            context.getOperationMode(),
                            context.getCurrentUser(),
                            field
                    );

            logFieldLookup(fieldContext);

            /*
             * ===============================
             * 3. Field must be registered
             * ===============================
             */

            if (!isFieldRegistered(fieldContext)) {
                continue;
            }

            /*
             * ===============================
             * 4. Check user-wise field behavior
             * ===============================
             */

            if (!isEditable(fieldContext)) {
                continue;
            }

            /*
             * ===============================
             * 5. Check mandatory / optional execution mode
             * ===============================
             */

            FieldDefinition definition =
                    FieldDefinitionRegistry.get(
                            page,
                            field
                    );

            if (!isAllowedByExecutionMode(
                    context,
                    definition)) {

                continue;
            }

            /*
             * ===============================
             * 6. Check conditional dependency
             * ===============================
             */

            if (!isConditionSatisfied(
                    page,
                    field,
                    fieldValues)) {

                continue;
            }

            /*
             * ===============================
             * 7. FIELD PASSED ALL CONTROL RULES
             *
             * No UI action is executed here.
             *
             * Smart Field Control only determines
             * that this field is allowed to proceed.
             *
             * Smart Core will consume this decision later.
             * ===============================
             */

            FieldControlLogger.success(
                    "Field allowed"
                    + " | page=" + page.getSimpleName()
                    + " | field=" + field
            );
        }

        FieldControlLogger.step(
                "Field evaluation completed"
                + " | page=" + page.getSimpleName()
        );
    }

    // =========================================================
    // VALUE VALIDATION
    // =========================================================

    private static <E extends Enum<E>>
    boolean isValidValue(
            E field,
            Object value) {

        if (value == null) {

            FieldControlLogger.debug(() ->
                    "Field skipped"
                    + " | field=" + field
                    + " | reason=value is NULL"
            );

            return false;
        }

        String text =
                value.toString().trim();

        if (text.isEmpty()) {

            FieldControlLogger.debug(() ->
                    "Field skipped"
                    + " | field=" + field
                    + " | reason=empty value"
            );

            return false;
        }

        if ("NA".equalsIgnoreCase(text)) {

            FieldControlLogger.debug(() ->
                    "Field skipped"
                    + " | field=" + field
                    + " | reason=NA value"
            );

            return false;
        }

        return true;
    }

    // =========================================================
    // FIELD CONTROL
    // =========================================================

    private static <E extends Enum<E>>
    boolean isFieldRegistered(
            FieldContext<E> fieldContext) {

        if (!FieldRegistry.isRegistered(
                fieldContext)) {

            FieldControlLogger.debug(() ->
                    "Field skipped"
                    + " | field="
                    + fieldContext.getField()
                    + " | reason="
                    + ExecutionMessage.FIELD_NOT_REGISTERED
                            .getMessage()
            );

            return false;
        }

        return true;
    }

    // =========================================================
    // USER-WISE FIELD PERMISSION
    // =========================================================

    private static <E extends Enum<E>>
    boolean isEditable(
            FieldContext<E> fieldContext) {

        FieldBehavior behavior =
                FieldRegistry.getBehavior(
                        fieldContext
                );

        FieldControlLogger.debug(() ->
                "Field behavior resolved"
                + " | field="
                + fieldContext.getField()
                + " | behavior=" + behavior
        );

        if (behavior != FieldBehavior.EDITABLE) {

            FieldControlLogger.debug(() ->
                    "Field skipped"
                    + " | field="
                    + fieldContext.getField()
                    + " | reason="
                    + ExecutionMessage.FIELD_NOT_EDITABLE
                            .getMessage()
                    + " | behavior=" + behavior
            );

            return false;
        }

        return true;
    }

    // =========================================================
    // MANDATORY / OPTIONAL CONTROL
    // =========================================================

    private static <E extends Enum<E>>
    boolean isAllowedByExecutionMode(
            FieldExecutionContext<E> context,
            FieldDefinition definition) {

        FieldControlLogger.debug(() ->
                "Checking execution mode"
                + " | executionMode="
                + context.getExecutionMode()
                + " | definition="
                + getDefinitionType(definition)
        );

        switch (context.getExecutionMode()) {

        case ALL_FIELDS:

            return true;

        case MANDATORY_FIELDS:

            if (definition == null
                    || !definition.isMandatory()) {

                FieldControlLogger.debug(() ->
                        "Field skipped"
                        + " | reason="
                        + ExecutionMessage
                                .FIELD_NOT_MANDATORY
                                .getMessage()
                );

                return false;
            }

            return true;

        case OPTIONAL_FIELDS:

            if (definition != null
                    && definition.isMandatory()) {

                FieldControlLogger.debug(() ->
                        "Field skipped"
                        + " | reason="
                        + ExecutionMessage
                                .MANDATORY_FIELD_EXCLUDED_FROM_OPTIONAL
                                .getMessage()
                );

                return false;
            }

            return true;

        case CUSTOM_FIELDS:

            FieldControlLogger.debug(
                    ExecutionMessage
                            .CUSTOM_FIELDS_NOT_IMPLEMENTED
                            .getMessage()
            );

            return false;

        default:

            FieldControlLogger.warn(() ->
                    ExecutionMessage
                            .UNSUPPORTED_EXECUTION_MODE
                            .getMessage()
                            + " | executionMode="
                            + context.getExecutionMode()
            );

            return false;
        }
    }

    private static String getDefinitionType(
            FieldDefinition definition) {

        if (definition == null) {
            return "NULL";
        }

        return definition.isMandatory()
                ? "MANDATORY"
                : "OPTIONAL";
    }

    // =========================================================
    // CONDITIONAL FIELD CONTROL
    // =========================================================

    private static <E extends Enum<E>>
    boolean isConditionSatisfied(
            Class<E> page,
            E field,
            EnumMap<E, Object> fieldValues) {

        /*
         * Direct lookup.
         *
         * If this field has no condition,
         * no condition processing is performed.
         */

        FieldCondition<E> condition =
                FieldConditionRegistry.get(
                        page,
                        field
                );

        if (condition == null) {

            return true;
        }

        /*
         * =====================================================
         * Field is condition-controlled
         * =====================================================
         */

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
                + " | operator="
                + condition.getOperator()
                + " | result=" + result
        );

        if (!result) {

            FieldControlLogger.debug(() ->
                    "Field skipped"
                    + " | field=" + field
                    + " | reason="
                    + ExecutionMessage
                            .FIELD_CONDITION_NOT_SATISFIED
                            .getMessage()
            );

            return false;
        }

        return true;
    }

    // =========================================================
    // LOGGING
    // =========================================================

    private static <E extends Enum<E>>
    void logFieldLookup(
            FieldContext<E> fieldContext) {

        FieldControlLogger.debug(() ->
                "Field lookup"
                + " | page="
                + fieldContext.getPage().getSimpleName()
                + " | operationMode="
                + fieldContext.getOperationMode()
                + " | userType="
                + fieldContext.getUserType()
                + " | field="
                + fieldContext.getField()
        );
    }

    private static <E extends Enum<E>>
    void logExecutionHeader(
            FieldExecutionContext<E> context) {

        FieldControlLogger.step(
                "Field execution started"
                + " | page="
                + context.getPage().getSimpleName()
                + " | operationMode="
                + context.getOperationMode()
                + " | executionMode="
                + context.getExecutionMode()
                + " | currentUser="
                + context.getCurrentUser()
        );
    }
}