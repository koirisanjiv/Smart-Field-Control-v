# Qaverse Smart Field Control

**Smart Field Control (SFC)** is a framework-independent Java library
for evaluating field definitions, permissions, execution modes,
conditional dependencies, parent/child field groups, and field
eligibility.

It **does not execute browser or UI actions**.

Its responsibility is:

> **Decide which fields are allowed to be processed, which fields must
> be skipped, and why.**

Actual UI execution is delegated to **Smart Core** or the consuming
automation framework.

------------------------------------------------------------------------

## 1. Product Responsibility

``` text
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
        | field decisions
        v
Smart Core
        |
        | actual execution
        v
Selenium / Playwright / other automation layer
```

### Smart Field Control owns

-   Field definitions
-   Mandatory/optional classification
-   User-based permissions
-   ADD/EDIT operation rules
-   Field behavior
-   Conditional field dependencies
-   Parent/child field-group dependencies
-   Execution modes
-   Field evaluation
-   Execution-plan generation
-   Evaluation status and skip reasons

### Smart Field Control does not own

-   Selenium
-   TestNG
-   WebDriver/WebElement
-   Page Objects
-   Browser interaction
-   Click/type/select implementation
-   Wait strategies
-   Locator handling/healing
-   UI action strategies
-   `Runnable` execution

Those responsibilities belong to Smart Core or the consuming framework.

------------------------------------------------------------------------

# 2. Core Design Principle

> **Smart Field Control decides WHAT is allowed. Smart Core decides HOW
> it is executed.**

SFC is intentionally independent of any browser automation technology.

The same field-control rules can therefore be reused by different
automation implementations.

------------------------------------------------------------------------

# 3. Recommended Usage

The main public facade is:

``` java
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

The consuming layer then processes the returned plan:

``` java
for (FieldDecision<PF_Admin> decision :
        plan.getAllowedFields()) {

    PF_Admin field = decision.getField();

    // Smart Core performs the actual UI execution here.
}
```

SFC only evaluates and returns decisions.

------------------------------------------------------------------------

# 4. Field Definitions

Field definitions describe whether a field is mandatory or optional.

``` java
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

A field that is not explicitly defined is currently treated as
**optional**.

Definitions are evaluated together with the supplied field values and
execution mode.

------------------------------------------------------------------------

# 5. Permission and Field Behavior Rules

Permissions define what a user can do with a field for a specific
operation.

``` java
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

Current field behaviors include:

``` text
EDITABLE
READ_ONLY
HIDDEN
```

For execution-plan processing, only `EDITABLE` fields are currently
considered executable.

`READ_ONLY` and `HIDDEN` remain distinct behaviors and can be used by
future verification/state-oriented use cases.

------------------------------------------------------------------------

# 6. Conditional Fields

Conditional rules define whether a dependent field is eligible based on
another field's value.

``` java
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

``` text
ENABLE_SECURITY_PIN == TRUE
             |
             +----> SECURITY_PIN
             +----> RETYPE_SECURITY_PIN
```

Available operators:

``` text
TRUE
FALSE
EQUALS
NOT_EQUALS
EMPTY
NOT_EMPTY
```

Conditions are evaluated using the field values supplied in the
`FieldControlRequest`.

------------------------------------------------------------------------

# 7. Parent / Child Field Groups

SFC supports parent-controlled field groups.

A field can belong to a group controlled by one or more
parent/controller fields.

``` java
FieldGroupRegistry.group(
        PF_Job.class,
        PF_Job.WANT_TO_ADD_CHECKPOINT
).controls(
        PF_Job.CHECKPOINT_POI,
        PF_Job.CHECKPOINT_NAME,
        PF_Job.CHECKPOINT_STATE
);
```

Multiple controllers can be supplied:

