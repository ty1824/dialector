@file:OptIn(KspExperimental::class)

package dev.dialector.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Variance
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import dev.dialector.syntax.Child
import dev.dialector.syntax.ModelConstructorDsl
import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeDefinition
import dev.dialector.syntax.NodeReference
import dev.dialector.syntax.NodeReferenceImpl
import dev.dialector.syntax.Property
import dev.dialector.syntax.Reference
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.reflect.KClass

const val OPT_PREFIX = "dev.dialector"

fun missingProperty(
    property: String,
    message: String,
): String = "Missing option $property: $message"

fun invalidValue(
    property: String,
    message: String,
): String = "Invalid value for $property: $message"

class DialectorSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        fun opt(name: String): String? = environment.options["$OPT_PREFIX.$name"]

        fun req(
            name: String,
            errorMessage: String,
        ): String? {
            val propertyName = "$OPT_PREFIX.$name"
            if (!environment.options.containsKey(propertyName)) {
                environment.logger.error(missingProperty(propertyName, errorMessage))
            }
            return environment.options[propertyName]
        }
        val formatterOptions =
            if (opt("formatter.enable") != "false") {
                FormatterOptions(
                    opt("formatter.editorConfigPath")?.let {
                        val path = Paths.get(it)
                        if (path.exists()) {
                            path
                        } else {
                            throw RuntimeException(
                                invalidValue("$OPT_PREFIX.formatter.enable", " editor config path `$path` does not exist"),
                            )
                        }
                    },
                )
            } else {
                null
            }
        val options =
            GenerationOptions(
                req("targetPackage", "must provide a target package") ?: return NoOpSymbolProcessor,
                opt("indent") ?: "    ",
                formatterOptions,
                opt("factory").toBoolean(),
            )
        return DialectorSymbolProcessor(
            environment,
            options,
        )
    }
}

object NoOpSymbolProcessor : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> = emptyList()
}

class FormatterOptions(
    /**
     * The path to the editor config file.
     */
    val editorConfigPath: Path?,
)

class GenerationOptions(
    /**
     * The package of the generated code.
     */
    val targetPackage: String,
    /**
     * The character pattern for indentation.
     */
    val indent: String,
    /**
     * Options for ktlint formatting. If present, formatting is enabled.
     */
    val formatter: FormatterOptions?,
    /**
     * Whether to generate factory functions.
     */
    val factory: Boolean = false,
)

class ProcessorError(
    val message: String,
    val symbol: KSNode? = null,
)

/**
 * Processes classes annotated with [NodeDefinition] and produces implementations and a builder DSL.
 */
