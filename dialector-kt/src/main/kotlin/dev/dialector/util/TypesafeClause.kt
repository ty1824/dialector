package dev.dialector.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass

interface TypesafeClause<T : Any> {
    val clauseClass: KClass<out T>
    fun constraint(candidate: T): Boolean

}

@ExperimentalContracts
inline fun <T : Any, reified V : T> TypesafeClause<V>.evaluate(candidate: T): Boolean {
    contract {
        returns(true) implies (candidate is V)
    }

    return clauseClass.isInstance(candidate) && constraint(candidate as V)
}

abstract class InstanceClause<T : Any>(val instance: T) : TypesafeClause<T> {
    override val clauseClass: KClass<out T> = instance::class
    override fun constraint(candidate: T): Boolean = instance == candidate
}

abstract class ClassifierClause<T : Any>(override val clauseClass: KClass<T>) : TypesafeClause<T> {
    override fun constraint(candidate: T): Boolean = true
}