``` java
FieldGroupRegistry.group(
        PF_Job.class,
        PF_Job.WANT_TO_ADD_CHECKPOINT,
        PF_Job.WANT_TO_ADD_START_CHECKPOINT,
        PF_Job.WANT_TO_ADD_END_CHECKPOINT
).controls(
        PF_Job.SEARCH_LOCATION
);
```

### Controller behavior

Multiple controllers are treated as an **OR** relationship.

Conceptually:

``` text
Controller A == TRUE
       OR
Controller B == TRUE
       OR
Controller C == TRUE
       |
       +----> child field is eligible
```

If none of the registered parent/controller fields is active, the child
field is skipped.

This is separate from normal field conditions.

### Parent group vs. conditional dependency

They solve different problems:

``` text
Parent / Field Group
--------------------
Controls whether a field belongs to an active UI section/group.

Condition
---------
Controls whether a field is eligible based on a specific condition.
```

Both checks can participate in the final field decision.

------------------------------------------------------------------------

# 8. Field Evaluation Flow

SFC evaluates fields through a consistent sequence.

Conceptually:

``` text
Field value
    |
    v
Validate value
    |
    v
Field registered?
    |
    v
Field behavior / editable?
    |
    v
Execution mode?
    |
    v
Parent group active?
    |
    v
Normal conditions satisfied?
    |
    v
ALLOWED
```

A field can therefore be skipped for different reasons without executing
any UI action.

------------------------------------------------------------------------

# 9. Invalid Field Values

Field evaluation validates the supplied value before allowing execution.

Values that are treated as invalid for normal processing include:

``` text
null
empty string
NA
```

Invalid values are represented through:

``` text
SKIPPED_INVALID_VALUE
```

This prevents invalid/unavailable field values from being sent to the
consuming UI execution layer.

------------------------------------------------------------------------

# 10. Evaluation Modes

Supported execution modes are:

``` text
ALL_FIELDS
MANDATORY_FIELDS
OPTIONAL_FIELDS
CUSTOM_FIELDS
```

### ALL_FIELDS

Evaluates all supplied fields that satisfy registration, permissions,
parent-group, and condition rules.

### MANDATORY_FIELDS

Restricts evaluation to mandatory fields.

### OPTIONAL_FIELDS

Restricts evaluation to optional fields.

### CUSTOM_FIELDS

Reserved for custom field selection.

The custom-selection implementation is currently not implemented.

------------------------------------------------------------------------

# 11. Build a Request

``` java
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

-   Page enum
-   Operation mode
-   Current user
-   Field values

Execution mode defaults to:

``` text
ALL_FIELDS
```

The request is framework-independent and contains only evaluation
data/context.

------------------------------------------------------------------------

# 12. FieldExecutionContext

During evaluation, SFC creates a framework-independent field context
containing the information required to evaluate a field.

The context is intended to remain independent of:

-   Selenium
-   TestNG
-   WebDriver
-   WebElement
-   Page Objects
-   Browser state

This keeps the evaluator reusable across automation frameworks.

------------------------------------------------------------------------

# 13. Evaluate

``` java
FieldExecutionPlan<PF_Admin> plan =
        FieldControl.evaluate(request);
```

No UI action happens here.

The result is a `FieldExecutionPlan` containing field decisions.

Conceptually:

``` text
FieldDecision
    |
    +-- field
    +-- value
    +-- behavior
    +-- evaluation status
    +-- reason
```

The plan can expose allowed fields as well as skipped decisions.

------------------------------------------------------------------------

# 14. Consume the Plan

The consuming layer can process allowed fields:

``` java
for (FieldDecision<PF_Admin> decision :
        plan.getAllowedFields()) {

    PF_Admin field = decision.getField();

    // Smart Core performs actual UI execution.
}
```

For example, Smart Core can maintain its own field-to-action mapping:

``` java
Map<PF_Admin, Runnable> fieldActions;
```

and execute it outside SFC:

``` java
for (FieldDecision<PF_Admin> decision :
        plan.getAllowedFields()) {

    PF_Admin field = decision.getField();

    Runnable action = fieldActions.get(field);

    if (action == null) {
        throw new IllegalStateException(
                "No UI action registered for allowed field: " + field
        );
    }

    action.run();
}
```

### Important architectural rule

The `Runnable` belongs to the **client/Smart Core execution layer**.

Smart Field Control:

-   does not receive `Runnable`
-   does not execute `Runnable`
-   does not know what the UI action does

It only returns the decision that a field is allowed to be processed.

------------------------------------------------------------------------

# 15. Evaluation Status

A field decision can have one of the following statuses:

``` text
ALLOWED

