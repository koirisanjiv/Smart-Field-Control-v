package com.qaverse.smart.FieldAccessControl.Model;

import java.util.EnumMap;
import java.util.Objects;

import com.qaverse.smart.FieldAccessControl.Configuration.ExecutionMode;
import com.qaverse.smart.FieldAccessControl.Configuration.OperationMode;
import com.qaverse.smart.FieldAccessControl.Configuration.UserType;

public final class FieldExecutionContext<E extends Enum<E>> {

	private final Class<E> page;

	private final OperationMode operationMode;

	private final ExecutionMode executionMode;

	private final UserType currentUser;

	private final EnumMap<E, Object> fieldValues;

	public FieldExecutionContext(Class<E> page, OperationMode operationMode, ExecutionMode executionMode,
			UserType currentUser, EnumMap<E, Object> fieldValues) {

		this.page = Objects.requireNonNull(page, "Page cannot be null");

		this.operationMode = Objects.requireNonNull(operationMode, "Operation mode cannot be null");

		this.executionMode = Objects.requireNonNull(executionMode, "Execution mode cannot be null");

		this.currentUser = Objects.requireNonNull(currentUser, "Current user cannot be null");

		Objects.requireNonNull(fieldValues, "Field values cannot be null");

		/*
		 * Create an internal snapshot.
		 *
		 * The caller can modify its original EnumMap after this constructor returns
		 * without affecting this execution context.
		 */
		this.fieldValues = new EnumMap<>(fieldValues);
	}

	public Class<E> getPage() {
		return page;
	}

	public OperationMode getOperationMode() {
		return operationMode;
	}

	public ExecutionMode getExecutionMode() {
		return executionMode;
	}

	public UserType getCurrentUser() {
		return currentUser;
	}

	/**
	 * Returns a defensive copy of the field values.
	 *
	 * <p>
	 * The internal execution snapshot cannot be modified by callers.
	 * </p>
	 */
	public EnumMap<E, Object> getFieldValues() {

		return new EnumMap<>(fieldValues);
	}
}