package com.qaverse.smart.FieldAccessControl.Configuration;

/**
 * Defines which fields should be executed during automation.
 */
public enum ExecutionMode {

    /**
     * Execute every editable field.
     */
    ALL_FIELDS,

    /**
     * Execute only mandatory editable fields.
     */
    MANDATORY_FIELDS,

    /**
     * Execute only optional editable fields.
     */
    OPTIONAL_FIELDS,

    /**
     * Execute only fields explicitly selected by the user.
     */
	CUSTOM_FIELDS
    
}
