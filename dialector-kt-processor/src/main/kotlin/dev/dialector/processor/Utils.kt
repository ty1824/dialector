package dev.dialector.processor

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import dev.dialector.syntax.Node
import kotlin.reflect.KClass

sealed class Result<out S, out F>

data class Success<S>(
    val value: S,
) : Result<S, Nothing>()

data class Failure<F>(
    val reason: F,
) : Result<Nothing, F>()

fun <A, B, F> Result<A, F>.flatMap(mapFn: (A) -> Result<B, F>): Result<B, F> =
    when (this) {
        is Success -> mapFn(value)
        is Failure -> this
    }

fun KSClassDeclaration.isSubclassOf(superclass: KClass<out Any>): Boolean =
    this.getAllSuperTypes().any {
        it.declaration.qualifiedName?.asString() == superclass.qualifiedName
    }

fun KSType.isAssignableTo(type: KSType): Boolean = type.isAssignableFrom(this)
