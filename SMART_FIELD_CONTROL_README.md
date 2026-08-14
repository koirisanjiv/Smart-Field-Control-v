# Qaverse Smart Field Control

Smart Field Control is a framework-independent Java library for evaluating field rules, definitions, permissions, execution modes, and conditional dependencies.

It does **not** execute browser/UI actions.

Its responsibility is:

> **Decide which fields are allowed to be processed and why.**

Actual UI execution is delegated to **Smart Core** or the consuming automation framework.

## 1. Product Responsibility

```text
Client / Test Project
        |
        | field values + context
        v
Smart Field Control
        |
        | evaluates
        v
FieldExecutionPlan
        |
        | allowed field decisions
        v
Smart Core
        |
        | actual execution
        v
Selenium / Playwright / other automation layer
```

### Smart Field Control owns

- Field definitions
- Mandatory/optional classification
- User-based permissions
- ADD/EDIT operation rules
- Field behavior
- Conditional field dependencies
- Execution modes
- Field evaluation
- Execution-plan generation

### Smart Field Control does not own

- Selenium
- TestNG
- WebDriver/WebElement
- Page Objects
- Browser interaction
- Click/type/select implementation
- Wait strategies
- Locator healing
- Action strategies

Those responsibilities belong to Smart Core.

## 2. Recommended Usage

The main public facade is:

```java
FieldExecutionPlan<PF_Admin> plan =
        FieldControl.evaluate(
                FieldControl
                        .request(PF_Admin.class)
                        .operationMode(OperationMode.EDIT)
                        .executionMode(ExecutionMode.ALL_FIELDS)
                        .currentUser(UserType.ADMIN)
                        .fieldValues(fieldValues)
                        .build()
        );
```

The consuming layer then processes:

```java
plan.getAllowedFields()
```

and performs actual UI actions.

## 3. Configure Rules

### Field definitions

```java
FieldControl.definitions(PF_Admin.class)
        .mandatory(
                PF_Admin.USER_NAME,
                PF_Admin.PASSWORD,
                PF_Admin.DATA_STORAGE
        );

FieldControl.definitions(PF_Admin.class)
        .optional(
                PF_Admin.MOBILE_NUMBER,
                PF_Admin.CITY
        );
```

A field not explicitly defined is currently treated as optional.

### Permission rules

```java
FieldControl.rules(PF_Admin.class)
        .editable(
                OperationMode.ADD,
                UserType.SUPER_ADMIN,
                PF_Admin.USER_NAME,
                PF_Admin.PASSWORD
        );

FieldControl.rules(PF_Admin.class)
        .readOnly(
                OperationMode.EDIT,
                UserType.ADMIN,
                PF_Admin.SHORT_NAME,
                PF_Admin.USER_NAME
        );

FieldControl.rules(PF_Admin.class)
        .hidden(
                OperationMode.EDIT,
                UserType.ADMIN,
                PF_Admin.ADMIN_CODE
        );
```

For execution, only `EDITABLE` fields are currently considered executable. `READ_ONLY` and `HIDDEN` remain distinct behaviors for future verification/state use cases.

## 4. Conditional Fields

```java
FieldConditionRegistry.when(
        PF_Admin.class,
        PF_Admin.ENABLE_SECURITY_PIN,
        ConditionOperator.TRUE
).controls(
        PF_Admin.SECURITY_PIN,
        PF_Admin.RETYPE_SECURITY_PIN
);
```

Conceptually:

```text
ENABLE_SECURITY_PIN == TRUE
             |
             +----> SECURITY_PIN
             +----> RETYPE_SECURITY_PIN
```

Available operators:

```text
TRUE
FALSE
EQUALS
NOT_EQUALS
EMPTY
NOT_EMPTY
```

## 5. Evaluation Modes

```text
ALL_FIELDS
MANDATORY_FIELDS
OPTIONAL_FIELDS
CUSTOM_FIELDS
```

`CUSTOM_FIELDS` is reserved for the custom-selection implementation and is currently not implemented.

## 6. Build a Request

