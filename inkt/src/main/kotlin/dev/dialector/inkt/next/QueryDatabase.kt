package dev.dialector.inkt.next

public interface DatabaseContext : QueryContext {
    /**
     * Assigns an explicit value that will be returned when running this query with the specified key.
     */
    public operator fun <K, V> QueryDefinition<K, V>.set(key: K, value: V)

    /**
     * Clears the query's value for the specified key. If it was explicitly assigned, it will revert back to
     * its original behavior.
     */
    public fun <K, V> QueryDefinition<K, V>.remove(key: K)
}

/**
 * An object that can store input data, evaluate queries, and memoize their results.
 */
public interface QueryDatabase {
    /**
     * Execute commands (set input, invoke queries) on this database.
     */
    public fun <T> run(body: DatabaseContext.() -> T): T
}
