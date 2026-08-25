package com.qaverse.smart.FieldAccessControl.Builder;

import com.qaverse.smart.FieldAccessControl.Condition.FieldCondition;
import com.qaverse.smart.FieldAccessControl.Logging.FieldControlLogger;
import com.qaverse.smart.FieldAccessControl.Registry.FieldConditionRegistry;

public final class ConditionBuilder<E extends Enum<E>> {

	private final Class<E> page;
	private final FieldCondition<E> condition;

	public ConditionBuilder(Class<E> page, FieldCondition<E> condition) {

		FieldControlLogger.debug(() -> "Creating condition builder | page=" + (page != null ? page.getSimpleName() : "null"));

		if (page == null) {

			FieldControlLogger.error(BuilderMessage.PAGE_NULL::getMessage);

			throw new IllegalArgumentException(BuilderMessage.PAGE_NULL.getMessage());
		}

		if (condition == null) {

			FieldControlLogger.error(BuilderMessage.FIELD_CONDITION_NULL::getMessage);

			throw new IllegalArgumentException(BuilderMessage.FIELD_CONDITION_NULL.getMessage());
		}

		this.page = page;
		this.condition = condition;

		FieldControlLogger.debug(() -> "Condition builder created | page=" + page.getSimpleName());
	}

	@SafeVarargs
	public final void controls(E... dependentFields) {

		if (dependentFields == null || dependentFields.length == 0) {

			FieldControlLogger.error(BuilderMessage.DEPENDENT_FIELD_REQUIRED::getMessage);

			throw new IllegalArgumentException(BuilderMessage.DEPENDENT_FIELD_REQUIRED.getMessage());
		}

		FieldControlLogger.debug(() -> "Registering field condition | page=" + page.getSimpleName() + " | dependentFields="
				+ dependentFields.length);

		FieldConditionRegistry.register(page, condition, dependentFields);

		FieldControlLogger.debug(() -> "Field condition registered | page=" + page.getSimpleName() + " | dependentFields="
				+ dependentFields.length);
	}
}