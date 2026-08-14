# Qaverse Smart Field Control — Architecture & Construction

## 1. Purpose

Smart Field Control is a framework-independent decision engine for field-level automation control.

It converts:

```text
Page + Operation + User + Values + Configuration
```

into:

```text
FieldExecutionPlan
```

The plan is consumed by Smart Core.

## 2. High-Level Architecture

```text
                         SMART FIELD CONTROL
                                  |
                         +--------+--------+
                         |   FieldControl  |
                         |   Public API    |
                         +--------+--------+
                                  |
                 +----------------+----------------+
                 |                |                |
                 v                v                v
              Rules         Definitions       Conditions
                 |                |                |
                 +----------------+----------------+
                                  |
                                  v
                       FieldControlRequest
                                  |
                                  v
                       FieldExecutionContext
                                  |
                                  v
                          FieldEvaluator
                                  |
                                  v
                         FieldEvaluationResult
                                  |
                                  v
                            FieldDecision
                                  |
                                  v
                        FieldExecutionPlan
                                  |
                                  v
                             SMART CORE
                                  |
                                  v
                    Browser / UI automation layer
```

## 3. Package Structure

```text
com.qaverse.smart.FieldAccessControl
|
+-- API
|   +-- FieldControl
|   +-- FieldControlRequest
|
+-- Builder
|   +-- ConditionBuilder
|   +-- FieldDefinitionBuilder
|   +-- FieldExecutionContextBuilder
|   +-- RuleBuilder
|
+-- Condition
|   +-- ConditionEvaluator
|   +-- ConditionOperator
|   +-- FieldCondition
|
+-- Configuration
|   +-- ExecutionMode
|   +-- FieldBehavior
|   +-- OperationMode
|   +-- UserType
|   +-- UserTypeResolver
|
+-- Execution
|   +-- FieldEvaluator
|   +-- FieldEvaluationResult
|   +-- FieldEvaluationStatus
|
+-- Metadata
|   +-- PageMetadata
|
+-- Model
|   +-- FieldContext
|   +-- FieldDefinition
|   +-- FieldDecision
|   +-- FieldExecutionContext
|   +-- FieldExecutionPlan
|   +-- FieldKey
|
+-- Registry
    +-- FieldConditionRegistry
    +-- FieldDefinitionRegistry
    +-- FieldRegistry
    +-- MetadataRegistry
```

## 4. API Layer

`FieldControl` is the primary public facade.

Responsibilities:

- Create rule builders
- Create definition builders
- Create request builders
- Evaluate requests
- Lifecycle cleanup

Recommended API:

```java
FieldExecutionPlan<E> plan =
        FieldControl.evaluate(
                FieldControl
                        .request(Page.class)
                        .operationMode(OperationMode.EDIT)
                        .currentUser(UserType.ADMIN)
                        .fieldValues(values)
                        .build()
        );
```

Consumers should not need to directly interact with registries or evaluators.

## 5. Request Layer

`FieldControlRequest` represents an evaluation request:

```text
Page
OperationMode
ExecutionMode
UserType
FieldValues
```

It forms the clean boundary between the client and the Field Control engine and uses defensive copies for field values.

## 6. Builder Layer

### RuleBuilder

Registers:

```text
EDITABLE
READ_ONLY
HIDDEN
```

for:

```text
Page + Operation + User + Field
```

### FieldDefinitionBuilder

Registers:

```text
MANDATORY
OPTIONAL
```

### ConditionBuilder

Connects a controller field to dependent fields.

### FieldExecutionContextBuilder

Provides a programmatic builder for `FieldExecutionContext`. It is primarily a construction convenience; consumers should prefer `FieldControl`.

## 7. Configuration Layer

### OperationMode

```text
ADD
EDIT
```

### UserType

Current roles:

```text
SUPER_ADMIN
ADMIN
ADMIN_SUB_USER
RESELLER
RESELLER_SUB_USER
COMPANY
COMPANY_SUB_USER
```

Aliases are resolved through `UserTypeResolver`.

### FieldBehavior

```text
EDITABLE
READ_ONLY
HIDDEN
```

### ExecutionMode

```text
ALL_FIELDS
MANDATORY_FIELDS
OPTIONAL_FIELDS
CUSTOM_FIELDS
```

## 8. Condition Layer

`FieldCondition` represents:

```text
controller field
operator
expected value
```

`ConditionOperator` currently supports:

```text
TRUE
FALSE
EQUALS
NOT_EQUALS
EMPTY
NOT_EMPTY
```

`ConditionEvaluator` evaluates controller values against conditions. The current implementation normalizes values to strings for comparison.

## 9. Registry Layer

### FieldRegistry

Key:

```text
Page + OperationMode + UserType + Field
```

Value:

```text
FieldBehavior
```

### FieldDefinitionRegistry

Stores:

```text
Page + Field -> FieldDefinition
```

Undefined fields currently resolve to `OPTIONAL`.

### FieldConditionRegistry

Stores:

```text
Page + DependentField -> FieldCondition
```

### MetadataRegistry

