package com.qaverse.smart.FieldAccessControl.Condition;

public final class FieldCondition<E extends Enum<E>> {

    private final E controllerField;
    private final ConditionOperator operator;
    private final Object expectedValue;

    public FieldCondition(
            E controllerField,
            ConditionOperator operator) {

        this(
                controllerField,
                operator,
                null
        );
    }

    public FieldCondition(
            E controllerField,
            ConditionOperator operator,
            Object expectedValue) {

        if (controllerField == null) {
            throw new IllegalArgumentException(
                    "Controller field cannot be null"
            );
        }

        if (operator == null) {
            throw new IllegalArgumentException(
                    "Condition operator cannot be null"
            );
        }

        this.controllerField = controllerField;
        this.operator = operator;
        this.expectedValue = expectedValue;
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

        return "FieldCondition{" +
                "controllerField=" + controllerField +
                ", operator=" + operator +
                ", expectedValue=" + expectedValue +
                '}';
    }
}