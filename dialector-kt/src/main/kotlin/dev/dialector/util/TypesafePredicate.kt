package dev.dialector.util

import kotlin.reflect.KClass
import kotlin.reflect.safeCast

/**
 * Represents a type-safe constraint that can be used as a basis for conditional rules.
 */
public interface TypesafePredicate<T : Any, in C> {
    public val clauseClass: KClass<out T>
    public fun predicate(candidate: T, context: C): Boolean

    /**
     * Evaluates the given candidate against this clause with the given context.
     *
     * True if the candidate is an instance of the clause's type and matches the predicate. False otherwise.
     */
    public operator fun invoke(candidate: Any, context: C): Boolean =
        clauseClass.safeCast(candidate)?.let { predicate(it, context) } ?: false
}

/**
 * Runs the given function if the candidate is valid for this clause.
 */
public fun <T : Any, C, R> TypesafePredicate<T, C>.runIfValid(candidate: Any, context: C, runIfValid: (T) -> R): R? =
    if (this(candidate, context)) {
        @Suppress("UNCHECKED_CAST")
        runIfValid(candidate as T)
    } else {
        null
    }

/**
 * A specialized [TypesafePredicate] that matches against a specific instance using standard equality.
 */
public abstract class InstancePredicate<T : Any>(public val instance: T) : TypesafePredicate<T, Any> {
    override val clauseClass: KClass<out T> = instance::class
    override fun predicate(candidate: T, context: Any): Boolean = instance == candidate
}

/**
 * A specialized [TypesafePredicate] that matches against instances of a class (or its subclasses).
 */
public abstract class ClassifierPredicate<T : Any>(override val clauseClass: KClass<T>) : TypesafePredicate<T, Any> {
    override fun predicate(candidate: T, context: Any): Boolean = true
}

/**
 * A generalized [TypesafePredicate] that matches against a predicate function.
 */
public abstract class LogicalPredicate<T : Any, in C>(
    override val clauseClass: KClass<T>,
    private val predicate: C.(T) -> Boolean,
) : TypesafePredicate<T, C> {
    override fun predicate(candidate: T, context: C): Boolean = context.predicate(candidate)
}
