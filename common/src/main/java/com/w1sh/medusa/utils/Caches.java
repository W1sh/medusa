package com.w1sh.medusa.utils;

import com.github.benmanes.caffeine.cache.Cache;

import java.util.Collection;
import java.util.Objects;

public final class Caches {

    private Caches() {}

    public static <K, V, C extends Collection<V>> void storeMultivalue(K key, V value, C collection, Cache<K, C> cache) {
        requireNonNullArguments(key, value, collection, cache);

        collection.remove(value);
        collection.add(value);
        cache.put(key, collection);
    }

    private static <K, V, C extends Collection<V>> void requireNonNullArguments(K key, V value, C collection, Cache<K, C> cache){
        Objects.requireNonNull(collection);
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        Objects.requireNonNull(cache);
    }
}
