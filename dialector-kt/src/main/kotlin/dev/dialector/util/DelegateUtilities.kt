package dev.dialector.util

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A utility that allows creating a delegate value that uses the property name upon creation.
 */
public fun <T, V : Any> withPropertyName(initializer: (String) -> V): ReadOnlyProperty<T, V> =
    NamedInitializerDelegate(initializer)

/**
 * Wraps a value in a delegate. Primarily useful when writing code that may generate different delegates and a
 * particular state would otherwise not require a delegate.
 */
public fun <T, V : Any> wrapperDelegate(value: V): ReadOnlyProperty<T, V> = WrapperDelegate(value)

internal class WrapperDelegate<T, V : Any>(val value: V) : ReadOnlyProperty<T, V> {
    override fun getValue(thisRef: T, property: KProperty<*>): V = value
}

internal class NamedInitializerDelegate<T, V : Any>(initializer: (String) -> V) : ReadOnlyProperty<T, V> {
    private var initializer: ((String) -> V)? = initializer

    @Volatile private var _value: V? = null
    override fun getValue(thisRef: T, property: KProperty<*>): V {
        val v1 = _value
        if (v1 != null) {
            return v1
        }

        return synchronized(this) {
            val v2 = _value
            if (v2 != null) {
                v2
            } else {
                val typedValue = initializer!!(property.name)
                _value = typedValue
                initializer = null
                typedValue
            }
        }
    }
}
