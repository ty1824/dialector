package dev.dialector.processor

import com.google.auto.service.AutoService
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Variance
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import dev.dialector.syntax.Child
import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeDefinition
import dev.dialector.syntax.NodeReference
import dev.dialector.syntax.Property
import dev.dialector.syntax.Reference
import kotlin.reflect.KClass


@AutoService(SymbolProcessorProvider::class)
class DialectorSymbolProcessorProvider: SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        DialectorSymbolProcessor(environment.codeGenerator, environment.options["dev.dialector.targetPackage"]!!)
}

class DialectorSymbolProcessor(val codeGenerator: CodeGenerator, val targetPackage: String) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(NodeDefinition::class.qualifiedName!!)
        val nodeDefinitions = symbols.filterIsInstance<KSClassDeclaration>()
        Generator(resolver).generate(targetPackage, nodeDefinitions).forEach { (fileSpec, ksClasses) ->
            val file = codeGenerator.createNewFile(Dependencies(true, *(ksClasses.mapNotNull { it.containingFile }.distinct().toTypedArray())), fileSpec.packageName, fileSpec.name)
            file.bufferedWriter().use { stream ->
                fileSpec.writeTo(stream)
            }
        }
        return listOf()
    }
}

/*
TODO:
- Handle inheritance
- Mutability through change objects
- No builder if no properties
 */

class Generator(private val resolver: Resolver) {
    private fun KClass<out Any>.getClassDeclaration(): KSClassDeclaration? =
        resolver.getClassDeclarationByName(resolver.getKSNameFromString(this.qualifiedName!!))

    private val nodeClass = Node::class.getClassDeclaration()!!
    private val nodeType = nodeClass.asType(listOf())
    private val nullableNodeType = nodeClass.asType(listOf()).makeNullable()
    private val nodeListType = List::class.getClassDeclaration()!!.asType(listOf(
        resolver.getTypeArgument(resolver.createKSTypeReferenceFromKSType(nodeType), Variance.COVARIANT)
    ))
    private val nodeReferenceType = NodeReference::class.getClassDeclaration()!!.asType(listOf(
        resolver.getTypeArgument(resolver.createKSTypeReferenceFromKSType(nodeType), Variance.COVARIANT)
    )).makeNullable()

    fun generate(targetPackage: String, classes: Sequence<KSClassDeclaration>): List<Pair<FileSpec, List<KSClassDeclaration>>> = createGenerationModel(targetPackage, classes).assumeSuccess().generate()

    private class NodeModel constructor(val nodeClass: KSClassDeclaration) {

        @Suppress("UNCHECKED_CAST")
        val inheritedNodes: List<KSClassDeclaration> = nodeClass.getAllSuperTypes()
            .asSequence()
            .mapNotNull { it.declaration as? KSClassDeclaration }
            .filter {
                it.getAllSuperTypes().any { type -> type.declaration.qualifiedName?.asString() == Node::class.qualifiedName }
                    && it.hasAnnotation(NodeDefinition::class)
            }.toList()

        val properties: List<KSPropertyDeclaration> = nodeClass.getAllProperties().filter { it.hasAnnotation(Property::class) }.toList()
        val children: List<KSPropertyDeclaration> = nodeClass.getAllProperties().filter { it.hasAnnotation(Child::class) }.toList()
        val references: List<KSPropertyDeclaration> = nodeClass.getAllProperties().filter { it.hasAnnotation(Reference::class) }.toList()
    }

    private fun createNodeModel(nodeClass: KSClassDeclaration): Result<NodeModel, String> {
        val model = NodeModel(nodeClass)
        val problems = model.validate()
        return if (problems.isEmpty())
                Success(model)
            else
                Failure("Errors found for $nodeClass:\n\t" +
                    problems.joinToString("\n\t"))
    }

    private class ModelError(val message: String, val location: Location) {
        override fun toString(): String {
            if (location is FileLocation) {
                return "$message (${location.filePath}:${location.lineNumber})"
            } else {
                return "$message ($location)"
            }
        }
    }

    private infix fun String.at(location: Location): ModelError = ModelError(this, location)

