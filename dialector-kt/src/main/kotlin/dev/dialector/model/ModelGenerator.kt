package dev.dialector.model

import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

internal class NodeModel<T : Node>(val nodeClass: KClass<T>) {
    val properties: List<KProperty<*>> = nodeClass.memberProperties.filter { it.findAnnotation<Property>() != null }
    val children: List<KProperty<*>> = nodeClass.memberProperties.filter { it.findAnnotation<Child>() != null }
    val references: List<KProperty<*>> = nodeClass.memberProperties.filter { it.findAnnotation<Reference>() != null }
}

private val nodeType = Node::class.createType()
private val nullableNodeType = Node::class.createType(nullable = true)
private val nodeListType = List::class.createType(listOf(KTypeProjection(KVariance.OUT, nodeType)))
private val nodeReferenceType = NodeReference::class.createType(listOf(KTypeProjection(KVariance.OUT, nodeType)), true)

class Generator {
    fun generateFromClasses(classes: List<KClass<out Node>>) : FileSpec {
        val builder = FileSpec.builder("dev.dialector.genmodel", "file")
        for (kclass in classes) {
            builder.addType(generateClass(kclass))
            // Create extension functions for the base class, too.
        }
        return builder.build()
    }

    fun generateClass(nodeClass: KClass<out Node>): TypeSpec {
        val model = NodeModel(nodeClass)

        return TypeSpec.classBuilder(nodeClass.simpleName + "Impl")
            .superclass(nodeClass)
            .addSuperinterface(Node::class)
            // Add properties
            .addProperties(model.properties.map { generateProperty(it) })
            // Add children
            .addProperties(model.children.map { generateChild(it) })
            // Add references
            .addProperties(model.references.map { generateReference(it)} )
            .build()
    }

    fun generateProperty(property: KProperty<*>): PropertySpec {
        // A property may be any type besides a Node
        if (property.returnType.isSubtypeOf(nullableNodeType)) {
            throw RuntimeException("Property: $property must not be of type Node")
        } else {
            return PropertySpec.builder(property.name, property.returnType.asTypeName())
                .mutable(true)
                //.delegate("%T", PropertyValue::class.asClassName().parameterizedBy(property.returnType.asTypeName()))
                .build()
        }
    }

    fun generateChild(child: KProperty<*>): PropertySpec {
        // A child must either be of type T? or List<T> where T is a subclass of Node
        if (child.returnType.isSubtypeOf(nullableNodeType)) {
            return PropertySpec.builder(child.name, child.returnType.asTypeName())
                .mutable(true)
                .build()
        } else if (child.returnType.isSubtypeOf(nodeListType)) {
            return PropertySpec.builder(child.name, child.returnType.asTypeName())
                .build()
        } else {
            throw RuntimeException("Child: $child must be of type Node or List<Node>")
        }
    }

    fun generateReference(reference: KProperty<*>): PropertySpec {
        // A reference must be of type NodeReference<T> where T is a subclass of Node.
        if (!reference.returnType.isSubtypeOf(nodeReferenceType)) {
            throw RuntimeException("Reference: $reference must be of type NodeReference")
        } else {
            return PropertySpec.builder(reference.name, reference.returnType.asTypeName())
                .mutable(true)
                .build()
        }
    }
}