package dev.dialector.processor

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.Variance
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
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

internal fun KSDeclaration.getLocalQualifiedName(): List<String> =
    if (this.parentDeclaration != null) {
        this.parentDeclaration!!.getLocalQualifiedName() + this.simpleName.asString()
    } else {
        listOf(this.simpleName.asString())
    }

internal fun KSClassDeclaration.asClassName(): ClassName =
    ClassName(this.packageName.asString(), this.getLocalQualifiedName())

internal fun KSType.asTypeName(): TypeName {
    return if (this.declaration is KSClassDeclaration) {
        val declarationName = (this.declaration as KSClassDeclaration).asClassName()
        val candidate = if (this.arguments.isNotEmpty()) {
            declarationName.parameterizedBy(*this.arguments.map { it.type!!.resolve().asTypeName() }.toTypedArray())
        } else {
            declarationName
        }

        if (this.isMarkedNullable) candidate.copy(nullable = true) else candidate
    } else if (this.declaration is KSTypeParameter) {
        val declarationName: TypeVariableName = (this.declaration as KSTypeParameter).asTypeVariableName()
        if (this.isMarkedNullable) declarationName.copy(nullable = true) else declarationName
    } else {
        throw RuntimeException("Failed to create TypeName for $this")
    }
}

internal fun KSTypeParameter.asTypeVariableName(): TypeVariableName {
    return TypeVariableName.invoke(
        name = name.asString(),
        bounds = this.bounds.map { it.resolve().asTypeName() }.toList().toTypedArray(),
        variance = when (variance) {
            Variance.INVARIANT, Variance.STAR -> null
            Variance.CONTRAVARIANT -> KModifier.IN
            Variance.COVARIANT -> KModifier.OUT
            else -> null
        }
    )
}