    private fun NodeModel.validate(): List<ModelError> {
        val errors: MutableList<ModelError> = mutableListOf()

        if (!nodeClass.isSubclassOf(Node::class))
            errors += "Input class must have Node as a superinterface" at nodeClass.location

        if (nodeClass.modifiers.contains(Modifier.FINAL) || nodeClass.modifiers.contains(Modifier.SEALED)) {
            errors += "Input class must be extensible." at nodeClass.location
        }

        properties.forEach {
            // A property may be any type besides a Node
            if (it.type.resolve().isAssignableTo(nullableNodeType))
                errors += "'${it.qualifiedName}' Property must not be of type Node" at it.location
        }

        children.forEach {
            // A child must either be of type T? or List<T> where T is a subclass of Node
            val resolvedType = it.type.resolve()
            if (!(resolvedType.isAssignableTo(nullableNodeType) || resolvedType.isAssignableTo(nodeListType)))
                errors += "'${it.qualifiedName}': Child must be of type Node or List<Node>" at it.location
        }

        references.forEach {
            // A reference must be of type NodeReference<T> where T is a subclass of Node.
            if (!it.type.resolve().isAssignableTo(nodeReferenceType)) {
                errors += "'${it.qualifiedName}' Reference must be of type NodeReference" at it.location
            }
        }

        return errors.toList()
    }
    
    private fun createGenerationModel(targetPackage: String, classes: Sequence<KSClassDeclaration>): Result<GenerationModel, String> {
        val errors: MutableList<String> = mutableListOf()
        val nodeModels: MutableMap<KSClassDeclaration, NodeModel> = mutableMapOf()
        classes.forEach {
            when (val result = createNodeModel(it)) {
                is Success -> nodeModels[it] = result.value
                is Failure -> errors += result.reason
            }
        }

        if (errors.isNotEmpty()) {
            return Failure("Failed to generate classes, ${errors.size} errors found:\n" +
                errors.joinToString("\n"))
        } else {
            return Success(GenerationModel(targetPackage, nodeModels.toMap()))
        }
    }

