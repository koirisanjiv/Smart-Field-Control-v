package com.qaverse.smart.FieldAccessControl.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class FieldExecutionPlan<E extends Enum<E>> {

	private final Class<E> page;

	private final List<FieldDecision<E>> decisions;

	public FieldExecutionPlan(Class<E> page, List<FieldDecision<E>> decisions) {

		this.page = Objects.requireNonNull(page, "Page cannot be null");

		Objects.requireNonNull(decisions, "Decisions cannot be null");

		this.decisions = Collections.unmodifiableList(new ArrayList<>(decisions));
	}

	public Class<E> getPage() {
		return page;
	}

	public List<FieldDecision<E>> getDecisions() {
		return decisions;
	}

	public List<FieldDecision<E>> getAllowedFields() {

		List<FieldDecision<E>> allowed = new ArrayList<>();

		for (FieldDecision<E> decision : decisions) {

			if (decision.isAllowed()) {
				allowed.add(decision);
			}
		}

		return Collections.unmodifiableList(allowed);
	}

	public List<FieldDecision<E>> getSkippedFields() {

		List<FieldDecision<E>> skipped = new ArrayList<>();

		for (FieldDecision<E> decision : decisions) {

			if (!decision.isAllowed()) {
				skipped.add(decision);
			}
		}

		return Collections.unmodifiableList(skipped);
	}

	public boolean hasExecutableFields() {
		return decisions.stream().anyMatch(FieldDecision::isAllowed);
	}

	public int size() {
		return decisions.size();
	}

	@Override
	public String toString() {

		return "FieldExecutionPlan{" + "page=" + page.getSimpleName() + ", decisions=" + decisions + '}';
	}
}