Stores page-level metadata.

## 10. Execution Layer

`FieldEvaluator` is the core evaluation engine.

Evaluation order:

```text
1. Validate value
2. Create field context
3. Check registration
4. Resolve behavior
5. Check EDITABLE
6. Resolve definition
7. Check ExecutionMode
8. Evaluate condition
9. Produce evaluation result
```

No browser action is performed.

## 11. FieldEvaluationResult

Internal evaluation result containing:

```text
Field
Value
Behavior
Status
Reason
```

It can be converted into a `FieldDecision`.

## 12. FieldEvaluationStatus

```text
ALLOWED
SKIPPED_INVALID_VALUE
SKIPPED_NOT_REGISTERED
SKIPPED_NOT_EDITABLE
SKIPPED_EXECUTION_MODE
SKIPPED_CONDITION
SKIPPED_CUSTOM_FIELDS
```

## 13. FieldDecision

`FieldDecision` is the field-level contract exposed through the plan:

```text
Field
Value
Behavior
Status
Reason
```

Smart Core uses this information to decide whether and how to execute the field.

## 14. FieldExecutionPlan

`FieldExecutionPlan` is the primary output of Smart Field Control.

It contains an immutable list of field decisions.

Important operations:

```java
plan.getDecisions();
plan.getAllowedFields();
plan.getSkippedFields();
plan.hasExecutableFields();
plan.size();
```

## 15. Framework Boundary

Smart Field Control must not contain:

```text
Selenium
TestNG
WebDriver
WebElement
POM
Page Object
Browser
Runnable UI actions
```

Smart Core owns:

```text
Locator resolution
Action strategies
Click/type/select
Waits
Browser interaction
Locator healing
JavaScript execution
Reporting
Test lifecycle
```

## 16. Client Integration

The consuming project may still have UI actions:

```java
EnumMap<E, Runnable> fieldActions =
        new EnumMap<>(getEnumClass());
```

That is acceptable because those actions are outside Smart Field Control.

The integration flow is:

```java
FieldControlRequest<E> request =
        FieldControl
                .request(getEnumClass())
                .operationMode(operationMode)
                .executionMode(executionMode)
                .currentUser(currentUser)
                .fieldValues(args)
                .build();

FieldExecutionPlan<E> plan =
        FieldControl.evaluate(request);

for (FieldDecision<E> decision :
        plan.getAllowedFields()) {

    E field = decision.getField();

    Runnable action = fieldActions.get(field);

    if (action == null) {
        throw new IllegalStateException(
                "No UI action registered for allowed field: "
                        + field
        );
    }

    action.run();
}
```

## 17. Data Flow

```text
Excel / JSON / Data Source
          |
          v
EnumMap<Field, Object>
          |
          v
FieldControlRequest
          |
          v
FieldExecutionContext
          |
          v
FieldEvaluator
          |
          +---- FieldRegistry
          +---- FieldDefinitionRegistry
          +---- FieldConditionRegistry
          |
          v
FieldEvaluationResult
          |
          v
FieldDecision
          |
          v
FieldExecutionPlan
          |
          v
Smart Core
```

## 18. Immutability Strategy

The request and execution context use defensive copies.

```text
Client Map
    |
    v
FieldControlRequest
    |
    | defensive copy
    v
FieldExecutionContext
    |
    | evaluation snapshot
    v
FieldExecutionPlan
```

The plan also exposes an immutable decision list.

## 19. Error Handling Principles

Configuration errors should fail early:

```text
null page
null operation
null user
null field values
null condition
null dependent field
invalid user type
self-referencing condition
```

Runtime UI errors belong to Smart Core.

## 20. Current Limitations / Future Work

### Custom fields

`ExecutionMode.CUSTOM_FIELDS` needs a selected-field model.

### Composite conditions

Future support:

```text
A == X AND B != Y
A == X OR B == Y
```

### Typed conditions

Future support for:

```text
Number
Date
Boolean
Enum
```

without relying only on string conversion.

### Configuration lifecycle

Future:

```text
CONFIGURING
    |
    v
VALIDATE
    |
    v
FREEZE
    |
    v
EXECUTE
```

This will make registry configuration safer for parallel execution.

### Duplicate registration policy

Future configuration validation should detect accidental conflicting registrations.

## 21. Development Rules

1. Do not add Selenium/TestNG/POM dependencies to Smart Field Control.
2. Do not add browser actions to `FieldEvaluator`.
3. Do not put `Runnable` into the SFC domain model.
4. Keep `FieldControl` as the main public facade.
5. Prefer immutable request/context/result objects.
6. Keep Smart Core responsible for actual execution.
7. Keep configuration separate from evaluation.
8. Prefer deterministic decisions with explicit reasons.
9. Avoid mixing UI execution into field policy.
10. Preserve the SFC -> Smart Core contract.

## 22. Product Boundary

```text
SMART FIELD CONTROL
"What should happen?"

        +

SMART CORE
"How should it happen?"
```

This separation makes Smart Field Control a reusable field-policy and decision engine rather than a browser automation library.
