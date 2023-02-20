package dev.dialector.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass

/**
 * Represents a type-safe constraint that can be used as a basis for conditional rules.
 */
public interface TypesafeClause<T : Any> {
    public val clauseClass: KClass<out T>
    public fun constraint(candidate: T): Boolean
}

@ExperimentalContracts
public inline fun <T : Any, reified V : T> TypesafeClause<V>.evaluate(candidate: T): Boolean {
    contract {
        returns(true) implies (candidate is V)
    }
    return clauseClass.isInstance(candidate) && constraint(candidate as V)
}

/**
 * A specialized [TypesafeClause] that matches against a specific instance.
 */
public abstract class InstanceClause<T : Any>(public val instance: T) : TypesafeClause<T> {
    override val clauseClass: KClass<out T> = instance::class
    override fun constraint(candidate: T): Boolean = instance == candidate
}

/**
 * A specialized [TypesafeClause] that matches against instances of a class (or its subclasses).
 */
public abstract class ClassifierClause<T : Any>(override val clauseClass: KClass<T>) : TypesafeClause<T> {
    override fun constraint(candidate: T): Boolean = true
}
