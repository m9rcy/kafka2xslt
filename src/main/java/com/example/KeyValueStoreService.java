package com.example;

import java.util.Optional;

/**
 * Interface for searching key-value stores with configurable store names
 */
public interface KeyValueStoreService<K, V> {
    /**
     * Search for a value by key in the specified store
     * @param storeName The name of the store to query
     * @param key The key to search for
     * @return Optional containing the value if found
     */
    Optional<V> findByKey(String storeName, K key);

    /**
     * Check if the service is enabled
     * @return true if the service is operational
     */
    boolean isEnabled();

    /**
     * Check if a specific store is available
     * @param storeName The name of the store to check
     * @return true if the store exists and is queryable
     */
    default boolean isStoreAvailable(String storeName) {
        return isEnabled();
    }
}