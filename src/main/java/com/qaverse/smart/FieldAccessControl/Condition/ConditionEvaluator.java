package com.qaverse.smart.FieldAccessControl.Condition;

import com.qaverse.smart.FieldAccessControl.Logging.FieldControlLogger;

public final class ConditionEvaluator {

    private ConditionEvaluator() {
    }

    public static boolean evaluate(
            Object actualValue,
            ConditionOperator operator,
            Object expectedValue) {

        FieldControlLogger.debug(() ->
                "Evaluating field condition | operator="
                + operator
                + " | actualValue="
                + actualValue
                + " | expectedValue="
                + expectedValue);

        String actual =
                actualValue == null
                        ? ""
                        : actualValue.toString().trim();

        boolean result;

        switch (operator) {

            case EQUALS:
                result = actual.equalsIgnoreCase(
                        String.valueOf(expectedValue).trim()
                );
                break;

            case NOT_EQUALS:
                result = !actual.equalsIgnoreCase(
                        String.valueOf(expectedValue).trim()
                );
                break;

            case TRUE:
                result = Boolean.parseBoolean(actual);
                break;

            case FALSE:
                result = !Boolean.parseBoolean(actual);
                break;

            case NOT_EMPTY:
                result = !actual.isEmpty()
                        && !"NA".equalsIgnoreCase(actual);
                break;

            case EMPTY:
                result = actual.isEmpty()
                        || "NA".equalsIgnoreCase(actual);
                break;

            default:
                result = false;
                break;
        }

        FieldControlLogger.debug(() ->
                "Field condition evaluated | operator="
                + operator
                + " | result="
                + result);

        return result;
    }
}