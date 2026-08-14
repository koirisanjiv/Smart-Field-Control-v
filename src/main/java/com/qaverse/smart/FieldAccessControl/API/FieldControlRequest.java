package com.qaverse.smart.FieldAccessControl.API;

import java.util.EnumMap;
import java.util.Objects;

import com.qaverse.smart.FieldAccessControl.Configuration.ExecutionMode;
import com.qaverse.smart.FieldAccessControl.Configuration.OperationMode;
import com.qaverse.smart.FieldAccessControl.Configuration.UserType;

public final class FieldControlRequest<E extends Enum<E>> {

	private final Class<E> page;
	private final OperationMode operationMode;
	private final ExecutionMode executionMode;
	private final UserType currentUser;
	private final EnumMap<E, Object> fieldValues;

	private FieldControlRequest(Class<E> page, OperationMode operationMode, ExecutionMode executionMode,
			UserType currentUser, EnumMap<E, Object> fieldValues) {

		this.page = Objects.requireNonNull(page, "Page cannot be null");

		this.operationMode = Objects.requireNonNull(operationMode, "Operation mode cannot be null");

		this.executionMode = Objects.requireNonNull(executionMode, "Execution mode cannot be null");

		this.currentUser = Objects.requireNonNull(currentUser, "Current user cannot be null");

		Objects.requireNonNull(fieldValues, "Field values cannot be null");

		this.fieldValues = new EnumMap<>(fieldValues);
	}

	public static <E extends Enum<E>> Builder<E> builder(Class<E> page) {

		return new Builder<>(page);
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

	public EnumMap<E, Object> getFieldValues() {
		return new EnumMap<>(fieldValues);
	}

	public static final class Builder<E extends Enum<E>> {

		private final Class<E> page;

		private OperationMode operationMode;

		private ExecutionMode executionMode = ExecutionMode.ALL_FIELDS;

		private UserType currentUser;

		private EnumMap<E, Object> fieldValues;

		private Builder(Class<E> page) {

			this.page = Objects.requireNonNull(page, "Page cannot be null");
		}

		public Builder<E> operationMode(OperationMode operationMode) {

			this.operationMode = Objects.requireNonNull(operationMode, "Operation mode cannot be null");

			return this;
		}

		public Builder<E> executionMode(ExecutionMode executionMode) {

			this.executionMode = Objects.requireNonNull(executionMode, "Execution mode cannot be null");

			return this;
		}

		public Builder<E> currentUser(UserType currentUser) {

			this.currentUser = Objects.requireNonNull(currentUser, "Current user cannot be null");

			return this;
		}

		public Builder<E> fieldValues(EnumMap<E, Object> fieldValues) {

			this.fieldValues = Objects.requireNonNull(fieldValues, "Field values cannot be null");

			return this;
		}

		public FieldControlRequest<E> build() {

			Objects.requireNonNull(operationMode, "Operation mode must be specified");

			Objects.requireNonNull(currentUser, "Current user must be specified");

			Objects.requireNonNull(fieldValues, "Field values must be specified");

			return new FieldControlRequest<>(page, operationMode, executionMode, currentUser, fieldValues);
		}
	}
}