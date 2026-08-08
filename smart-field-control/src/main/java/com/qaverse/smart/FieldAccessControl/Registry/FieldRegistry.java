package com.qaverse.smart.FieldAccessControl.Registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.qaverse.smart.FieldAccessControl.Configuration.FieldBehavior;
import com.qaverse.smart.FieldAccessControl.Model.FieldContext;

public final class FieldRegistry {

    private FieldRegistry() {
    }

    /**
     * Page + Operation Mode + User + Field -> Behavior
     */
    private static final Map<
            FieldContext<?>,
            FieldBehavior
            > RULES = new ConcurrentHashMap<>();

    public static <F extends Enum<F>> void register(
            FieldContext<F> context,
            FieldBehavior behavior) {

        RULES.put(context, behavior);
    }

    public static <F extends Enum<F>> FieldBehavior getBehavior(
            FieldContext<F> context) {

        return RULES.get(context);
    }

    public static boolean isRegistered(
            FieldContext<?> context) {

        return RULES.containsKey(context);
    }

    public static void clear() {
        RULES.clear();
    }
}