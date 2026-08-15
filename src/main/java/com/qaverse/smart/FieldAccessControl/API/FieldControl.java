package com.qaverse.smart.FieldAccessControl.API;

import java.util.EnumMap;
import java.util.Objects;

import com.qaverse.smart.FieldAccessControl.Builder.FieldDefinitionBuilder;
import com.qaverse.smart.FieldAccessControl.Builder.RuleBuilder;
import com.qaverse.smart.FieldAccessControl.Configuration.ExecutionMode;
import com.qaverse.smart.FieldAccessControl.Configuration.OperationMode;
import com.qaverse.smart.FieldAccessControl.Configuration.UserType;
import com.qaverse.smart.FieldAccessControl.Execution.FieldEvaluator;
import com.qaverse.smart.FieldAccessControl.Model.FieldExecutionContext;
import com.qaverse.smart.FieldAccessControl.Model.FieldExecutionPlan;
import com.qaverse.smart.logger.SmartLog;

public final class FieldControl {

    private FieldControl() {
    }

    // =========================================================
    // CONFIGURATION API
    // =========================================================

    /**
     * Create a rule builder for a page.
     */
    public static <E extends Enum<E>> RuleBuilder<E> rules(Class<E> page) {

        SmartLog.debug(() ->
                "Creating rule builder | page="
                + (page != null ? page.getSimpleName() : "null"));

        return new RuleBuilder<>(page);
    }

    /**
     * Create a field-definition builder for a page.
     */
    public static <E extends Enum<E>> FieldDefinitionBuilder<E> definitions(Class<E> page) {

        SmartLog.debug(() ->
                "Creating field-definition builder | page="
                + (page != null ? page.getSimpleName() : "null"));

        return new FieldDefinitionBuilder<>(page);
    }

    // =========================================================
    // REQUEST API
    // =========================================================

    /**
     * Create a field-control request builder for a page.
     *
     * <p>
     * This is the recommended entry point for evaluation.
     * </p>
     */
    public static <E extends Enum<E>> FieldControlRequest.Builder<E> request(Class<E> page) {

        SmartLog.debug(() ->
                "Creating field-control request | page="
                + (page != null ? page.getSimpleName() : "null"));

        return FieldControlRequest.builder(page);
    }

    // =========================================================
    // PRIMARY EVALUATION API
    // =========================================================

    /**
     * Evaluate field-control rules.
     *
     * <p>
     * Smart Field Control does not execute any UI action. It evaluates field
     * definitions, permissions, execution modes, and conditions and returns a
     * field execution plan.
     * </p>
     *
     * <p>
     * The returned plan is intended to be consumed by Smart Core for actual
     * automation execution.
     * </p>
     *
     * @param request field-control request
     * @return field execution plan
     */
    public static <E extends Enum<E>> FieldExecutionPlan<E> evaluate(
            FieldControlRequest<E> request) {

        Objects.requireNonNull(
                request,
                FieldControlMessage.REQUEST_NULL.getMessage());

        String pageName = request.getPage().getSimpleName();

        SmartLog.debug(() ->
                FieldControlMessage.EVALUATION_STARTED.getMessage()
                + " | page=" + pageName
                + " | operationMode=" + request.getOperationMode()
                + " | executionMode=" + request.getExecutionMode());

        try {

            FieldExecutionContext<E> context =
                    new FieldExecutionContext<>(
                            request.getPage(),
                            request.getOperationMode(),
                            request.getExecutionMode(),
                            request.getCurrentUser(),
                            request.getFieldValues());

            FieldExecutionPlan<E> plan = FieldEvaluator.evaluate(context);

            SmartLog.debug(() ->
                    FieldControlMessage.EVALUATION_COMPLETED.getMessage()
                    + " | page=" + pageName);

            return plan;

        } catch (RuntimeException exception) {

            SmartLog.error( () ->
                    FieldControlMessage.EVALUATION_FAILED.getMessage()
                    + " | page=" + pageName,
                    exception);

            throw exception;
        }
    }

    // =========================================================
    // COMPATIBILITY API
    // =========================================================

    /**
     * Evaluate field-control rules using explicit parameters.
     *
     * <p>
     * Kept temporarily for backward compatibility. New code should prefer
     * {@link #evaluate(FieldControlRequest)}.
     * </p>
     */
    public static <E extends Enum<E>> FieldExecutionPlan<E> evaluate(
            Class<E> page,
            OperationMode operationMode,
            ExecutionMode executionMode,
            UserType currentUser,
            EnumMap<E, Object> fieldValues) {

        return evaluate(
                FieldControlRequest
                        .builder(page)
                        .operationMode(operationMode)
                        .executionMode(executionMode)
                        .currentUser(currentUser)
                        .fieldValues(fieldValues)
                        .build());
    }

    /**
     * Evaluate using ALL_FIELDS as the default execution mode.
     *
     * <p>
     * Kept temporarily for backward compatibility.
     * </p>
     */
    public static <E extends Enum<E>> FieldExecutionPlan<E> evaluate(
            Class<E> page,
            OperationMode operationMode,
            UserType currentUser,
            EnumMap<E, Object> fieldValues) {

        return evaluate(
                FieldControlRequest
                        .builder(page)
                        .operationMode(operationMode)
                        .currentUser(currentUser)
                        .fieldValues(fieldValues)
                        .build());
    }

    // =========================================================
    // LIFECYCLE / CLEANUP
    // =========================================================

    /**
     * Clear all configured rules, definitions, conditions, and page metadata.
     *
     * <p>
     * Primarily intended for test/framework lifecycle cleanup.
     * </p>
     */
    public static void clear() {

        SmartLog.debug(() ->
                FieldControlMessage.REGISTRY_CLEANUP_STARTED.getMessage());

        com.qaverse.smart.FieldAccessControl.Registry.FieldRegistry.clear();

        com.qaverse.smart.FieldAccessControl.Registry.FieldDefinitionRegistry.clear();

        com.qaverse.smart.FieldAccessControl.Registry.FieldConditionRegistry.clear();

        com.qaverse.smart.FieldAccessControl.Registry.MetadataRegistry.clear();

        SmartLog.debug(() ->
                FieldControlMessage.REGISTRY_CLEANUP_COMPLETED.getMessage());
    }
}