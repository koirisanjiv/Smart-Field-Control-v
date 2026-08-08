package com.qaverse.smart.FieldAccessControl.Configuration;

public enum FieldBehavior {

    /**
     * Field is not available.
     */

    HIDDEN,

    /**
     * Field is visible but user cannot modify.
     */

    READ_ONLY,

    /**
     * User can modify the field.
     */

    EDITABLE;
}