class DialectorSymbolProcessor(
    val environment: SymbolProcessorEnvironment,
    val options: GenerationOptions,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(NodeDefinition::class.qualifiedName!!)
        val nodeDefinitions = symbols.filterIsInstance<KSClassDeclaration>()
        val generationResult = Generator(resolver).generate(options, nodeDefinitions)
        when (generationResult) {
            is Failure -> {
                generationResult.reason.forEach {
                    environment.logger.error(it.message, it.symbol)
                }
            }

            is Success -> {
//                val formatter = options.formatter?.let { formatOptions ->
//                    val ruleProviders = buildSet {
//                        ServiceLoader.load(RuleSetProviderV3::class.java)
//                            .flatMapTo(this) { it.getRuleProviders() }
//                    }
//                    KtLintRuleEngine(
//                        ruleProviders = StandardRuleSetProvider().getRuleProviders(),
//                        editorConfigDefaults = EditorConfigDefaults.load(formatOptions.editorConfigPath, setOf()),
//                    )
//                }
                generationResult.value.forEach { output ->
                    try {
                        val file =
                            environment.codeGenerator.createNewFile(
                                Dependencies(
                                    true,
                                    *(
                                        output.dependencies
                                            .mapNotNull { it.containingFile }
                                            .distinct()
                                            .toTypedArray()
                                    ),
                                ),
                                output.fileSpec.packageName,
                                output.fileSpec.name,
                            )
                        file.bufferedWriter().use { stream ->
//                            if (formatter != null) {
//                                val unformatted = StringBuilder().apply {
//                                    fileSpec.writeTo(this)
//                                }.toString()
//                                val formatted = formatter.format(Code.fromSnippet(unformatted))
//                                stream.write(formatted)
//                            } else {
//                                fileSpec.writeTo(stream)
//                            }
                            output.fileSpec.writeTo(stream)
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        }
        return listOf()
    }
}

class GeneratorOutput(
    val fileSpec: FileSpec,
    val source: KSClassDeclaration,
    val dependencies: List<KSNode>,
)

class Generator(
    private val resolver: Resolver,
) {
    private fun KClass<out Any>.getClassDeclaration(): KSClassDeclaration? =
        resolver.getClassDeclarationByName(resolver.getKSNameFromString(this.qualifiedName!!))

    // Helper types for code generation
    private val unitClass = Unit::class.getClassDeclaration()!!
    private val unitType = unitClass.asType(listOf())
    private val stringClass = String::class.getClassDeclaration()!!
    private val stringType = stringClass.asType(listOf())
    private val listClass = resolver.getClassDeclarationByName("kotlin.collections.List")!!
    private val mutableListClass = resolver.getClassDeclarationByName("kotlin.collections.MutableList")!!
    private val mapClass = Map::class.getClassDeclaration()!!

    private val nodeClass = Node::class.getClassDeclaration()!!
    private val nodeType = nodeClass.asType(listOf())
    private val nullableNodeType = nodeClass.asType(listOf()).makeNullable()
    private val nodeListType =
        listClass.asType(
            listOf(resolver.getTypeArgument(resolver.createKSTypeReferenceFromKSType(nodeType), Variance.INVARIANT)),
        )
    private val nodeReferenceClass = NodeReference::class.getClassDeclaration()!!
    private val nodeReferenceType =
        nodeReferenceClass
            .asType(
                listOf(
                    resolver.getTypeArgument(resolver.createKSTypeReferenceFromKSType(nodeType), Variance.COVARIANT),
                ),
            ).makeNullable()
    private val nodeReferenceImplClass = NodeReferenceImpl::class.getClassDeclaration()!!

    // Map<String, Any?>
    private val propertiesMapType by lazy {
        val anyClass = Any::class.getClassDeclaration()!!
        mapClass.asType(
            listOf(
                resolver.getTypeArgument(resolver.createKSTypeReferenceFromKSType(stringType), Variance.INVARIANT),
                resolver.getTypeArgument(
                    resolver.createKSTypeReferenceFromKSType(anyClass.asType(listOf()).makeNullable()),
                    Variance.INVARIANT,
                ),
            ),
        )
    }

    // Map<String, List<Node>>
    private val childrenMapType by lazy {
        mapClass.asType(
            listOf(
                resolver.getTypeArgument(resolver.createKSTypeReferenceFromKSType(stringType), Variance.INVARIANT),
                resolver.getTypeArgument(resolver.createKSTypeReferenceFromKSType(nodeListType), Variance.INVARIANT),
            ),
        )
    }

    // Map<String, NodeReference<*>?>
    private val referencesMapType by lazy {
        val starProjectedNodeRef = nodeReferenceClass.asStarProjectedType().makeNullable()
        mapClass.asType(
            listOf(
                resolver.getTypeArgument(resolver.createKSTypeReferenceFromKSType(stringType), Variance.INVARIANT),
                resolver.getTypeArgument(resolver.createKSTypeReferenceFromKSType(starProjectedNodeRef), Variance.INVARIANT),
            ),
        )
    }

    fun generate(
        options: GenerationOptions,
        classes: Sequence<KSClassDeclaration>,
    ): Result<List<GeneratorOutput>, List<ProcessorError>> = createGenerationModel(options, classes).flatMap { it.generate() }

    internal class PropertyModel(
        val forProperty: KSPropertyDeclaration,
        val hasDefault: Boolean = false,
    ) {
        val resolvedType: KSType by lazy { forProperty.type.resolve() }
    }

    internal class NodeModel(
        val sourceClass: KSClassDeclaration,
    ) {
        val inheritedNodes: List<KSClassDeclaration> =
            sourceClass
                .getAllSuperTypes()
                .mapNotNull { it.declaration as? KSClassDeclaration }
                .filter {
                    it.getAllSuperTypes().any { type ->
                        type.declaration.qualifiedName?.asString() == Node::class.qualifiedName
                    } && it.isAnnotationPresent(NodeDefinition::class)
                }.toList()

        val properties: List<PropertyModel> =
            sourceClass
                .getAllProperties()
                .mapNotNull { property ->
                    property.getAnnotationsByType(Property::class).firstOrNull()?.let {
                        PropertyModel(property, it.hasDefault)
                    }
                }.toList()
        val children: List<KSPropertyDeclaration> = sourceClass.getAllProperties().filter { it.isAnnotationPresent(Child::class) }.toList()
        val references: List<KSPropertyDeclaration> =
            sourceClass
                .getAllProperties()
                .filter {
                    it.isAnnotationPresent(
                        Reference::class,
                    )
                }.toList()

        val baseName = sourceClass.simpleName.getShortName()

        fun getImplClassName() = "${baseName}Impl"

        fun getBuilderClassName() = "${baseName}Initializer"

        fun getDslFunctionName() = baseName.replaceFirstChar { it.lowercaseChar() }

        fun requiresInit(): Boolean = properties.isNotEmpty() || children.isNotEmpty() || references.isNotEmpty()
    }

    private fun createNodeModel(nodeClass: KSClassDeclaration): Result<NodeModel, List<ProcessorError>> {
        val model = NodeModel(nodeClass)
        val problems = model.validate()
        return if (problems.isEmpty()) {
            Success(model)
        } else {
            Failure(
                problems,
            )
        }
    }

    private class ModelError(
        val message: String,
        val location: Location,
    ) {
        override fun toString(): String =
            if (location is FileLocation) {
                "$message (${location.filePath}:${location.lineNumber})"
            } else {
                "$message ($location)"
            }
    }

    private infix fun String.at(symbol: KSNode): ProcessorError = ProcessorError(this, symbol)

    private fun NodeModel.validate(): List<ProcessorError> {
        val errors: MutableList<ProcessorError> = mutableListOf()

        if (!sourceClass.isSubclassOf(Node::class)) {
            errors += "Input class must have Node as a superinterface" at sourceClass
        }

        if (sourceClass.modifiers.contains(Modifier.FINAL) || sourceClass.modifiers.contains(Modifier.SEALED)) {
            errors += "Input class must be extensible." at sourceClass
        }

        val validateName = { name: String, symbol: KSNode ->
            if (name == "properties" || name == "children" || name == "references") {
                errors += "Property name '$name' is reserved." at symbol
            }
        }

        properties.forEach {
            // A property may be any type besides a Node
            if (it.resolvedType.isAssignableTo(nullableNodeType)) {
                errors += "'${it.forProperty.qualifiedName}' Property must not be of type Node" at it.forProperty
            }
            validateName(it.forProperty.simpleName.asString(), it.forProperty)
        }

        children.forEach {
            // A child must either be of type T? or List<T> where T is a subclass of Node
            val resolvedType = it.type.resolve()
            if (!(resolvedType.isAssignableTo(nullableNodeType) || resolvedType.isAssignableTo(nodeListType))) {
                errors += "'${it.qualifiedName}': Child must be of type Node or List<Node>" at it
            }
            validateName(it.simpleName.asString(), it)
        }

        references.forEach {
            // A reference must be of type NodeReference<T>? where T is a subclass of Node.
            if (!it.type.resolve().isAssignableTo(nodeReferenceType)) {
                errors += "'${it.qualifiedName}' Reference must be of type NodeReference" at it
            }
            validateName(it.simpleName.asString(), it)
        }

        return errors.toList()
    }

    private fun createGenerationModel(
        options: GenerationOptions,
        classes: Sequence<KSClassDeclaration>,
    ): Result<GenerationModel, List<ProcessorError>> {
        val errors: MutableList<ProcessorError> = mutableListOf()
        val nodeModels: MutableMap<KSClassDeclaration, NodeModel> = mutableMapOf()
        classes.forEach {
            when (val result = createNodeModel(it)) {
                is Success -> nodeModels[it] = result.value
                is Failure -> errors += result.reason
            }
        }

        return if (errors.isNotEmpty()) {
            Failure(errors)
        } else {
            Success(GenerationModel(options, nodeModels.toMap()))
        }
    }

    internal inner class GenerationModel(
        val options: GenerationOptions,
        val nodeModels: Map<KSClassDeclaration, NodeModel>,
    ) {
        fun generate(): Result<List<GeneratorOutput>, List<ProcessorError>> {
            try {
                return Success(
                    nodeModels.values.map {
                        val builder = FileSpec.builder(options.targetPackage, "${it.baseName}Model")
                        builder.indent(options.indent)
                        handleClass(it, builder)
                        GeneratorOutput(builder.build(), it.sourceClass, it.inheritedNodes + it.sourceClass)
                    },
                )
            } catch (e: Exception) {
                return Failure(listOf(ProcessorError(e.stackTraceToString())))
            }
        }

        fun handleClass(
            model: NodeModel,
            builder: FileSpec.Builder,
        ) {
            builder.addType(generateImpl(model))
            if (model.requiresInit()) {
                builder.addType(generateBuilder(model))
            }
            builder.addFunction(generateBuilderDsl(model))
            generateFactory(model)?.let { builder.addFunction(it) }
        }

        fun generateBuilderDsl(model: NodeModel): FunSpec {
            val initializerClassName =
                ClassName(
                    options.targetPackage,
                    model.getBuilderClassName(),
                )
            val name = model.getDslFunctionName()
            if (model.requiresInit()) {
                return FunSpec
                    .builder(name)
                    .addParameter(
                        "init",
                        LambdaTypeName.get(
                            receiver = initializerClassName,
                            returnType = unitType.toTypeName(),
                        ),
                    ).returns(model.sourceClass.toClassName())
                    .addStatement("val node = ${initializerClassName.canonicalName}().apply(init).build()")
                    .addStatement("node.%M().forEach { it.parent = node }", MemberName("dev.dialector.syntax", "getAllChildren", true))
                    .addStatement("return node")
                    .build()
            } else {
                return FunSpec
                    .builder(name)
                    .returns(model.sourceClass.toClassName())
                    .addStatement("""return ${ClassName(options.targetPackage, model.getImplClassName())}()""")
                    .build()
            }
        }

        fun generateFactory(model: NodeModel): FunSpec? {
            if (options.factory && model.requiresInit()) {
                val name = model.getDslFunctionName()
                return FunSpec
                    .builder(name)
                    .apply {
                        // Add properties
                        addParameters(
                            model.properties.map { prop ->
                                ParameterSpec
                                    .builder(
                                        prop.forProperty.simpleName.asString(),
                                        prop.resolvedType
                                            .let {
                                                if (prop.hasDefault) {
                                                    it.makeNullable()
                                                } else {
                                                    it
                                                }
                                            }.toTypeName(),
                                    ).apply {
                                        if (prop.hasDefault || prop.resolvedType.isMarkedNullable) {
                                            defaultValue("null")
                                        }
                                    }.build()
                            },
                        )

                        // Add children
                        addParameters(
                            model.children.map {
                                val resolvedType = it.type.resolve()
                                when {
                                    resolvedType.isAssignableTo(nullableNodeType) -> {
                                        ParameterSpec
                                            .builder(
                                                it.simpleName.asString(),
                                                resolvedType.toTypeName(),
                                            ).apply {
                                                if (resolvedType.isMarkedNullable) {
                                                    defaultValue("null")
                                                }
                                            }.build()
                                    }

                                    // If we're dealing with a list of children, create a MutableList
                                    resolvedType.isAssignableTo(nodeListType) -> {
                                        ParameterSpec
                                            .builder(
                                                it.simpleName.asString(),
                                                ClassName("kotlin.collections", "List").parameterizedBy(
                                                    resolvedType.arguments.map { argument ->
                                                        argument.type!!.resolve().toTypeName()
                                                    },
                                                ),
                                            ).defaultValue("listOf()")
                                            .build()
                                    }

                                    else -> {
                                        throw RuntimeException("Unexpected child type found: $it : ${it.type}")
                                    }
                                }
                            },
                        )

                        // Add references - initialized using the target identifier
                        addParameters(
                            model.references.map { ref ->
                                ParameterSpec
                                    .builder(
                                        ref.simpleName.asString(),
                                        stringType
                                            .let {
                                                if (ref.type.resolve().isMarkedNullable) {
                                                    it.makeNullable()
                                                } else {
                                                    it
                                                }
                                            }.toTypeName(),
                                    ).build()
                            },
                        )
                    }.returns(model.sourceClass.toClassName())
                    .addCode(
                        CodeBlock
                            .builder()
                            .beginControlFlow("return %N", name)
                            .apply {
                                model.properties.forEach { prop ->
                                    val propName = prop.forProperty.simpleName.asString()
                                    addStatement("this.%N = %N", propName, propName)
                                }
                                model.children.forEach { child ->
                                    val childName = child.simpleName.asString()
                                    val resolvedType = child.type.resolve()
                                    when {
                                        resolvedType.isAssignableTo(nullableNodeType) -> {
                                            addStatement("this.%N = %N", childName, childName)
                                        }

                                        resolvedType.isAssignableTo(nodeListType) -> {
                                            addStatement("this.%N += %N", childName, childName)
                                        }

                                        else -> {
                                            throw RuntimeException("Unexpected child type found: $child : ${child.type}")
                                        }
                                    }
                                }

                                model.references.forEach { ref ->
                                    val refName = ref.simpleName.asString()
                                    addStatement("this.%N = %N", refName, refName)
                                }
                            }.endControlFlow()
                            .build(),
                    ).build()
            }
            return null
        }

        fun generateImpl(model: NodeModel): TypeSpec =
            TypeSpec
                .classBuilder(model.getImplClassName())
                .addModifiers(KModifier.PRIVATE)
                .addSuperinterface(model.sourceClass.asStarProjectedType().toTypeName())
                .addSuperinterface(nodeType.toTypeName())
                .primaryConstructor(generateConstructor(model))
                // Add properties
                .addProperties(model.properties.map { generateProperty(it) })
                // Add children
                .addProperties(model.children.map { generateChild(it) })
                // Add references
                .addProperties(model.references.map { generateReference(it) })
                .addFunction(generateToString(model))
                // Implement Node
                .apply { this.generateNodeImplementation(model) }
                .build()

        /**
         * Generates a `toString` implementation that delegates to `toDebugString`
         */
        fun generateToString(model: NodeModel) =
            FunSpec
                .builder("toString")
                .addModifiers(KModifier.OVERRIDE)
                .returns(stringType.toTypeName())
                .addCode("return super<%T>.%N()", model.sourceClass.toClassName(), Node::toDebugString.name)
                .build()

        private val optionalNodeTypeName = nodeType.makeNullable().toTypeName()

        fun TypeSpec.Builder.generateNodeImplementation(model: NodeModel) {
            // parent
            addProperty(
                PropertySpec
                    .builder("parent", optionalNodeTypeName)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("null")
                    .mutable(true)
                    .setter(
                        FunSpec
                            .setterBuilder()
                            .addParameter("value", nodeType.toTypeName())
                            // TODO: Expand this exception
                            .addStatement("""if (field != null) throw RuntimeException("A node may not be a child of two nodes.")""")
                            .addStatement("""field = value""")
                            .build(),
                    ).build(),
            )

            // properties map
            addProperty(
                PropertySpec
                    .builder(Node::properties.name, propertiesMapType.toTypeName())
                    .addModifiers(KModifier.OVERRIDE)
                    .getter(
                        FunSpec
                            .getterBuilder()
                            .addCode(
                                CodeBlock
                                    .builder()
                                    .add("return mapOf(")
                                    .indent()
                                    .apply {
                                        model.properties.forEach { property ->
                                            add(
                                                "\"${property.forProperty.simpleName.asString()}\" to " +
                                                    "${property.forProperty.simpleName.asString()}, ",
                                            )
                                        }
                                    }.unindent()
                                    .add(")")
                                    .build(),
                            ).build(),
                    ).build(),
            )

            // children map
            addProperty(
                PropertySpec
                    .builder(Node::children.name, childrenMapType.toTypeName())
                    .addModifiers(KModifier.OVERRIDE)
                    .getter(
                        FunSpec
                            .getterBuilder()
                            .addCode(
                                CodeBlock
                                    .builder()
                                    .add("return mapOf(")
                                    .indent()
                                    .apply {
                                        model.children.forEach { property ->
                                            val resolvedType = property.type.resolve()
                                            when {
                                                resolvedType.isAssignableTo(nullableNodeType) -> {
                                                    add(
                                                        "\"${property.simpleName.asString()}\" to listOfNotNull(${property.simpleName.asString()}), ",
                                                    )
                                                }

                                                resolvedType.isAssignableTo(nodeListType) -> {
                                                    add("\"${property.simpleName.asString()}\" to ${property.simpleName.asString()}, ")
                                                }

                                                else -> {
                                                    throw RuntimeException("Unexpected child type found: $property : ${property.type}")
                                                }
                                            }
                                        }
                                    }.unindent()
                                    .add(")")
                                    .build(),
                            ).build(),
                    ).build(),
            )

            // references map
            addProperty(
                PropertySpec
                    .builder(Node::references.name, referencesMapType.toTypeName())
                    .addModifiers(KModifier.OVERRIDE)
                    .getter(
                        FunSpec
                            .getterBuilder()
                            .addCode(
                                CodeBlock
                                    .builder()
                                    .add("return mapOf(")
                                    .indent()
                                    .apply {
                                        model.references.forEach { property ->
                                            add("\"${property.simpleName.asString()}\" to ${property.simpleName.asString()}, ")
                                        }
                                    }.unindent()
                                    .add(")")
                                    .build(),
                            ).build(),
                    ).build(),
            )
        }

        fun generateConstructor(model: NodeModel): FunSpec =
            FunSpec
                .constructorBuilder()
                .apply {
                    if (model.requiresInit()) {
                        addParameter(
                            ParameterSpec(
                                "init",
                                ClassName(options.targetPackage, model.getBuilderClassName()),
                            ),
                        )
                    }
                }.build()

        fun generateProperty(property: PropertyModel): PropertySpec {
            val propertyName = property.forProperty.simpleName.asString()
            val initializerSuffix =
                when {
                    property.hasDefault -> " ?: super.$propertyName"
                    !property.resolvedType.isMarkedNullable -> "!!"
                    else -> ""
                }
            return PropertySpec
                .builder(propertyName, property.resolvedType.toTypeName())
                .mutable(true)
                .addModifiers(KModifier.OVERRIDE)
                .initializer("init.$propertyName$initializerSuffix")
//                 .delegate("%T", PropertyValue::class.asClassName().parameterizedBy(property.returnType.asTypeName()))
                .build()
        }

        fun generateChild(child: KSPropertyDeclaration): PropertySpec {
            val resolvedType = child.type.resolve()
            return when {
                resolvedType.isAssignableTo(nullableNodeType) -> {
                    PropertySpec
                        .builder(child.simpleName.asString(), resolvedType.toTypeName())
                        .mutable(true)
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer("init.${child.simpleName.asString()}${if (!resolvedType.isMarkedNullable) "!!" else ""}")
                        .build()
                }

                resolvedType.isAssignableTo(nodeListType) -> {
                    PropertySpec
                        .builder(
                            child.simpleName.asString(),
                            mutableListClass.toClassName().parameterizedBy(
                                resolvedType.arguments.map { argument ->
                                    argument.type!!.resolve().toTypeName()
                                },
                            ),
                        ).addModifiers(KModifier.OVERRIDE)
                        .initializer("init.${child.simpleName.asString()}.toMutableList()")
                        .build()
                }

                else -> {
                    throw RuntimeException("Unexpected child type found: $child : $resolvedType")
                }
            }
        }

        fun generateReference(reference: KSPropertyDeclaration): PropertySpec {
            val resolvedType = reference.type.resolve()
            val className = (reference.parentDeclaration as KSClassDeclaration).toClassName()
            val referenceName = className.member(reference.simpleName.asString())
            val initializerCode =
                if (resolvedType.isMarkedNullable) {
                    CodeBlock.of(
                        "init.%L?.let { %T(this, %L, it) }",
                        reference.simpleName.asString(),
                        nodeReferenceImplClass.toClassName(),
                        referenceName.reference(),
                    )
                } else {
                    CodeBlock.of(
                        "%T(this, %L, init.%L!!)",
                        nodeReferenceImplClass.toClassName(),
                        referenceName.reference(),
                        reference.simpleName.asString(),
                    )
                }

            return PropertySpec
                .builder(reference.simpleName.asString(), resolvedType.toTypeName())
                .mutable(true)
                .addModifiers(KModifier.OVERRIDE)
                .initializer(initializerCode)
                .build()
        }

        /**
         * Creates the [Node] builder for the given [NodeModel]
         */
        fun generateBuilder(model: NodeModel): TypeSpec {
            val builder = TypeSpec.classBuilder("${model.baseName}Initializer")

            builder.addAnnotation(AnnotationSpec.builder(ModelConstructorDsl::class).build())

            builder.addProperties(
                model.properties.map {
                    PropertySpec
                        .builder(
                            it.forProperty.simpleName.asString(),
                            it.resolvedType.makeNullable().toTypeName(),
                        ).mutable(true)
                        .initializer("null")
                        .build()
                },
            )

            builder.addProperties(
                model.children.map {
                    val resolvedType = it.type.resolve()
                    when {
                        resolvedType.isAssignableTo(nullableNodeType) -> {
                            PropertySpec
                                .builder(
                                    it.simpleName.asString(),
                                    resolvedType.makeNullable().toTypeName(),
                                ).mutable(true)
                                .initializer("null")
                                .build()
                        }

                        // If we're dealing with a list of children, create a MutableList
                        resolvedType.isAssignableTo(nodeListType) -> {
                            PropertySpec
                                .builder(
                                    it.simpleName.asString(),
                                    ClassName("kotlin.collections", "MutableList").parameterizedBy(
                                        resolvedType.arguments.map { argument ->
                                            argument.type!!.resolve().toTypeName()
                                        },
                                    ),
                                ).initializer("mutableListOf()")
                                .build()
                        }

                        else -> {
                            throw RuntimeException("Unexpected child type found: $it : ${it.type}")
                        }
                    }
                },
            )

            // References are initialized using the target identifier
            builder.addProperties(
                model.references.map {
                    PropertySpec
                        .builder(
                            it.simpleName.asString(),
                            stringType.makeNullable().toTypeName(),
                        ).mutable(true)
                        .initializer("null")
                        .build()
                },
            )

            builder.addFunction(
                FunSpec
                    .builder("build")
                    .returns(model.sourceClass.toClassName())
                    .addStatement("return %N(this)", model.getImplClassName())
                    .build(),
            )

            return builder.build()
        }
    }
}
