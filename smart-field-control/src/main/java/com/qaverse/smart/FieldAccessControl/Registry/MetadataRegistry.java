package com.qaverse.smart.FieldAccessControl.Registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.qaverse.smart.FieldAccessControl.Metadata.PageMetadata;

public final class MetadataRegistry {

    private MetadataRegistry() {
    }

    private static final Map<
            Class<? extends Enum<?>>,
            PageMetadata<?>
            > PAGE_METADATA = new ConcurrentHashMap<>();

    public static <F extends Enum<F>> void register(
            Class<F> page,
            PageMetadata<F> metadata) {

        PAGE_METADATA.put(
                page,
                metadata
        );
    }

    @SuppressWarnings("unchecked")
    public static <F extends Enum<F>> PageMetadata<F> get(
            Class<F> page) {

        return (PageMetadata<F>)
                PAGE_METADATA.get(page);
    }

    public static boolean contains(
            Class<? extends Enum<?>> page) {

        return PAGE_METADATA.containsKey(page);
    }

    public static void clear() {
        PAGE_METADATA.clear();
    }
}