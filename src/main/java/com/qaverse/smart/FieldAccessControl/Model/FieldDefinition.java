package com.qaverse.smart.FieldAccessControl.Model;

public final class FieldDefinition {

    /**
     * Default definition.
     * Any field that is not explicitly registered
     * is considered optional.
     */
    public static final FieldDefinition DEFAULT =
            new FieldDefinition(false);

    private static final FieldDefinition MANDATORY =
            new FieldDefinition(true);

    private static final FieldDefinition OPTIONAL =
            DEFAULT;

    private final boolean mandatory;

    private FieldDefinition(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public static FieldDefinition mandatory() {
        return MANDATORY;
    }

    public static FieldDefinition optional() {
        return OPTIONAL;
    }

    @Override
    public String toString() {
        return mandatory
                ? "MANDATORY"
                : "OPTIONAL";
    }
}