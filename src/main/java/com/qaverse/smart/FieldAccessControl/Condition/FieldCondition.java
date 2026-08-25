package com.qaverse.smart.FieldAccessControl.Condition;

import com.qaverse.smart.FieldAccessControl.Logging.FieldControlLogger;

public final class FieldCondition<E extends Enum<E>> {

	private final E controllerField;
	private final ConditionOperator operator;
	private final Object expectedValue;

	public FieldCondition(E controllerField, ConditionOperator operator) {

		this(controllerField, operator, null);
	}

	public FieldCondition(E controllerField, ConditionOperator operator, Object expectedValue) {

		if (controllerField == null) {

			FieldControlLogger.error(ConditionMessage.CONTROLLER_FIELD_NULL::getMessage);

			throw new IllegalArgumentException(ConditionMessage.CONTROLLER_FIELD_NULL.getMessage());
		}

		if (operator == null) {

			FieldControlLogger.error(ConditionMessage.CONDITION_OPERATOR_NULL::getMessage);

			throw new IllegalArgumentException(ConditionMessage.CONDITION_OPERATOR_NULL.getMessage());
		}

		this.controllerField = controllerField;
		this.operator = operator;
		this.expectedValue = expectedValue;

		FieldControlLogger.debug(() -> "Field condition created | controllerField=" + controllerField + " | operator=" + operator
				+ " | expectedValue=" + expectedValue);
	}

	public E getControllerField() {
		return controllerField;
	}

	public ConditionOperator getOperator() {
		return operator;
	}

	public Object getExpectedValue() {
		return expectedValue;
	}

	@Override
	public String toString() {

		return "FieldCondition{" + "controllerField=" + controllerField + ", operator=" + operator + ", expectedValue="
				+ expectedValue + '}';
	}
}