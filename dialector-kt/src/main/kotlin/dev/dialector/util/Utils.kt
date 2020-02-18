package dev.dialector.util

typealias Cache<K, V> = MutableMap<K, V>

class LruCache<K, V>(val maxSize: Int?) : LinkedHashMap<K, V>() {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean = maxSize == null || this.size > maxSize
}

/**
 * Creates a simple map-based Cache with the given maximum number of entries.
 *
 * @param maxSize The maximum number of entries. If null, the cache size will not be capped.
 */
fun <K, V> lruCache(maxSize: Int? = null): Cache<K, V> = LruCache(maxSize)