package com.qaverse.smart.FieldAccessControl.Execution;

import java.util.Objects;

import com.qaverse.smart.FieldAccessControl.Configuration.FieldBehavior;
import com.qaverse.smart.FieldAccessControl.Model.FieldDecision;

public final class FieldEvaluationResult<E extends Enum<E>> {

    private final E field;

    private final Object value;

    private final FieldEvaluationStatus status;

    private final FieldBehavior behavior;

    private final String reason;

    private FieldEvaluationResult(
            E field,
            Object value,
            FieldEvaluationStatus status,
            FieldBehavior behavior,
            String reason) {

        this.field = Objects.requireNonNull(
                field,
                ExecutionMessage.FIELD_NULL.getMessage()
        );

        this.value = value;

        this.status = Objects.requireNonNull(
                status,
                ExecutionMessage.EVALUATION_STATUS_NULL.getMessage()
        );

        this.behavior = behavior;

        this.reason = reason;
    }

    public static <E extends Enum<E>> FieldEvaluationResult<E> allowed(
            E field,
            Object value,
            FieldBehavior behavior) {

        return new FieldEvaluationResult<>(
                field,
                value,
                FieldEvaluationStatus.ALLOWED,
                behavior,
                ExecutionMessage.FIELD_PASSED_RULES.getMessage()
        );
    }

    public static <E extends Enum<E>> FieldEvaluationResult<E> skipped(
            E field,
            Object value,
            FieldEvaluationStatus status,
            FieldBehavior behavior,
            String reason) {

        return new FieldEvaluationResult<>(
                field,
                value,
                status,
                behavior,
                reason
        );
    }

    public E getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    public FieldEvaluationStatus getStatus() {
        return status;
    }

    public FieldBehavior getBehavior() {
        return behavior;
    }

    public String getReason() {
        return reason;
    }

    public boolean isAllowed() {
        return status == FieldEvaluationStatus.ALLOWED;
    }

    public FieldDecision<E> toDecision() {

        return new FieldDecision<>(
                field,
                value,
                behavior,
                status,
                reason
        );
    }

    @Override
    public String toString() {

        return "FieldEvaluationResult{"
                + "field=" + field
                + ", value=" + value
                + ", status=" + status
                + ", behavior=" + behavior
                + ", reason='" + reason + '\''
                + '}';
    }
}