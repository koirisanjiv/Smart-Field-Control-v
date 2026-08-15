package com.qaverse.smart.FieldAccessControl.Model;

import java.util.Objects;

import com.qaverse.smart.FieldAccessControl.Configuration.FieldBehavior;
import com.qaverse.smart.FieldAccessControl.Execution.FieldEvaluationStatus;

public final class FieldDecision<E extends Enum<E>> {

    private final E field;

    private final Object value;

    private final FieldBehavior behavior;

    private final FieldEvaluationStatus status;

    private final String reason;

    public FieldDecision(
            E field,
            Object value,
            FieldBehavior behavior,
            FieldEvaluationStatus status,
            String reason) {

        this.field = Objects.requireNonNull(
                field,
                ModelMessage.FIELD_NULL.getMessage()
        );

        this.value = value;

        this.behavior = behavior;

        this.status = Objects.requireNonNull(
                status,
                ModelMessage.EVALUATION_STATUS_NULL.getMessage()
        );

        this.reason = reason;
    }

    public E getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    public FieldBehavior getBehavior() {
        return behavior;
    }

    public FieldEvaluationStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public boolean isAllowed() {
        return status == FieldEvaluationStatus.ALLOWED;
    }

    @Override
    public String toString() {

        return "FieldDecision{"
                + "field=" + field
                + ", value=" + value
                + ", behavior=" + behavior
                + ", status=" + status
                + ", reason='" + reason + '\''
                + '}';
    }
}