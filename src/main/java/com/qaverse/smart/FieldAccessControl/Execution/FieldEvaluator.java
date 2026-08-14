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
	public static <E extends Enum<E>> FieldExecutionPlan<E> evaluate(FieldExecutionContext<E> context) {

		Class<E> page = context.getPage();

		EnumMap<E, Object> fieldValues = context.getFieldValues();

		List<FieldDecision<E>> decisions = new ArrayList<>();

		for (Map.Entry<E, Object> entry : fieldValues.entrySet()) {

			E field = entry.getKey();

			Object value = entry.getValue();

			FieldEvaluationResult<E> result = evaluateField(context, page, field, value, fieldValues);

			decisions.add(result.toDecision());
		}

		return new FieldExecutionPlan<>(page, decisions);
	}

	/**
	 * Evaluates a single field.
	 */
	private static <E extends Enum<E>> FieldEvaluationResult<E> evaluateField(FieldExecutionContext<E> context,
			Class<E> page, E field, Object value, EnumMap<E, Object> fieldValues) {

		/*
		 * ===================================================== 1. Validate value
		 * =====================================================
		 */

		if (!isValidValue(value)) {

			return FieldEvaluationResult.skipped(field, value, FieldEvaluationStatus.SKIPPED_INVALID_VALUE, null,
					"Field value is null, empty, or NA");
		}

		/*
		 * ===================================================== 2. Create field context
		 * =====================================================
		 */

		FieldContext<E> fieldContext = new FieldContext<>(page, context.getOperationMode(), context.getCurrentUser(),
				field);

		/*
		 * ===================================================== 3. Field must be
		 * registered =====================================================
		 */

		if (!FieldRegistry.isRegistered(fieldContext)) {

			return FieldEvaluationResult.skipped(field, value, FieldEvaluationStatus.SKIPPED_NOT_REGISTERED, null,
					"Field is not registered");
		}

		/*
		 * ===================================================== 4. Resolve field
		 * behavior =====================================================
		 */

		FieldBehavior behavior = FieldRegistry.getBehavior(fieldContext);

		/*
		 * ===================================================== 5. Field must be
		 * editable for execution =====================================================
		 */

		if (behavior != FieldBehavior.EDITABLE) {

			return FieldEvaluationResult.skipped(field, value, FieldEvaluationStatus.SKIPPED_NOT_EDITABLE, behavior,
					"Field behavior is " + behavior + ", not EDITABLE");
		}

		/*
		 * ===================================================== 6. Resolve field
		 * definition =====================================================
		 */

		FieldDefinition definition = FieldDefinitionRegistry.get(page, field);

		/*
		 * ===================================================== 7. Check execution mode
		 * =====================================================
		 */

		FieldEvaluationStatus executionStatus = evaluateExecutionMode(context, definition);

		if (executionStatus != null) {

			return FieldEvaluationResult.skipped(field, value, executionStatus, behavior,
					getExecutionModeReason(context, definition));
		}

		/*
		 * ===================================================== 8. Check conditional
		 * dependency =====================================================
		 */

		if (!isConditionSatisfied(page, field, fieldValues)) {

			return FieldEvaluationResult.skipped(field, value, FieldEvaluationStatus.SKIPPED_CONDITION, behavior,
					"Field condition is not satisfied");
		}

		/*
		 * ===================================================== 9. Field passed all
		 * rules =====================================================
		 */

		return FieldEvaluationResult.allowed(field, value, behavior);
	}

	// =========================================================
	// VALUE VALIDATION
	// =========================================================

	private static boolean isValidValue(Object value) {

		if (value == null) {
			return false;
		}

		String text = value.toString().trim();

		if (text.isEmpty()) {
			return false;
		}

		return !"NA".equalsIgnoreCase(text);
	}

	// =========================================================
	// EXECUTION MODE
	// =========================================================

	private static <E extends Enum<E>> FieldEvaluationStatus evaluateExecutionMode(FieldExecutionContext<E> context,
			FieldDefinition definition) {

		switch (context.getExecutionMode()) {

		case ALL_FIELDS:

			return null;

		case MANDATORY_FIELDS:

			if (definition == null || !definition.isMandatory()) {

				return FieldEvaluationStatus.SKIPPED_EXECUTION_MODE;
			}

			return null;

		case OPTIONAL_FIELDS:

			if (definition != null && definition.isMandatory()) {

				return FieldEvaluationStatus.SKIPPED_EXECUTION_MODE;
			}

			return null;

		case CUSTOM_FIELDS:

			return FieldEvaluationStatus.SKIPPED_CUSTOM_FIELDS;

		default:

			return FieldEvaluationStatus.SKIPPED_EXECUTION_MODE;
		}
	}

	private static <E extends Enum<E>> String getExecutionModeReason(FieldExecutionContext<E> context,
			FieldDefinition definition) {

		switch (context.getExecutionMode()) {

		case MANDATORY_FIELDS:

			return definition == null ? "Field has no mandatory definition" : "Field is not mandatory";

		case OPTIONAL_FIELDS:

			return definition != null && definition.isMandatory() ? "Mandatory field is excluded from OPTIONAL_FIELDS"
					: "Field is not allowed by execution mode";

		case CUSTOM_FIELDS:

			return "CUSTOM_FIELDS is not implemented yet";

		case ALL_FIELDS:

			return "Field is not allowed by execution mode";

		default:

			return "Unsupported execution mode";
		}
	}

	// =========================================================
	// CONDITIONAL FIELD CONTROL
	// =========================================================

	private static <E extends Enum<E>> boolean isConditionSatisfied(Class<E> page, E field,
			EnumMap<E, Object> fieldValues) {

		FieldCondition<E> condition = FieldConditionRegistry.get(page, field);

		/*
		 * No condition means field is allowed from the condition perspective.
		 */

		if (condition == null) {
			return true;
		}

		E controllerField = condition.getControllerField();

		Object controllerValue = fieldValues.get(controllerField);

		return ConditionEvaluator.evaluate(controllerValue, condition.getOperator(), condition.getExpectedValue());
	}
}