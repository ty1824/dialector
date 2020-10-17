package dev.dialector.model

import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal class NodeModel<T : Node>(val nodeClass: KClass<T>) {
    val properties: List<KProperty<*>> = nodeClass.memberProperties.filter { it.findAnnotation<Property>() != null }
    val children: List<KProperty<*>> = nodeClass.memberProperties.filter { it.findAnnotation<Child>() != null }
    val references: List<KProperty<*>> = nodeClass.memberProperties.filter { it.findAnnotation<Reference>() != null }
}

class Generator {
    fun <T : Node> generate(nodeClass: KClass<T>): FileSpec {
        val model = NodeModel(nodeClass)
        return FileSpec.builder("dev.dialector.genmodel", "file")
            .addType(TypeSpec.classBuilder(nodeClass.simpleName + "Impl")
                .superclass(nodeClass)
                .addSuperinterface(Node::class)
                .addProperties(model.properties.map { generateProperty(it) })
                // Add children & references
                .build())
            .build()
    }

    fun generateProperty(property: KProperty<*>): PropertySpec {
        return PropertySpec.builder(property.name, property.returnType.asTypeName())
            //.getter(FunSpec.getterBuilder().add)
            .delegate("%T", PropertyValue::class.asClassName().parameterizedBy(property.returnType.asTypeName()))
            .build()
    }
}