    private inner class GenerationModel(
        val genPackage: String,
        val nodeModels: Map<KSClassDeclaration, NodeModel>
    ) {

        fun generate() : List<Pair<FileSpec, List<KSClassDeclaration>>> {
            val files: MutableList<Pair<FileSpec, List<KSClassDeclaration>>> = mutableListOf()
            for (model in nodeModels.values) {
                val builder = FileSpec.builder(genPackage, "${model.nodeClass.simpleName.getShortName()}Model")
                handleClass(model, builder)
                files += builder.build() to model.inheritedNodes + model.nodeClass
            }
            return files.toList()
        }

        fun handleClass(model: NodeModel, builder: FileSpec.Builder) {
            builder.addType(generateImpl(model))
            builder.addType(generateBuilder(model))
            builder.addFunction(generateBuilderDsl(model))

            // TODO: Create extension functions for the base class, too.
        }

        fun generateBuilderDsl(model: NodeModel): FunSpec {
            val initializerClassName = ClassName(
                model.nodeClass.packageName.asString(),
                "${model.nodeClass.simpleName.asString()}Initializer"
            )
            return FunSpec.builder(
                model.nodeClass.simpleName.getShortName().let {
                    val first = it.first()
                    it.replaceFirst(first, first.lowercaseChar())
                })
                .addParameter("init",
                    LambdaTypeName.get(
                        receiver = initializerClassName,
                        returnType = Unit::class.asTypeName()
                    )
                )
                .returns(model.nodeClass.asClassName())
                .addStatement("""return ${initializerClassName.canonicalName}().apply(init).build()""")
                .build()
        }

        fun generateImpl(model: NodeModel): TypeSpec =
            TypeSpec.classBuilder(model.nodeClass.simpleName.getShortName() + "Impl")
                .addModifiers(KModifier.PRIVATE)
                .addSuperinterface(model.nodeClass.asStarProjectedType().asTypeName())
                .addSuperinterface(Node::class.asTypeName())
                .primaryConstructor(generateConstructor(model))
                // Add properties
                .addProperties(model.properties.map { generateProperty(it) })
                // Add children
                .addProperties(model.children.map { generateChild(it) })
                // Add references
                .addProperties(model.references.map { generateReference(it)} )
                // Implement Node
                .apply { this.generateNodeImplementation(model) }
                .build()

        fun TypeSpec.Builder.generateNodeImplementation(model: NodeModel) {
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
                                        add("\"${property.simpleName.asString()}\" to ${property.simpleName.asString()}, ")
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
                                        when {
                                            property.type.resolve().isAssignableTo(nullableNodeType) ->
                                                add("\"${property.simpleName.asString()}\" to listOfNotNull(${property.simpleName.asString()}), ")
                                            property.type.resolve().isAssignableTo(nodeListType) ->
                                                add("\"${property.simpleName.asString()}\" to ${property.simpleName.asString()}, ")
                                            else ->
                                                throw RuntimeException("Unexpected child type found: $property : ${property.type}")
                                        }
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
                                        add("\"${property.simpleName.asString()}\" to ${property.simpleName.asString()}, ")
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

        fun generateConstructor(model: NodeModel): FunSpec =
            FunSpec.constructorBuilder()
                .addParameter(ParameterSpec(
                    "init",
                    ClassName(genPackage, "${model.nodeClass.simpleName.asString()}Initializer")
                ))
                .build()

        fun generateProperty(property: KSPropertyDeclaration): PropertySpec =
            PropertySpec.builder(property.simpleName.asString(), property.type.resolve().asTypeName())
                .mutable(true)
                .addModifiers(KModifier.OVERRIDE)
                .initializer("init.${property.simpleName.asString()}${if (!property.type.resolve().isMarkedNullable) "!!" else ""}")
                //.delegate("%T", PropertyValue::class.dev.dialector.processor.asClassName().parameterizedBy(property.returnType.dev.dialector.processor.asTypeName()))
                .build()

        fun generateChild(child: KSPropertyDeclaration): PropertySpec {
            val resolvedType = child.type.resolve()
            return when {
                resolvedType.isAssignableTo(nullableNodeType) -> {
                    PropertySpec.builder(child.simpleName.asString(), resolvedType.asTypeName())
                        .mutable(true)
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer("init.${child.simpleName.asString()}${if (!resolvedType.isMarkedNullable) "!!" else ""}")
                        .build()
                }
                resolvedType.isAssignableTo(nodeListType) -> {
                    PropertySpec.builder(
                        child.simpleName.asString(),
                        ClassName("kotlin.collections", "MutableList").parameterizedBy(
                            resolvedType.arguments.map { argument ->
                                argument.type!!.resolve().asTypeName()
                            }
                        ))
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer("init.${child.simpleName.asString()}.toMutableList()")
                        .build()
                }
                else -> {
                    throw RuntimeException("Unexpected child type found: $child : $resolvedType")
                }
            }
        }

        internal fun generateReference(reference: KSPropertyDeclaration): PropertySpec =
            PropertySpec.builder(reference.simpleName.asString(), reference.type.resolve().asTypeName())
                .mutable(true)
                .addModifiers(KModifier.OVERRIDE)
                .initializer("init.${reference}${if (!reference.type.resolve().isMarkedNullable) "!!" else ""}")
                .build()

        /**
         * Creates the [Node] builder for the given [NodeModel]
         */
        internal fun generateBuilder(model: NodeModel): TypeSpec {
            val builder = TypeSpec.classBuilder("${model.nodeClass.simpleName.asString()}Initializer")

            builder.addProperties(model.properties.map {
                PropertySpec.builder(
                    it.simpleName.asString(),
                    it.type.resolve().makeNullable().asTypeName()
                ).mutable(true).initializer("null").build()
            })

            builder.addProperties(model.children.map {
                val resolvedType = it.type.resolve()
                when {
                    resolvedType.isAssignableTo(nullableNodeType) ->
                        PropertySpec.builder(
                            it.simpleName.asString(),
                            resolvedType.makeNullable().asTypeName()
                        ).mutable(true).initializer("null").build()
                    // If we're dealing with a list of children, create a MutableList
                    resolvedType.isAssignableTo(nodeListType) ->
                        PropertySpec.builder(
                            it.simpleName.asString(),
                            ClassName("kotlin.collections", "MutableList").parameterizedBy(
                                resolvedType.arguments.map { argument ->
                                    argument.type!!.resolve().asTypeName()
                                }
                            )
                        ).initializer("mutableListOf()").build()
                    else -> throw RuntimeException("Unexpected child type found: $it : ${it.type}")
                }
            })

            builder.addProperties(model.references.map {
                PropertySpec.builder(
                    it.simpleName.asString(),
                    it.type.resolve().makeNullable().asTypeName()
                ).mutable(true).initializer("null").build()
            })

            builder.addFunction(FunSpec.builder("build")
                .returns(model.nodeClass.asClassName())
                .addStatement("return ${model.nodeClass.simpleName.asString()}Impl(this)")
                .build()
            )

            return builder.build()
        }
    }
}



