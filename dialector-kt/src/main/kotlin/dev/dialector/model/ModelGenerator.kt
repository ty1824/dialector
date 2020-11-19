package dev.dialector.model

import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.*

typealias Mapping<K, V> = (K) -> V

/*
TODO:
- Implement Class properties (DONE)
- Implement builder (DONE)
- Generate Node implementation
- Handle inheritance
 */

/*
Notes:
Look into using KAPT/creating a compiler plugin
Incremental compilation??
 */

sealed class Result<out S, out F>
data class Success<S>(val value: S) : Result<S, Nothing>()
data class Failure<F>(val reason: F) : Result<Nothing, F>()

fun <S> Result<S, Any>.assumeSuccess(): S = when (this) {
    is Success -> value
    is Failure -> throw RuntimeException(reason.toString())
}

internal class NodeModel<T : Node> private constructor(val nodeClass: KClass<T>) {
    companion object {
        fun <T : Node> create(nodeClass: KClass<T>): Result<NodeModel<T>, String> {
            val model = NodeModel(nodeClass)
            val problems = model.validate()
            if (problems.isEmpty())
                return Success(model)
            else
                return Failure("Errors found for $nodeClass:\n\t" +
                    problems.joinToString("\n\t"))
        }
    }

    @Suppress("UNCHECKED_CAST")
    val inheritedNodes: List<KClass<Node>> = nodeClass.supertypes
        .asSequence()
        .mapNotNull { it.classifier as? KClass<*> }
        .filter {
            it.isSubclassOf(Node::class) && it.hasAnnotation<NodeDefinition>()
        }.toList() as List<KClass<Node>>

    val properties: List<KProperty<*>> = nodeClass.memberProperties.filter { it.hasAnnotation<Property>() }
    val children: List<KProperty<*>> = nodeClass.memberProperties.filter { it.hasAnnotation<Child>() }
    val references: List<KProperty<*>> = nodeClass.memberProperties.filter { it.hasAnnotation<Reference>() }
}

internal fun <T : Node> NodeModel<T>.validate(): List<String> {
    val errors: MutableList<String> = mutableListOf()

    if (!nodeClass.isSubclassOf(Node::class))
        errors += "Input classes must have Node as a superinterface"

    properties.forEach {
        // A property may be any type besides a Node
        if (it.returnType.isSubtypeOf(nullableNodeType))
            errors += "'${it.name}' Property must not be of type Node"
    }

    children.forEach {
        // A child must either be of type T? or List<T> where T is a subclass of Node
        if (!(it.returnType.isSubtypeOf(nullableNodeType) || it.returnType.isSubtypeOf(nodeListType)))
            errors += "'${it.name}': Child must be of type Node or List<Node>"
    }

    references.forEach {
        // A reference must be of type NodeReference<T> where T is a subclass of Node.
        if (!it.returnType.isSubtypeOf(nodeReferenceType)) {
            errors += "'${it.name}' Reference must be of type NodeReference"
        }
    }

    return errors.toList()
}

private val nodeType = Node::class.createType()
private val nullableNodeType = Node::class.createType(nullable = true)
private val nodeListType = List::class.createType(listOf(KTypeProjection(KVariance.OUT, nodeType)))
private val nodeReferenceType = NodeReference::class.createType(listOf(KTypeProjection(KVariance.OUT, nodeType)), true)

object Generator {
    fun generate(classes: List<KClass<out Node>>) =
        GenerationModel.create(classes).assumeSuccess().generate()
}

