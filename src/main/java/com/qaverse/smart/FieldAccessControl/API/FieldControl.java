package com.qaverse.smart.FieldAccessControl.API;

import java.util.EnumMap;

import com.qaverse.smart.FieldAccessControl.Builder.FieldDefinitionBuilder;
import com.qaverse.smart.FieldAccessControl.Builder.RuleBuilder;
import com.qaverse.smart.FieldAccessControl.Configuration.ExecutionMode;
import com.qaverse.smart.FieldAccessControl.Configuration.OperationMode;
import com.qaverse.smart.FieldAccessControl.Configuration.UserType;
import com.qaverse.smart.FieldAccessControl.Execution.FieldExecutor;
import com.qaverse.smart.FieldAccessControl.Model.FieldExecutionContext;

public final class FieldControl {

    private FieldControl() {
    }

    /**
     * Create a rule builder for a page.
     */
    public static <E extends Enum<E>> RuleBuilder<E> rules(
            Class<E> page) {

        return new RuleBuilder<>(page);
    }

    /**
     * Create a field-definition builder for a page.
     */
    public static <E extends Enum<E>> FieldDefinitionBuilder<E> definitions(
            Class<E> page) {

        return new FieldDefinitionBuilder<>(page);
    }

    /**
     * Execute field actions using the configured field-control rules.
     */
    public static <E extends Enum<E>> void execute(
            Class<E> page,
            OperationMode operationMode,
            ExecutionMode executionMode,
            UserType currentUser,
            EnumMap<E, Object> fieldValues,
            EnumMap<E, Runnable> fieldActions) {

        FieldExecutionContext<E> context =
                new FieldExecutionContext<>(
                        page,
                        operationMode,
                        executionMode,
                        currentUser,
                        fieldValues,
                        fieldActions
                );

        FieldExecutor.execute(context);
    }

    /**
     * Execute using the page's default execution mode.
     */
    public static <E extends Enum<E>> void execute(
            Class<E> page,
            OperationMode operationMode,
            UserType currentUser,
            EnumMap<E, Object> fieldValues,
            EnumMap<E, Runnable> fieldActions) {

        execute(
                page,
                operationMode,
                ExecutionMode.ALL_FIELDS,
                currentUser,
                fieldValues,
                fieldActions
        );
    }

    /**
     * Clear all configured rules and metadata.
     */
    public static void clear() {
        com.qaverse.smart.FieldAccessControl.Registry.FieldRegistry.clear();
        com.qaverse.smart.FieldAccessControl.Registry.FieldDefinitionRegistry.clear();
        com.qaverse.smart.FieldAccessControl.Registry.FieldConditionRegistry.clear();
        com.qaverse.smart.FieldAccessControl.Registry.MetadataRegistry.clear();
    }
}