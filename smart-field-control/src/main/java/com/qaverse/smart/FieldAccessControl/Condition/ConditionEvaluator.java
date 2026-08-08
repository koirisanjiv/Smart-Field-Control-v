package com.qaverse.smart.FieldAccessControl.Condition;

public final class ConditionEvaluator {

    private ConditionEvaluator() {
    }

    public static boolean evaluate(
            Object actualValue,
            ConditionOperator operator,
            Object expectedValue) {

        String actual =
                actualValue == null
                        ? ""
                        : actualValue.toString().trim();

        switch (operator) {

            case EQUALS:
                return actual.equalsIgnoreCase(
                        String.valueOf(expectedValue).trim()
                );

            case NOT_EQUALS:
                return !actual.equalsIgnoreCase(
                        String.valueOf(expectedValue).trim()
                );

            case TRUE:
                return Boolean.parseBoolean(actual);

            case FALSE:
                return !Boolean.parseBoolean(actual);

            case NOT_EMPTY:
                return !actual.isEmpty()
                        && !"NA".equalsIgnoreCase(actual);

            case EMPTY:
                return actual.isEmpty()
                        || "NA".equalsIgnoreCase(actual);

            default:
                return false;
        }
    }
}