internal class GenerationModel(
    val genPackage: String,
    val nodeModels: Map<KClass<out Node>, NodeModel<out Node>>
){
    companion object {
        fun create(classes: List<KClass<out Node>>): Result<GenerationModel, String> {
            val errors: MutableList<String> = mutableListOf()
            val nodeModels: MutableMap<KClass<out Node>, NodeModel<out Node>> = mutableMapOf()
            classes.forEach {
                val result = NodeModel.create(it)
                when (result) {
                    is Success -> nodeModels[it] = result.value
                    is Failure -> errors += result.reason
                }
            }

            if (errors.isNotEmpty()) {
                return Failure("Failed to generate classes, ${errors.size} errors found:\n" +
                    errors.joinToString("\n"))
            } else {
                return Success(GenerationModel("dev.dialector.genmodel", nodeModels.toMap()))
            }
        }
    }

    fun generate() : FileSpec {
        val builder = FileSpec.builder(genPackage, "file")
        for (model in nodeModels.values) {
            handleClass(model, builder)
        }
        return builder.build()
    }

    internal fun handleClass(model: NodeModel<out Node>, builder: FileSpec.Builder) {
        builder.addType(generateImpl(model))
        builder.addType(generateBuilder(model))

        // Create extension functions for the base class, too.
    }

    /*
override val parent: Node?

override val properties: Map<KProperty<*>, Any?>

override val children: Map<KProperty<*>, List<Node>>

override val references: Map<KProperty<*>, List<NodeReference<*>>>

     */

    internal fun generateImpl(model: NodeModel<out Node>): TypeSpec =
        TypeSpec.classBuilder(model.nodeClass.simpleName + "Impl")
            .addModifiers(KModifier.PRIVATE)
            .addSuperinterface(model.nodeClass)
            .addSuperinterface(Node::class)
            .primaryConstructor(generateConstructor(model))
            // Add properties
            .addProperties(model.properties.map { generateProperty(it) })
            // Add children
            .addProperties(model.children.map { generateChild(it) })
            // Add references
            .addProperties(model.references.map { generateReference(it)} )
            // Implement Node
            .apply { generateNodeImplementation(model) }
            .build()

    internal fun TypeSpec.Builder.generateNodeImplementation(model: NodeModel<out Node>) {
        // parent
        this.addProperty(Node::parent.let {
            PropertySpec.builder(it.name, it.returnType.asTypeName())
                .addModifiers(KModifier.OVERRIDE)
                .initializer("null")
                .mutable(true)
                .build()
        })

        // properties map
        this.addProperty(
            PropertySpec.builder(Node::properties.name, Node::properties.returnType.asTypeName())
                .addModifiers(KModifier.OVERRIDE)
                .getter(FunSpec.getterBuilder()
                    .addCode(
                        CodeBlock.builder()
                            .add("return mapOf(")
                            .indent()
                            .apply {
                                model.properties.forEach { property ->
                                    add("\"${property.name}\" to ${property.name}")
                                }
                            }
                            .unindent()
                            .add(")")
                            .build()
                    )
                    .build()
                )
                .build()
        )

        // children map
        this.addProperty(
            PropertySpec.builder(Node::children.name, Node::children.returnType.asTypeName())
                .addModifiers(KModifier.OVERRIDE)
                .getter(FunSpec.getterBuilder()
                    .addCode(
                        CodeBlock.builder()
                            .add("return mapOf(")
                            .indent()
                            .apply {
                                model.children.forEach { property ->
                                    if (property.returnType.isSubtypeOf(nullableNodeType))
                                        add("\"${property.name}\" to listOf(${property.name})")
                                    else if (property.returnType.isSubtypeOf(nodeListType))
                                        add("\"${property.name}\" to (${property.name})")
                                    else
                                        throw RuntimeException("Unexpected child type found: $property : ${property.returnType}")
                                }
                            }
                            .unindent()
                            .add(")")
                            .build()
                    )
                    .build()
                )
                .build()
        )

        // references map
        this.addProperty(
            PropertySpec.builder(Node::references.name, Node::references.returnType.asTypeName())
                .addModifiers(KModifier.OVERRIDE)
                .getter(FunSpec.getterBuilder()
                    .addCode(
                        CodeBlock.builder()
                            .add("return mapOf(")
                            .indent()
                            .apply {
                                model.references.forEach { property ->
                                    add("\"${property.name}\" to ${property.name}")
                                }
                            }
                            .unindent()
                            .add(")")
                            .build()
                    )
                    .build()
                )
                .build()
        )
    }

    internal fun generateConstructor(model: NodeModel<out Node>): FunSpec =
        FunSpec.constructorBuilder()
            .addParameter(ParameterSpec(
                "init",
                ClassName(genPackage, "${model.nodeClass.simpleName}Initializer")
            ))
            .build()

    internal fun generateProperty(property: KProperty<*>): PropertySpec =
        PropertySpec.builder(property.name, property.returnType.asTypeName())
            .mutable(true)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("init.${property.name}${if (!property.returnType.isMarkedNullable) "!!" else ""}")
            //.delegate("%T", PropertyValue::class.asClassName().parameterizedBy(property.returnType.asTypeName()))
            .build()

    internal fun generateChild(child: KProperty<*>): PropertySpec {
        if (child.returnType.isSubtypeOf(nullableNodeType)) {
            return PropertySpec.builder(child.name, child.returnType.asTypeName())
                .mutable(true)
                .addModifiers(KModifier.OVERRIDE)
                .initializer("init.${child.name}${if (!child.returnType.isMarkedNullable) "!!" else ""}")
                .build()
        } else if (child.returnType.isSubtypeOf(nodeListType)) {
            return PropertySpec.builder(child.name, child.returnType.asTypeName())
                .addModifiers(KModifier.OVERRIDE)
                .initializer("init.${child.name}.toMutableList()")
                .build()
        } else {
            throw RuntimeException("Unexpected child type found: $child : ${child.returnType}")
        }
    }

    internal fun generateReference(reference: KProperty<*>): PropertySpec =
        PropertySpec.builder(reference.name, reference.returnType.asTypeName())
            .mutable(true)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("init.${reference.name}${if (!reference.returnType.isMarkedNullable) "!!" else ""}")
            .build()

//    internal fun generateNodeDef(model: NodeModel<out Node>): TypeSpec {
//
//    }

    /**
     * Creates the [Node] builder for the given [NodeModel]
     */
    internal fun generateBuilder(model: NodeModel<out Node>): TypeSpec {
        val builder = TypeSpec.classBuilder("${model.nodeClass.simpleName}Initializer")

        builder.addProperties(model.properties.map {
            PropertySpec.builder(
                it.name,
                it.returnType.withNullability(true).asTypeName()
            ).mutable(true).initializer("null").build()
        })

        builder.addProperties(model.children.map {
            if (it.returnType.isSubtypeOf(nullableNodeType))
                PropertySpec.builder(
                    it.name,
                    it.returnType.withNullability(true).asTypeName()
                ).mutable(true).initializer("null").build()
            else if (it.returnType.isSubtypeOf(nodeListType))
                // If we're dealing with a list of children, create a MutableList
                PropertySpec.builder(
                    it.name,
                    MutableList::class.createType(it.returnType.arguments).asTypeName()
                ).mutable(true).initializer("mutableListOf()").build()
            else throw RuntimeException("Unexpected child type found: $it : ${it.returnType}")
        })

        builder.addProperties(model.references.map {
            PropertySpec.builder(
                it.name,
                it.returnType.withNullability(true).asTypeName()
            ).mutable(true).initializer("null").build()
        })

        builder.addFunction(FunSpec.builder("build")
            .returns(model.nodeClass.asTypeName())
            .addStatement("return ${model.nodeClass.simpleName}Impl(this)")
            .build()
        )

        return builder.build()
    }
}