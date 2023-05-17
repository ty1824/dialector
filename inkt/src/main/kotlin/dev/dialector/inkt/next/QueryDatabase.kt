package dev.dialector.inkt.next

/**
 * A context that allows modifying entries in a [QueryDatabase].
 */
public interface DatabaseContext : QueryContext {
    /**
     * Assigns an explicit value that will be returned when running this query with the specified key.
     */
    public fun <K : Any, V> set(definition: QueryDefinition<K, V>, key: K, value: V)

    /**
     * Assigns an explicit value that will be returned when running this no-arg query.
     */
    public fun <V> set(definition: QueryDefinition<Unit, V>, value: V): Unit = set(definition, Unit, value)

    /**
     * Clears the query's value for the specified key. If it was explicitly assigned, it will revert back to
     * its original behavior.
     */
    public fun <K : Any, V> remove(definition: QueryDefinition<K, V>, key: K)

    /**
     * Clears the query's value for the specified no-arg query. If it was explicitly assigned, it will revert back to
     * its original behavior.
     */
    public fun <V> remove(definition: QueryDefinition<Unit, V>): Unit = remove(definition, Unit)
}

/**
 * An object that can store input data, evaluate queries, and memoize their results.
 */
public interface QueryDatabase {
    /**
     * Creates a read-only transaction against this database. A transaction may not be multithreaded.
     */
    public fun <T> readTransaction(body: QueryContext.() -> T): T

    /**
     * Creates a read/write transaction against this database. A transaction may not be multithreaded.
     */
    public fun <T> writeTransaction(body: DatabaseContext.() -> T): T
}

/**
 * Convenience function to execute a single query in a read transaction.
 */
public fun <K : Any, V> QueryDatabase.query(definition: QueryDefinition<K, V>, key: K): V =
    readTransaction { query(definition, key) }

/**
 * Convenience function to set a single query value in a write transaction.
 */
public fun <K : Any, V> QueryDatabase.set(definition: QueryDefinition<K, V>, key: K, value: V) {
    writeTransaction { set(definition, key, value) }
}

/**
 * Convenience function to remove a single query value in a write transaction.
 */
public fun <K : Any, V> QueryDatabase.remove(definition: QueryDefinition<K, V>, key: K) {
    writeTransaction { remove(definition, key) }
}
