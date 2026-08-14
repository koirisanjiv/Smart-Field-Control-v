package com.qaverse.smart.FieldAccessControl.Execution;

import java.util.EnumMap;
import java.util.Map;

import com.qaverse.smart.FieldAccessControl.Condition.ConditionEvaluator;
import com.qaverse.smart.FieldAccessControl.Condition.FieldCondition;
import com.qaverse.smart.FieldAccessControl.Configuration.FieldBehavior;
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
	public static <E extends Enum<E>> void execute(FieldExecutionContext<E> context) {

		Class<E> page = context.getPage();

		EnumMap<E, Object> fieldValues = context.getFieldValues();

		printExecutionHeader(context);

		for (Map.Entry<E, Object> entry : fieldValues.entrySet()) {

			E field = entry.getKey();
			Object value = entry.getValue();

			System.out.println("\n-------------------------------------------------");

			System.out.println("Processing Field : " + field);

			/*
			 * =============================== 1. Validate field value	===============================
			 */

			if (!isValidValue(field, value)) {
				continue;
			}

			/*
			 * =============================== 2. Create field context	==================================
			 */

			FieldContext<E> fieldContext = new FieldContext<>(page, context.getOperationMode(),
					context.getCurrentUser(), field);

			printFieldLookup(fieldContext);

			/*
			 * ============================== 3. Field must be * registered ===================================
			 */

			if (!isFieldRegistered(fieldContext)) {
				continue;
			}

			/*
			 * ======================== 4. Check user-wise field behavior ==============================
			 */

			if (!isEditable(fieldContext)) {
				continue;
			}

			/*
			 * ====================== 5. Check mandatory / optional execution mode ==========================
			 */

			FieldDefinition definition = FieldDefinitionRegistry.get(page, field);

			if (!isAllowedByExecutionMode(context, definition)) {

				continue;
			}

			/*
			 * =========================6. Check conditional dependency ===========================
			 */

			if (!isConditionSatisfied(page, field, fieldValues)) {

				continue;
			}

			/*
			 * ==================== 7. FIELD PASSED ALL
			 * CONTROL RULES ================================
			 *
			 * No UI action is executed here.
			 *
			 * Smart Field Control only determines that this field is allowed to proceed.
			 *
			 * Smart Core will consume this decision later.
			 */

			System.out.println("✅ Field Allowed : " + field);
		}

		System.out.println("\n=============== Field Evaluation Completed ===============\n");
	}

	// =========================================================
	// VALUE VALIDATION
	// =========================================================

	private static <E extends Enum<E>> boolean isValidValue(E field, Object value) {

		if (value == null) {

			System.out.println("❌ Skipped : Value is NULL");

			return false;
		}

		String text = value.toString().trim();

		System.out.println("Value : [" + text + "]");

		if (text.isEmpty()) {

			System.out.println("❌ Skipped : Empty value");

			return false;
		}

		if ("NA".equalsIgnoreCase(text)) {

			System.out.println("❌ Skipped : NA value");

			return false;
		}

		return true;
	}

	// =========================================================
	// FIELD CONTROL
	// =========================================================

	private static <E extends Enum<E>> boolean isFieldRegistered(FieldContext<E> fieldContext) {

		if (!FieldRegistry.isRegistered(fieldContext)) {

			System.out.println("Behavior : NOT REGISTERED");

			System.out.println("❌ Skipped : Field is not registered");

			return false;
		}

		return true;
	}

	// =========================================================
	// USER-WISE FIELD PERMISSION
	// =========================================================

	private static <E extends Enum<E>> boolean isEditable(FieldContext<E> fieldContext) {

		FieldBehavior behavior = FieldRegistry.getBehavior(fieldContext);

		System.out.println("Behavior : " + behavior);

		if (behavior != FieldBehavior.EDITABLE) {

			System.out.println("❌ Skipped : Not EDITABLE");

			return false;
		}

		return true;
	}

	// =========================================================
	// MANDATORY / OPTIONAL CONTROL
	// =========================================================

	private static <E extends Enum<E>> boolean isAllowedByExecutionMode(FieldExecutionContext<E> context,
			FieldDefinition definition) {

		if (definition == null) {

			System.out.println("Definition : NULL");

		} else {

			System.out.println("Definition : " + (definition.isMandatory() ? "MANDATORY" : "OPTIONAL"));
		}

		switch (context.getExecutionMode()) {

		case ALL_FIELDS:

			System.out.println("Execution : ALL_FIELDS");

			return true;

		case MANDATORY_FIELDS:

			System.out.println("Execution : MANDATORY_FIELDS");

			if (definition == null || !definition.isMandatory()) {

				System.out.println("❌ Skipped : Not Mandatory");

				return false;
			}

			return true;

		case OPTIONAL_FIELDS:

			System.out.println("Execution : OPTIONAL_FIELDS");

			if (definition != null && definition.isMandatory()) {

				System.out.println("❌ Skipped : Mandatory field ignored");

				return false;
			}

			return true;

		case CUSTOM_FIELDS:

			/*
			 * CUSTOM_FIELDS will be implemented later.
			 *
			 * We deliberately do not introduce the custom-field selection model in Point
			 * #1.
			 */

			System.out.println("❌ Skipped : CUSTOM_FIELDS not implemented");

			return false;

		default:

			return false;
		}
	}

	// =========================================================
	// CONDITIONAL FIELD CONTROL
	// =========================================================

	private static <E extends Enum<E>> boolean isConditionSatisfied(Class<E> page, E field,
			EnumMap<E, Object> fieldValues) {

		/*
		 * Direct lookup.
		 *
		 * If this field has no condition, no condition processing is performed.
		 */

		FieldCondition<E> condition = FieldConditionRegistry.get(page, field);

		if (condition == null) {

			return true;
		}

		/*
		 * ===================================================== Field is
		 * condition-controlled =====================================================
		 */

		E controllerField = condition.getControllerField();

		Object controllerValue = fieldValues.get(controllerField);

		boolean result = ConditionEvaluator.evaluate(controllerValue, condition.getOperator(),
				condition.getExpectedValue());

		System.out.println("Condition Controller : " + controllerField + " | Operator : " + condition.getOperator()
				+ " | Expected : " + condition.getExpectedValue() + " | Actual : " + controllerValue + " | Result : "
				+ result);

		if (!result) {

			System.out.println("❌ Skipped : Condition not satisfied for " + field);

			return false;
		}

		return true;
	}

	// =========================================================
	// LOGGING / DEBUG
	// =========================================================

	private static <E extends Enum<E>> void printFieldLookup(FieldContext<E> fieldContext) {

		System.out
				.println("LOOKUP : " + fieldContext.getPage().getSimpleName() + " | " + fieldContext.getOperationMode()
						+ " | " + fieldContext.getUserType() + " | " + fieldContext.getField());
	}

	private static <E extends Enum<E>> void printExecutionHeader(FieldExecutionContext<E> context) {

		System.out.println("\n=================================================");

		System.out.println("Page           : " + context.getPage().getSimpleName());

		System.out.println("Operation Mode : " + context.getOperationMode());

		System.out.println("Execution Mode : " + context.getExecutionMode());

		System.out.println("Current User   : " + context.getCurrentUser());

		System.out.println("=================================================\n");
	}
}