package dev.dialector.util

internal typealias Cache<K, V> = MutableMap<K, V>

public class LeastRecentlyAddedCache<K, V>(private val capacity: Int?) : LinkedHashMap<K, V>() {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean = capacity == null || this.size > capacity
}

/**
 * Creates a simple map-based Cache with the given maximum number of entries, dropping the oldest entry when adding
 * past capacity.
 *
 * @param maxSize The maximum number of entries. If null, the cache size will not be capped.
 */
public fun <K, V> lraCache(capacity: Int? = null): Cache<K, V> = LeastRecentlyAddedCache(capacity)