SKIPPED_INVALID_VALUE
SKIPPED_NOT_REGISTERED
SKIPPED_NOT_EDITABLE
SKIPPED_EXECUTION_MODE
SKIPPED_PARENT
SKIPPED_CONDITION
SKIPPED_CUSTOM_FIELDS
```

### Status meanings

  -----------------------------------------------------------------------
  Status                              Meaning
  ----------------------------------- -----------------------------------
  `ALLOWED`                           Field passed all applicable checks
                                      and can be processed.

  `SKIPPED_INVALID_VALUE`             Supplied value is
                                      invalid/unavailable.

  `SKIPPED_NOT_REGISTERED`            Field is not registered in the
                                      applicable configuration.

  `SKIPPED_NOT_EDITABLE`              Field is not executable because its
                                      behavior is not `EDITABLE`.

  `SKIPPED_EXECUTION_MODE`            Field is excluded by the selected
                                      execution mode.

  `SKIPPED_PARENT`                    Field's parent/controller group is
                                      not active.

  `SKIPPED_CONDITION`                 One or more normal field conditions
                                      are not satisfied.

  `SKIPPED_CUSTOM_FIELDS`             Field is excluded because
                                      custom-field selection is not
                                      implemented.
  -----------------------------------------------------------------------

Example:

``` text
COUNTRY
  status = ALLOWED

PASSWORD
  status = SKIPPED_NOT_EDITABLE

SECURITY_PIN
  status = SKIPPED_CONDITION

CHECKPOINT_POI
  status = SKIPPED_PARENT
```

This makes the execution plan explainable instead of simply returning a
list of fields.

------------------------------------------------------------------------

# 16. Complete Example

``` java
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

FieldControl.rules(PF_Admin.class)
        .hidden(
                OperationMode.EDIT,
                UserType.ADMIN,
                PF_Admin.ADMIN_CODE
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
values.put(PF_Admin.ENABLE_SECURITY_PIN, true);

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

    PF_Admin field = decision.getField();

    // Actual automation execution belongs here.
}
```

------------------------------------------------------------------------

# 17. Client Integration Pattern

A typical client integration can be structured as:

``` text
Client Page Object / Test
        |
        | collect field values
        v
FieldControl.request(...)
        |
        v
FieldControl.evaluate(...)
        |
        v
FieldExecutionPlan
        |
        +---- allowed fields
        |
        +---- skipped fields + reasons
        |
        v
Smart Core
        |
        v
UI action registry
        |
        v
