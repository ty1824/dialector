package dev.dialector.util

import kotlin.reflect.KClass

interface TypesafeClause<T : Any> {
    val clauseClass: KClass<out T>
    fun constraint(candidate: T): Boolean
}

abstract class InstanceClause<T : Any>(val instance: T) : TypesafeClause<T> {
    override val clauseClass: KClass<out T> = instance::class
    override fun constraint(candidate: T): Boolean = instance == candidate
}

abstract class ClassifierClause<T : Any>(override val clauseClass: KClass<T>) : TypesafeClause<T> {
    override fun constraint(candidate: T): Boolean = true
}

