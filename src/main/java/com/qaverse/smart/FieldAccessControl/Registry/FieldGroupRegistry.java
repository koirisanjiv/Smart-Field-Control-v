package com.qaverse.smart.FieldAccessControl.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FieldGroupRegistry {

    /*
     * Page -> Child Field -> Parent Controllers
     *
     * Example:
     *
     * SEARCH_LOCATION
     *      -> WANT_TO_ADD_CHECKPOINT
     *      -> WANT_TO_ADD_START_CHECKPOINT
     *      -> WANT_TO_ADD_END_CHECKPOINT
     *
     * Any controller being TRUE makes the child active.
     */
    private static final Map<Class<?>, Object> GROUPS =
            new ConcurrentHashMap<>();

    private FieldGroupRegistry() {
    }

    /**
     * Creates a field group controlled by one or more controllers.
     *
     * Multiple controllers work as OR.
     *
     * Example:
     *
     * group(
     *     PF_Job.class,
     *     PF_Job.WANT_TO_ADD_CHECKPOINT,
     *     PF_Job.WANT_TO_ADD_START_CHECKPOINT
     * )
     */
    @SafeVarargs
    public static <E extends Enum<E>> GroupBuilder<E> group(
            Class<E> page,
            E... controllers) {

        if (page == null) {
            throw new IllegalArgumentException("Page cannot be null");
        }

        if (controllers == null || controllers.length == 0) {
            throw new IllegalArgumentException(
                    "At least one controller is required");
        }

        List<E> controllerList = new ArrayList<>();

        for (E controller : controllers) {

            if (controller == null) {
                throw new IllegalArgumentException(
                        "Controller cannot be null");
            }

            if (!controllerList.contains(controller)) {
                controllerList.add(controller);
            }
        }

        return new GroupBuilder<>(page, controllerList);
    }

    /**
     * Returns all parent controllers for a child field.
     */
    public static <E extends Enum<E>> List<E> getControllers(
            Class<E> page,
            E field) {

        Map<E, List<E>> groups = getGroupMap(page);

        return groups.getOrDefault(
                field,
                List.of());
    }

    /**
     * Registers child fields against one or more controllers.
     */
    private static <E extends Enum<E>> void register(
            Class<E> page,
            List<E> controllers,
            List<E> children) {

        Map<E, List<E>> groups = getGroupMap(page);

        for (E child : children) {

            if (child == null) {
                continue;
            }

            groups.compute(child, (field, existingControllers) -> {

                List<E> mergedControllers =
                        existingControllers == null
                                ? new ArrayList<>()
                                : new ArrayList<>(existingControllers);

                for (E controller : controllers) {

                    if (!mergedControllers.contains(controller)) {
                        mergedControllers.add(controller);
                    }
                }

                return List.copyOf(mergedControllers);
            });
        }
    }

    /**
     * Returns the group map for a page.
     */
    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> Map<E, List<E>> getGroupMap(
            Class<E> page) {

        return (Map<E, List<E>>) GROUPS.computeIfAbsent(
                page,
                key -> new ConcurrentHashMap<E, List<E>>());
    }

    /**
     * Builder used to define child fields.
     */
    public static final class GroupBuilder<E extends Enum<E>> {

        private final Class<E> page;
        private final List<E> controllers;

        private GroupBuilder(
                Class<E> page,
                List<E> controllers) {

            this.page = page;
            this.controllers = List.copyOf(controllers);
        }

        /**
         * Defines fields controlled by this group.
         */
        @SafeVarargs
        public final void controls(E... fields) {

            if (fields == null || fields.length == 0) {
                return;
            }

            FieldGroupRegistry.register(
                    page,
                    controllers,
                    List.of(fields));
        }
    }
}