```java
FieldControlRequest<PF_Admin> request =
        FieldControl
                .request(PF_Admin.class)
                .operationMode(OperationMode.EDIT)
                .executionMode(ExecutionMode.ALL_FIELDS)
                .currentUser(UserType.ADMIN)
                .fieldValues(fieldValues)
                .build();
```

Required information:

- Page enum
- Operation mode
- Current user
- Field values

Execution mode defaults to `ALL_FIELDS`.

## 7. Evaluate

```java
FieldExecutionPlan<PF_Admin> plan =
        FieldControl.evaluate(request);
```

No UI action happens here.

The plan contains field decisions:

```text
Field
  |
  +-- value
  +-- behavior
  +-- evaluation status
  +-- reason
```

## 8. Consume the Plan

The client/Smart Core layer can consume allowed fields:

```java
for (FieldDecision<PF_Admin> decision :
        plan.getAllowedFields()) {

    PF_Admin field = decision.getField();

    // Smart Core performs actual UI execution here.
}
```

For an existing client integration where actions are represented by `Runnable`:

```java
for (FieldDecision<E> decision :
        plan.getAllowedFields()) {

    E field = decision.getField();

    Runnable action = fieldActions.get(field);

    if (action == null) {
        throw new IllegalStateException(
                "No UI action registered for allowed field: " + field
        );
    }

    action.run();
}
```

The `Runnable` belongs to the client/Smart Core side, **not** Smart Field Control.

## 9. Evaluation Status

A decision can have:

```text
ALLOWED
SKIPPED_INVALID_VALUE
SKIPPED_NOT_REGISTERED
SKIPPED_NOT_EDITABLE
SKIPPED_EXECUTION_MODE
SKIPPED_CONDITION
SKIPPED_CUSTOM_FIELDS
```

Example:

```text
COUNTRY
  status = ALLOWED

PASSWORD
  status = SKIPPED_NOT_EDITABLE

SECURITY_PIN
  status = SKIPPED_CONDITION
```

## 10. Complete Example

```java
FieldControl.definitions(PF_Admin.class)
        .mandatory(
                PF_Admin.USER_NAME,
                PF_Admin.PASSWORD,
                PF_Admin.DATA_STORAGE
        );

FieldControl.rules(PF_Admin.class)
        .editable(
                OperationMode.EDIT,
                UserType.ADMIN,
                PF_Admin.COUNTRY,
                PF_Admin.STATE,
                PF_Admin.CITY
        );

FieldControl.rules(PF_Admin.class)
        .readOnly(
                OperationMode.EDIT,
                UserType.ADMIN,
                PF_Admin.USER_NAME
        );

FieldConditionRegistry.when(
        PF_Admin.class,
        PF_Admin.ENABLE_SECURITY_PIN,
        ConditionOperator.TRUE
).controls(
        PF_Admin.SECURITY_PIN,
        PF_Admin.RETYPE_SECURITY_PIN
);

EnumMap<PF_Admin, Object> values =
        new EnumMap<>(PF_Admin.class);

values.put(PF_Admin.COUNTRY, "India");
values.put(PF_Admin.STATE, "Gujarat");
values.put(PF_Admin.CITY, "Surat");
values.put(PF_Admin.USER_NAME, "admin01");

FieldExecutionPlan<PF_Admin> plan =
        FieldControl.evaluate(
                FieldControl
                        .request(PF_Admin.class)
                        .operationMode(OperationMode.EDIT)
                        .executionMode(ExecutionMode.ALL_FIELDS)
                        .currentUser(UserType.ADMIN)
                        .fieldValues(values)
                        .build()
        );

for (FieldDecision<PF_Admin> decision :
        plan.getAllowedFields()) {

    // Actual automation execution belongs here.
}
```

## 11. Cleanup

For framework/test lifecycle cleanup:

```java
FieldControl.clear();
```

This clears rules, definitions, conditions, and page metadata.

## 12. Core Design Principle

> **Smart Field Control decides WHAT is allowed. Smart Core decides HOW it is executed.**

This separation keeps Smart Field Control reusable across automation technologies.
