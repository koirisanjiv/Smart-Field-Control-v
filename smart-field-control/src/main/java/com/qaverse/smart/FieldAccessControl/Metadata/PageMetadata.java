package com.qaverse.smart.FieldAccessControl.Metadata;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.qaverse.smart.FieldAccessControl.Configuration.ExecutionMode;

public final class PageMetadata<F extends Enum<F>> {

    private final Set<F> mandatoryFields;

    private final ExecutionMode defaultExecutionMode;

    public PageMetadata(
            Set<F> mandatoryFields,
            ExecutionMode defaultExecutionMode) {

        this.mandatoryFields =
                Collections.unmodifiableSet(
                        EnumSet.copyOf(mandatoryFields)
                );

        this.defaultExecutionMode =
                defaultExecutionMode;
    }

    public Set<F> getMandatoryFields() {
        return mandatoryFields;
    }

    public ExecutionMode getDefaultExecutionMode() {
        return defaultExecutionMode;
    }
}