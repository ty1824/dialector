package dev.dialector.processor

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import kotlin.reflect.KClass

sealed class Result<out S, out F>
data class Success<S>(val value: S) : Result<S, Nothing>()
data class Failure<F>(val reason: F) : Result<Nothing, F>()

fun <S> Result<S, Any>.assumeSuccess(): S = when (this) {
    is Success -> value
    is Failure -> throw RuntimeException(reason.toString())
}

fun KSAnnotated.findAnnotations(annotationType: KClass<out Annotation>): Sequence<KSAnnotation> =
    this.annotations.filter {
        it.shortName.asString() == annotationType.simpleName &&
            it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationType.qualifiedName
    }

fun KSAnnotated.hasAnnotation(annotationType: KClass<out Annotation>): Boolean = this.findAnnotations(annotationType).any()

fun KSClassDeclaration.isSubclassOf(superclass: KClass<out Any>): Boolean {
    return this.getAllSuperTypes().any { it.declaration.qualifiedName?.asString() == superclass.qualifiedName }
}

fun KSType.isAssignableTo(type: KSType): Boolean = type.isAssignableFrom(this)