Selenium / Playwright / other framework
```

This keeps rule evaluation and UI execution independent.

------------------------------------------------------------------------

# 18. Configuration Example for a Parent-Controlled Flow

A typical checkpoint flow can be configured as:

``` java
FieldGroupRegistry.group(
        PF_Job.class,
        PF_Job.WANT_TO_ADD_CHECKPOINT
).controls(
        PF_Job.CHECKPOINT_POI,
        PF_Job.CHECKPOINT_NAME,
        PF_Job.CHECKPOINT_STATE,
        PF_Job.FORM,
        PF_Job.LOCATION
);
```

Start checkpoint:

``` java
FieldGroupRegistry.group(
        PF_Job.class,
        PF_Job.WANT_TO_ADD_START_CHECKPOINT
).controls(
        PF_Job.START_CHECKPOINT_POI,
        PF_Job.ADD_CHECKPOINT_START_LOCATION,
        PF_Job.ADD_CHECKPOINT_START_LOCATION_RESULT,
        PF_Job.ADD_CHECKPOINT_START_LOCATION_NAME,
        PF_Job.START_CHECKPOINT_LOCATION
);
```

End checkpoint:

``` java
FieldGroupRegistry.group(
        PF_Job.class,
        PF_Job.WANT_TO_ADD_END_CHECKPOINT
).controls(
        PF_Job.END_CHECKPOINT_POI,
        PF_Job.ADD_CHECKPOINT_END_LOCATION,
        PF_Job.ADD_CHECKPOINT_END_LOCATION_RESULT,
        PF_Job.ADD_CHECKPOINT_END_LOCATION_NAME,
        PF_Job.END_CHECKPOINT_LOCATION
);
```

Shared fields can be controlled by multiple controllers:

``` java
FieldGroupRegistry.group(
        PF_Job.class,
        PF_Job.WANT_TO_ADD_CHECKPOINT,
        PF_Job.WANT_TO_ADD_START_CHECKPOINT,
        PF_Job.WANT_TO_ADD_END_CHECKPOINT
).controls(
        PF_Job.SEARCH_LOCATION
);
```

The consuming execution layer remains responsible for deciding how the
UI action for each allowed field is performed.

------------------------------------------------------------------------

# 19. Thread Safety and State

The public request and execution-plan objects are designed as immutable
evaluation data.

`FieldControlRequest` copies supplied field values rather than exposing
the caller's mutable map directly.

`FieldExecutionPlan` exposes decisions as an immutable result.

Configuration registries are maintained separately from individual
evaluation requests.

For framework/test lifecycle cleanup:

``` java
FieldControl.clear();
```

This clears configured rules, definitions, conditions, and page
metadata.

------------------------------------------------------------------------

# 20. Cleanup

For framework/test lifecycle cleanup:

``` java
FieldControl.clear();
```

Use cleanup at the appropriate application/test lifecycle boundary so
configurations from one execution do not unintentionally affect another
execution.

------------------------------------------------------------------------

# 21. What Smart Field Control Does NOT Do

SFC intentionally does not contain automation implementation logic.

It does not:

``` text
click()
type()
select()
findElement()
wait()
scroll()
switchWindow()
handleAlert()
create WebDriver
manage browser sessions
execute Runnable
```

Those operations belong to Smart Core or the consuming automation
framework.

------------------------------------------------------------------------

# 22. Architecture Summary

``` text
                    SMART FIELD CONTROL
                    ===================

                         INPUT
                           |
                           v
                 FieldExecutionRequest
                           |
                           v
                  +-------------------+
                  | Field Definitions |
                  +-------------------+
                           |
                           v
                  +-------------------+
                  | Permissions /     |
                  | Field Behavior    |
                  +-------------------+
                           |
                           v
                  +-------------------+
                  | Execution Mode    |
                  +-------------------+
                           |
                           v
                  +-------------------+
                  | Parent Groups     |
                  +-------------------+
                           |
                           v
                  +-------------------+
                  | Conditions        |
                  +-------------------+
                           |
                           v
                  FieldExecutionPlan
                           |
             +-------------+-------------+
             |                           |
             v                           v
       Allowed Fields            Skipped Decisions
                                      |
                                      +-- reason/status
             |
             v
        SMART CORE
             |
             v
     UI Action Execution
             |
             v
 Selenium / Playwright / etc.
```

------------------------------------------------------------------------

# 23. Final Principle

> **Smart Field Control decides WHAT is allowed.**
>
> **Smart Core decides HOW it is executed.**

This separation keeps Smart Field Control:

-   Framework-independent
-   UI-independent
-   Reusable
-   Test-framework-independent
-   Explainable
-   Easier to maintain
-   Suitable for multiple automation technologies

SFC is the **decision engine**.

Smart Core is the **execution engine**.