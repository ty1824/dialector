package dev.dialector.cst.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import dev.dialector.cst.schema.CategorySchema
import dev.dialector.cst.schema.CstSchema
import dev.dialector.cst.schema.FieldSchema
import dev.dialector.cst.schema.FieldType
import dev.dialector.cst.schema.NodeSchema
import dev.dialector.cst.schema.TokenCategorySchema
import dev.dialector.cst.schema.TokenSchema

/**
 * KotlinPoet-powered CST code generator that builds structured [FileSpec] ASTs
 * from a [CstSchema], supporting value classes (`KModifier.VALUE` + `@JvmInline`)
 * and Kotlin context parameters (`context(tree: ConcreteSyntaxTree)`).
 */
public class CstKotlinPoetGenerator(
    public val config: CstGeneratorConfig = CstGeneratorConfig(),
) {
    /**
     * Generates all KotlinPoet [FileSpec]s for the given [schema].
     */
    public fun generateFileSpecs(schema: CstSchema): List<FileSpec> {
        schema.validate()
        val specs = mutableListOf<FileSpec>()
        specs.add(generateKindsFileSpec(schema))
        specs.add(generateHandlesFileSpec(schema))
        if (config.generateNavigators || config.generateBuilderExtensions || config.generateDebugHelpers) {
            specs.add(generateExtensionsFileSpec(schema))
        }
        return specs
    }

    // =========================================================================
    // 1. `<Schema>Kinds.kt`
    // =========================================================================

    public fun generateKindsFileSpec(schema: CstSchema): FileSpec {
        val fileBuilder = FileSpec.builder(schema.packageName, "${schema.name}Kinds")
            .indent("    ")

        val nodeKindsClassName = ClassName(schema.packageName, "${schema.name}NodeKinds")
        val tokenKindsClassName = ClassName(schema.packageName, "${schema.name}TokenKinds")

        // 1. NodeKinds object
        val nodeKindsObj = TypeSpec.objectBuilder(nodeKindsClassName)
            .addKdoc("Integer kind identifiers for [%L] CST nodes.", schema.name)
            .addProperty(
                PropertySpec.builder("ERROR", INT, KModifier.PUBLIC, KModifier.CONST)
                    .initializer("%M", M_ERROR_NODE_KIND)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("UNKNOWN", INT, KModifier.PUBLIC, KModifier.CONST)
                    .initializer("-1")
                    .build()
            )

        val getNodeKindNameBody = CodeBlock.builder()
            .beginControlFlow("return when (kindId)")
            .addStatement("ERROR -> %S", "ErrorNode")

        for (node in schema.nodes) {
            val constName = toUpperSnake(node.name)
            val prop = PropertySpec.builder(constName, INT, KModifier.PUBLIC, KModifier.CONST)
                .initializer("%L", node.id)
            if (node.doc != null) prop.addKdoc("%L", node.doc)
            nodeKindsObj.addProperty(prop.build())
            getNodeKindNameBody.addStatement("%L -> %S", constName, node.name)
        }
        getNodeKindNameBody
            .addStatement("else -> %P", "UnknownNodeKind(\$kindId)")
            .endControlFlow()

        nodeKindsObj.addFunction(
            FunSpec.builder("getKindName")
                .addParameter("kindId", INT)
                .returns(STRING)
                .addCode(getNodeKindNameBody.build())
                .build()
        )
        fileBuilder.addType(nodeKindsObj.build())

        // 2. TokenKinds object
        val tokenKindsObj = TypeSpec.objectBuilder(tokenKindsClassName)
            .addKdoc(
                "Integer kind identifiers for [%L] CST tokens.\n" +
                    "- Positive constants (`ID_*` / token names) are 1-based schema token IDs.\n" +
                    "- Negative constants (`KIND_*`) are pre-encoded runtime token kinds stored in `ConcreteSyntaxTree.nodeKinds`.",
                schema.name,
            )
            .addProperty(
                PropertySpec.builder("UNKNOWN", INT, KModifier.PUBLIC, KModifier.CONST)
                    .initializer("0")
                    .build()
            )

        val getTokenNameBody = CodeBlock.builder()
            .addStatement(
                "val schemaId = if (%M(idOrRuntimeKind)) %M(idOrRuntimeKind) else idOrRuntimeKind",
                M_IS_TOKEN_KIND,
                M_DECODE_TOKEN_SCHEMA_ID,
            )
            .beginControlFlow("return when (schemaId)")
            .addStatement("UNKNOWN -> %S", "UNKNOWN")

        val getTokenTextBody = CodeBlock.builder()
            .addStatement(
                "val schemaId = if (%M(kindId)) %M(kindId) else kindId",
                M_IS_TOKEN_KIND,
                M_DECODE_TOKEN_SCHEMA_ID,
            )
            .beginControlFlow("return when (schemaId)")

        for (token in schema.tokens) {
            val upper = toUpperSnake(token.name)
            val idProp = PropertySpec.builder(upper, INT, KModifier.PUBLIC, KModifier.CONST)
                .initializer("%L", token.id)
            if (token.doc != null) idProp.addKdoc("%L", token.doc)
            tokenKindsObj.addProperty(idProp.build())

            tokenKindsObj.addProperty(
                PropertySpec.builder("KIND_$upper", INT, KModifier.PUBLIC, KModifier.CONST)
                    .initializer("%L", token.runtimeKind)
                    .build()
            )

            getTokenNameBody.addStatement("%L -> %S", upper, token.name)
            if (token.fixedText != null) {
                getTokenTextBody.addStatement("%L -> %S", upper, token.fixedText)
            }
        }

        getTokenNameBody
            .addStatement("else -> %P", "UnknownTokenKind(\$idOrRuntimeKind)")
            .endControlFlow()
        getTokenTextBody
            .addStatement("else -> null")
            .endControlFlow()

        tokenKindsObj.addFunction(
            FunSpec.builder("getKindName")
                .addKdoc("Resolves a token name from either its positive schema [idOrRuntimeKind] or negative runtime kind.")
                .addParameter("idOrRuntimeKind", INT)
                .returns(STRING)
                .addCode(getTokenNameBody.build())
                .build()
        )
        tokenKindsObj.addFunction(
            FunSpec.builder("getTokenText")
                .addKdoc("Returns the fixed literal text for a token by its positive schema ID or negative runtime kind.")
                .addParameter("kindId", INT)
                .returns(STRING.copy(nullable = true))
                .addCode(getTokenTextBody.build())
                .build()
        )
        fileBuilder.addType(tokenKindsObj.build())

        // 3. Node Categories object
        if (schema.categories.isNotEmpty()) {
            val categoriesObj = TypeSpec.objectBuilder(ClassName(schema.packageName, "${schema.name}Categories"))
                .addKdoc("Category membership sets and lookup functions for [%L] CST nodes.", schema.name)

            for (category in schema.categories) {
                val catNodes = schema.getCategoryNodes(category.name)
                val upperName = toUpperSnake(category.name)
                val setInit = CodeBlock.builder().add("setOf(\n").indent()
                for (node in catNodes) {
                    setInit.add("%T.%L,\n", nodeKindsClassName, toUpperSnake(node.name))
                }
                setInit.unindent().add(")")

                categoriesObj.addProperty(
                    PropertySpec.builder("${upperName}_KINDS", SET.parameterizedBy(INT))
                        .initializer(setInit.build())
                        .build()
                )
                categoriesObj.addFunction(
                    FunSpec.builder("is${category.name}")
                        .addParameter("kindId", INT)
                        .returns(BOOLEAN)
                        .addStatement("return kindId in %L_KINDS", upperName)
                        .build()
                )
            }
            fileBuilder.addType(categoriesObj.build())
        }

        // 4. Token Categories object
        if (schema.tokenCategories.isNotEmpty()) {
            val tokenCategoriesObj = TypeSpec.objectBuilder(ClassName(schema.packageName, "${schema.name}TokenCategories"))
                .addKdoc("Token category membership sets and lookup functions for [%L] CST tokens.", schema.name)

            for (tokenCat in schema.tokenCategories) {
                val catTokens = schema.getTokenCategoryTokens(tokenCat.name)
                val upperName = toUpperSnake(tokenCat.name)
                val setInit = CodeBlock.builder().add("setOf(\n").indent()
                for (tok in catTokens) {
                    setInit.add("%T.%L,\n", tokenKindsClassName, toUpperSnake(tok.name))
                }
                setInit.unindent().add(")")

                tokenCategoriesObj.addProperty(
                    PropertySpec.builder("${upperName}_IDS", SET.parameterizedBy(INT))
                        .initializer(setInit.build())
                        .build()
                )
                tokenCategoriesObj.addFunction(
                    FunSpec.builder("is${tokenCat.name}")
                        .addParameter("idOrRuntimeKind", INT)
                        .returns(BOOLEAN)
                        .addStatement(
                            "val schemaId = if (%M(idOrRuntimeKind)) %M(idOrRuntimeKind) else idOrRuntimeKind",
                            M_IS_TOKEN_KIND,
                            M_DECODE_TOKEN_SCHEMA_ID,
                        )
                        .addStatement("return schemaId in %L_IDS", upperName)
                        .build()
                )
            }
            fileBuilder.addType(tokenCategoriesObj.build())
        }

        return fileBuilder.build()
    }

    // =========================================================================
    // 2. `<Schema>Handles.kt`
    // =========================================================================

    public fun generateHandlesFileSpec(schema: CstSchema): FileSpec {
        val fileBuilder = FileSpec.builder(schema.packageName, "${schema.name}Handles")
            .indent("    ")

        val categoriesObjName = ClassName(schema.packageName, "${schema.name}Categories")
        val tokenCategoriesObjName = ClassName(schema.packageName, "${schema.name}TokenCategories")

        // 1. Node Category extensions & handles
        for (category in schema.categories) {
            fileBuilder.addProperty(
                contextProperty("is${category.name}", BOOLEAN, receiver = CN_CST_NODE)
                    .addKdoc("True if this node belongs to category [%L].", category.name)
                    .getter(
                        FunSpec.getterBuilder()
                            .addStatement("return isValid && %T.is%L(kind)", categoriesObjName, category.name)
                            .build()
                    )
                    .build()
            )

            if (config.generateCategoryHandles) {
                addNodeCategoryHandle(fileBuilder, schema, category)
            }
        }

        // 2. Token Category extensions & handles
        for (tokenCat in schema.tokenCategories) {
            fileBuilder.addProperty(
                contextProperty("is${tokenCat.name}", BOOLEAN, receiver = CN_CST_TOKEN)
                    .addKdoc("True if this token belongs to token category [%L].", tokenCat.name)
                    .getter(
                        FunSpec.getterBuilder()
                            .addStatement("return isValid && %T.is%L(schemaId)", tokenCategoriesObjName, tokenCat.name)
                            .build()
                    )
                    .build()
            )
            fileBuilder.addProperty(
                contextProperty("is${tokenCat.name}Token", BOOLEAN, receiver = CN_CST_NODE)
                    .addKdoc("True if this node is a token belonging to token category [%L].", tokenCat.name)
                    .getter(
                        FunSpec.getterBuilder()
                            .addStatement("return isToken && %T.is%L(kind)", tokenCategoriesObjName, tokenCat.name)
                            .build()
                    )
                    .build()
            )

            if (config.generateTokenHandles && config.generateCategoryHandles) {
                addTokenCategoryHandle(fileBuilder, schema, tokenCat)
            }
        }

        // 3. Concrete Node Handles
        for (node in schema.nodes) {
            addNodeHandle(fileBuilder, schema, node)
        }

        // 4. Concrete Token Handles
        if (config.generateTokenHandles) {
            for (token in schema.tokens) {
                addTokenHandle(fileBuilder, schema, token)
            }
        }

        return fileBuilder.build()
    }

    private fun addNodeCategoryHandle(
        fileBuilder: FileSpec.Builder,
        schema: CstSchema,
        category: CategorySchema,
    ) {
        val handleClassName = ClassName(schema.packageName, categoryHandleName(category.name))
        val typeBuilder = valueClassBuilder(handleClassName, "nodeIndex")
        if (category.doc != null) typeBuilder.addKdoc("%L", category.doc)

        addCommonNodeDelegatedMembers(typeBuilder)

        for (superCat in schema.getSuperCategories(category.name)) {
            val superHandle = ClassName(schema.packageName, categoryHandleName(superCat))
            typeBuilder.addProperty(
                simpleGetterProperty("as$superCat", superHandle, "%T(nodeIndex)", superHandle)
            )
        }

        for (member in schema.getCategoryNodes(category.name)) {
            val memberHandle = ClassName(schema.packageName, nodeHandleName(member.name))
            typeBuilder.addProperty(
                contextGetterProperty("is${member.name}", BOOLEAN, "isValid && asNode.kind == %T.KIND", memberHandle)
            )
            typeBuilder.addProperty(
                simpleGetterProperty("as${member.name}", memberHandle, "%T(nodeIndex)", memberHandle)
            )
            typeBuilder.addProperty(
                contextGetterProperty(
                    "as${member.name}OrNone",
                    memberHandle,
                    "if (is%L) %T(nodeIndex) else %T.NONE",
                    member.name,
                    memberHandle,
                    memberHandle,
                )
            )
        }

        val companion = TypeSpec.companionObjectBuilder()
            .addProperty(
                PropertySpec.builder("NONE", handleClassName)
                    .initializer("%T(%M)", handleClassName, M_NULL_NODE_INDEX)
                    .build()
            )
            .addFunction(
                FunSpec.builder("fromNode")
                    .addParameter("node", CN_CST_NODE)
                    .returns(handleClassName)
                    .addStatement("return %T(node.nodeIndex)", handleClassName)
                    .build()
            )
            .build()

        typeBuilder.addType(companion)
        typeBuilder.addFunction(handleToStringFun(handleClassName.simpleName, "nodeIndex"))
        fileBuilder.addType(typeBuilder.build())

        fileBuilder.addProperty(
            simpleGetterProperty(
                name = "as${category.name}",
                type = handleClassName,
                receiver = CN_CST_NODE,
                kdoc = "Unchecked reinterpretation of this [CstNode] as a [${handleClassName.simpleName}].",
                format = "%T(nodeIndex)",
                args = arrayOf(handleClassName),
            )
        )
        fileBuilder.addProperty(
            contextGetterProperty(
                name = "as${category.name}OrNone",
                type = handleClassName,
                receiver = CN_CST_NODE,
                kdoc = "Safe conversion to [${handleClassName.simpleName}]; returns [${handleClassName.simpleName}.NONE] if this node does not belong to [${category.name}].",
                format = "if (is%L) %T(nodeIndex) else %T.NONE",
                args = arrayOf(category.name, handleClassName, handleClassName),
            )
        )
    }

    private fun addTokenCategoryHandle(
        fileBuilder: FileSpec.Builder,
        schema: CstSchema,
        tokenCat: TokenCategorySchema,
    ) {
        val tokCatHandle = ClassName(schema.packageName, tokenCategoryHandleName(tokenCat.name))
        val baseCatName = stripTokenSuffix(tokenCat.name)
        val typeBuilder = valueClassBuilder(tokCatHandle, "tokenIndex")
        if (tokenCat.doc != null) typeBuilder.addKdoc("%L", tokenCat.doc)

        addCommonTokenDelegatedMembers(typeBuilder)

        for (superCat in schema.getSuperTokenCategories(tokenCat.name)) {
            val superHandle = ClassName(schema.packageName, tokenCategoryHandleName(superCat))
            typeBuilder.addProperty(
                simpleGetterProperty("as$superCat", superHandle, "%T(tokenIndex)", superHandle)
            )
        }

        for (member in schema.getTokenCategoryTokens(tokenCat.name)) {
            val memberHandle = ClassName(schema.packageName, tokenHandleName(member.name))
            typeBuilder.addProperty(
                contextGetterProperty("is${member.name}", BOOLEAN, "isValid && asToken.schemaId == %T.ID", memberHandle)
            )
            typeBuilder.addProperty(
                simpleGetterProperty("as${member.name}", memberHandle, "%T(tokenIndex)", memberHandle)
            )
            typeBuilder.addProperty(
                contextGetterProperty(
                    "as${member.name}OrNone",
                    memberHandle,
                    "if (is%L) %T(tokenIndex) else %T.NONE",
                    member.name,
                    memberHandle,
                    memberHandle,
                )
            )
        }

        val companion = TypeSpec.companionObjectBuilder()
            .addProperty(
                PropertySpec.builder("NONE", tokCatHandle)
                    .initializer("%T(%M)", tokCatHandle, M_NULL_TOKEN_INDEX)
                    .build()
            )
            .addFunction(
                FunSpec.builder("fromToken")
                    .addParameter("token", CN_CST_TOKEN)
                    .returns(tokCatHandle)
                    .addStatement("return %T(token.tokenIndex)", tokCatHandle)
                    .build()
            )
            .addFunction(
                FunSpec.builder("fromNode")
                    .addParameter("node", CN_CST_NODE)
                    .returns(tokCatHandle)
                    .addStatement("return %T(node.nodeIndex)", tokCatHandle)
                    .build()
            )
            .build()

        typeBuilder.addType(companion)
        typeBuilder.addFunction(handleToStringFun(tokCatHandle.simpleName, "tokenIndex"))
        fileBuilder.addType(typeBuilder.build())

        fileBuilder.addProperty(
            simpleGetterProperty(
                name = "as${tokenCat.name}",
                type = tokCatHandle,
                receiver = CN_CST_TOKEN,
                kdoc = "Unchecked reinterpretation of this [CstToken] as a [${tokCatHandle.simpleName}].",
                format = "%T(tokenIndex)",
                args = arrayOf(tokCatHandle),
            )
        )
        fileBuilder.addProperty(
            contextGetterProperty(
                name = "as${tokenCat.name}OrNone",
                type = tokCatHandle,
                receiver = CN_CST_TOKEN,
                kdoc = "Safe conversion to [${tokCatHandle.simpleName}]; returns [${tokCatHandle.simpleName}.NONE] if this token does not belong to [${tokenCat.name}].",
                format = "if (is%L) %T(tokenIndex) else %T.NONE",
                args = arrayOf(tokenCat.name, tokCatHandle, tokCatHandle),
            )
        )
        fileBuilder.addProperty(
            simpleGetterProperty(
                name = "as${baseCatName}Token",
                type = tokCatHandle,
                receiver = CN_CST_NODE,
                kdoc = "Converts this generic node handle into a [${tokCatHandle.simpleName}].",
                format = "%T(nodeIndex)",
                args = arrayOf(tokCatHandle),
            )
        )
    }

    private fun addNodeHandle(
        fileBuilder: FileSpec.Builder,
        schema: CstSchema,
        node: NodeSchema,
    ) {
        val handleClassName = ClassName(schema.packageName, nodeHandleName(node.name))
        val nodeKindsClassName = ClassName(schema.packageName, "${schema.name}NodeKinds")
        val typeBuilder = valueClassBuilder(handleClassName, "nodeIndex")
        if (node.doc != null) typeBuilder.addKdoc("%L", node.doc)

        addCommonNodeDelegatedMembers(typeBuilder)

        for (cat in schema.getNodeCategories(node.name)) {
            typeBuilder.addProperty(
                contextGetterProperty("is$cat", BOOLEAN, "true")
            )
            if (config.generateCategoryHandles) {
                val catHandle = ClassName(schema.packageName, categoryHandleName(cat))
                typeBuilder.addProperty(
                    simpleGetterProperty("as$cat", catHandle, "%T(nodeIndex)", catHandle)
                )
            }
        }

        for (field in node.fields) {
            addFieldMembersToNodeHandle(typeBuilder, schema, field)
        }

        val companion = TypeSpec.companionObjectBuilder()
            .addProperty(
                PropertySpec.builder("KIND", INT, KModifier.PUBLIC, KModifier.CONST)
                    .initializer("%T.%L", nodeKindsClassName, toUpperSnake(node.name))
                    .build()
            )
        for (field in node.fields) {
            companion.addProperty(
                PropertySpec.builder("SLOT_${toUpperSnake(field.name)}", INT, KModifier.PUBLIC, KModifier.CONST)
                    .initializer("%L", field.slot)
                    .build()
            )
        }
        companion
            .addProperty(
                PropertySpec.builder("NONE", handleClassName)
                    .initializer("%T(%M)", handleClassName, M_NULL_NODE_INDEX)
                    .build()
            )
            .addFunction(
                FunSpec.builder("fromNode")
                    .addParameter("node", CN_CST_NODE)
                    .returns(handleClassName)
                    .addStatement("return %T(node.nodeIndex)", handleClassName)
                    .build()
            )

        typeBuilder.addType(companion.build())
        typeBuilder.addFunction(handleToStringFun(handleClassName.simpleName, "nodeIndex"))
        fileBuilder.addType(typeBuilder.build())

        fileBuilder.addProperty(
            contextGetterProperty(
                name = "is${node.name}",
                type = BOOLEAN,
                receiver = CN_CST_NODE,
                kdoc = "True if this [CstNode] has kind [${handleClassName.simpleName}.KIND].",
                format = "isValid && kind == %T.KIND",
                args = arrayOf(handleClassName),
            )
        )
        fileBuilder.addProperty(
            simpleGetterProperty(
                name = "as${node.name}",
                type = handleClassName,
                receiver = CN_CST_NODE,
                kdoc = "Unchecked reinterpretation of this generic node handle into a [${handleClassName.simpleName}].",
                format = "%T(nodeIndex)",
                args = arrayOf(handleClassName),
            )
        )
        fileBuilder.addProperty(
            contextGetterProperty(
                name = "as${node.name}OrNone",
                type = handleClassName,
                receiver = CN_CST_NODE,
                kdoc = "Safe conversion of this generic node handle into a [${handleClassName.simpleName}], returning [${handleClassName.simpleName}.NONE] if kinds do not match.",
                format = "if (is%L) %T(nodeIndex) else %T.NONE",
                args = arrayOf(node.name, handleClassName, handleClassName),
            )
        )
        fileBuilder.addFunction(
            FunSpec.builder("to${node.name}")
                .receiver(CN_CST_NODE)
                .returns(handleClassName)
                .addKdoc("Converts this generic node handle into a [%L].", handleClassName.simpleName)
                .addStatement("return %T(nodeIndex)", handleClassName)
                .build()
        )
    }

    private fun addTokenHandle(
        fileBuilder: FileSpec.Builder,
        schema: CstSchema,
        token: TokenSchema,
    ) {
        val tokHandle = ClassName(schema.packageName, tokenHandleName(token.name))
        val tokenKindsClassName = ClassName(schema.packageName, "${schema.name}TokenKinds")
        val baseName = stripTokenSuffix(token.name)
        val upperToken = toUpperSnake(token.name)

        val typeBuilder = valueClassBuilder(tokHandle, "tokenIndex")
        if (token.doc != null) typeBuilder.addKdoc("%L", token.doc)

        addCommonTokenDelegatedMembers(typeBuilder)

        for (tCat in schema.getTokenCategories(token.name)) {
            typeBuilder.addProperty(
                contextGetterProperty("is$tCat", BOOLEAN, "true")
            )
            if (config.generateCategoryHandles) {
                val tCatHandle = ClassName(schema.packageName, tokenCategoryHandleName(tCat))
                typeBuilder.addProperty(
                    simpleGetterProperty("as$tCat", tCatHandle, "%T(tokenIndex)", tCatHandle)
                )
            }
        }

        val companion = TypeSpec.companionObjectBuilder()
            .addProperty(
                PropertySpec.builder("ID", INT, KModifier.PUBLIC, KModifier.CONST)
                    .initializer("%T.%L", tokenKindsClassName, upperToken)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("KIND", INT, KModifier.PUBLIC, KModifier.CONST)
                    .initializer("%T.KIND_%L", tokenKindsClassName, upperToken)
                    .build()
            )

        if (token.fixedText != null) {
            companion.addProperty(
                PropertySpec.builder("FIXED_TEXT", STRING, KModifier.PUBLIC, KModifier.CONST)
                    .initializer("%S", token.fixedText)
                    .build()
            )
        } else {
            companion.addProperty(
                PropertySpec.builder("FIXED_TEXT", STRING.copy(nullable = true), KModifier.PUBLIC)
                    .initializer("null")
                    .build()
            )
        }

        companion
            .addProperty(
                PropertySpec.builder("IS_TRIVIA", BOOLEAN, KModifier.PUBLIC, KModifier.CONST)
                    .initializer("%L", token.isTrivia)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("NONE", tokHandle)
                    .initializer("%T(%M)", tokHandle, M_NULL_TOKEN_INDEX)
                    .build()
            )
            .addFunction(
                FunSpec.builder("fromToken")
                    .addParameter("token", CN_CST_TOKEN)
                    .returns(tokHandle)
                    .addStatement("return %T(token.tokenIndex)", tokHandle)
                    .build()
            )
            .addFunction(
                FunSpec.builder("fromNode")
                    .addParameter("node", CN_CST_NODE)
                    .returns(tokHandle)
                    .addStatement("return %T(node.nodeIndex)", tokHandle)
                    .build()
            )

        typeBuilder.addType(companion.build())
        typeBuilder.addFunction(handleToStringFun(tokHandle.simpleName, "tokenIndex"))
        fileBuilder.addType(typeBuilder.build())

        fileBuilder.addProperty(
            contextGetterProperty(
                name = "is${token.name}",
                type = BOOLEAN,
                receiver = CN_CST_TOKEN,
                kdoc = "True if this [CstToken] has schema ID [${tokHandle.simpleName}.ID].",
                format = "isValid && schemaId == %T.ID",
                args = arrayOf(tokHandle),
            )
        )
        fileBuilder.addProperty(
            simpleGetterProperty(
                name = "as${token.name}",
                type = tokHandle,
                receiver = CN_CST_TOKEN,
                kdoc = "Unchecked reinterpretation of this generic token handle into a [${tokHandle.simpleName}].",
                format = "%T(tokenIndex)",
                args = arrayOf(tokHandle),
            )
        )
        fileBuilder.addProperty(
            contextGetterProperty(
                name = "as${token.name}OrNone",
                type = tokHandle,
                receiver = CN_CST_TOKEN,
                kdoc = "Safe conversion of this generic token handle into a [${tokHandle.simpleName}], returning [${tokHandle.simpleName}.NONE] if token IDs do not match.",
                format = "if (is%L) %T(tokenIndex) else %T.NONE",
                args = arrayOf(token.name, tokHandle, tokHandle),
            )
        )
        fileBuilder.addProperty(
            simpleGetterProperty(
                name = "as${baseName}Token",
                type = tokHandle,
                receiver = CN_CST_NODE,
                kdoc = "Converts this generic node handle into a [${tokHandle.simpleName}].",
                format = "%T(nodeIndex)",
                args = arrayOf(tokHandle),
            )
        )
        fileBuilder.addFunction(
            FunSpec.builder("to${token.name}")
                .receiver(CN_CST_TOKEN)
                .returns(tokHandle)
                .addKdoc("Converts this generic token handle into a [%L].", tokHandle.simpleName)
                .addStatement("return %T(tokenIndex)", tokHandle)
                .build()
        )
    }

    private fun addFieldMembersToNodeHandle(
        typeBuilder: TypeSpec.Builder,
        schema: CstSchema,
        field: FieldSchema,
    ) {
        val categoryNames = schema.categories.map { it.name }.toSet()
        val tokenCategoryNames = schema.tokenCategories.map { it.name }.toSet()
        val slotConst = "SLOT_${toUpperSnake(field.name)}"

        fun resolveNodeHandleType(nodeTypeName: String): ClassName = when {
            nodeTypeName == "CstNode" -> CN_CST_NODE
            categoryNames.contains(nodeTypeName) ->
                if (config.generateCategoryHandles) ClassName(schema.packageName, categoryHandleName(nodeTypeName)) else CN_CST_NODE
            else -> ClassName(schema.packageName, nodeHandleName(nodeTypeName))
        }

        fun resolveTokenHandleType(tokenTypeName: String?): ClassName {
            if (tokenTypeName == null || tokenTypeName == "CstToken" || !config.generateTokenHandles) {
                return CN_CST_TOKEN
            }
            if (tokenCategoryNames.contains(tokenTypeName)) {
                return if (config.generateCategoryHandles) ClassName(schema.packageName, tokenCategoryHandleName(tokenTypeName)) else CN_CST_TOKEN
            }
            return ClassName(schema.packageName, tokenHandleName(tokenTypeName))
        }

        when (val type = field.type) {
            is FieldType.ChildNode -> {
                val targetHandle = resolveNodeHandleType(type.nodeTypeName)
                val optionalityNote = if (type.isOptional) "Optional child" else "Mandatory child (may be [NONE] or `isMissing` on syntax error)"
                val doc = buildString {
                    if (field.doc != null) appendLine(field.doc)
                    append("($optionalityNote at slot [$slotConst] = ${field.slot})")
                }
                typeBuilder.addProperty(
                    contextGetterProperty(
                        name = field.name,
                        type = targetHandle,
                        kdoc = doc,
                        format = "%T(tree.getChildBySlot(nodeIndex, %L))",
                        args = arrayOf(targetHandle, slotConst),
                    )
                )
            }

            is FieldType.ChildNodeList -> {
                val targetHandle = resolveNodeHandleType(type.nodeTypeName)
                addListFieldMembers(typeBuilder, field, targetHandle, slotConst)
            }

            is FieldType.ChildToken -> {
                val targetHandle = resolveTokenHandleType(type.tokenTypeName)
                val optionalityNote = if (type.isOptional) "Optional token" else "Mandatory token (may be [NONE] or `isMissing` on syntax error)"
                val doc = buildString {
                    if (field.doc != null) appendLine(field.doc)
                    append("($optionalityNote at slot [$slotConst] = ${field.slot})")
                }
                typeBuilder.addProperty(
                    contextGetterProperty(
                        name = field.name,
                        type = targetHandle,
                        kdoc = doc,
                        format = "%T(tree.getChildBySlot(nodeIndex, %L))",
                        args = arrayOf(targetHandle, slotConst),
                    )
                )
            }

            is FieldType.ChildTokenList -> {
                val targetHandle = resolveTokenHandleType(type.tokenTypeName)
                addListFieldMembers(typeBuilder, field, targetHandle, slotConst)
            }
        }
    }

    private fun addListFieldMembers(
        typeBuilder: TypeSpec.Builder,
        field: FieldSchema,
        targetHandle: ClassName,
        slotConst: String,
    ) {
        val listType = ClassName("kotlin.collections", "List").parameterizedBy(targetHandle)
        val capitalized = field.name.replaceFirstChar { it.uppercaseChar() }

        val propBuilder = contextProperty(field.name, listType)
        if (field.doc != null) propBuilder.addKdoc("%L", field.doc)
        propBuilder.getter(
            FunSpec.getterBuilder()
                .addStatement("val list = mutableListOf<%T>()", targetHandle)
                .beginControlFlow("tree.forEachChildInSlot(nodeIndex, %L) { childIdx ->", slotConst)
                .addStatement("list.add(%T(childIdx))", targetHandle)
                .endControlFlow()
                .addStatement("return list")
                .build()
        )
        typeBuilder.addProperty(propBuilder.build())

        val actionLambdaType = LambdaTypeName.get(parameters = arrayOf(targetHandle), returnType = UNIT)
        typeBuilder.addFunction(
            contextFun("forEach$capitalized")
                .addModifiers(KModifier.INLINE)
                .addParameter("action", actionLambdaType)
                .beginControlFlow("tree.forEachChildInSlot(nodeIndex, %L) { childIdx ->", slotConst)
                .addStatement("action(%T(childIdx))", targetHandle)
                .endControlFlow()
                .build()
        )
    }

    // =========================================================================
    // 3. `<Schema>Extensions.kt`
    // =========================================================================

    public fun generateExtensionsFileSpec(schema: CstSchema): FileSpec {
        val fileBuilder = FileSpec.builder(schema.packageName, "${schema.name}Extensions")
            .indent("    ")

        val nodeKindsClassName = ClassName(schema.packageName, "${schema.name}NodeKinds")
        val tokenKindsClassName = ClassName(schema.packageName, "${schema.name}TokenKinds")

        if (config.generateDebugHelpers) {
            fileBuilder.addFunction(
                FunSpec.builder("to${schema.name}DebugString")
                    .receiver(CN_CONCRETE_SYNTAX_TREE)
                    .addKdoc("Formats this [ConcreteSyntaxTree] using [%L] and [%L] names.", nodeKindsClassName.simpleName, tokenKindsClassName.simpleName)
                    .addParameter(
                        ParameterSpec.builder("includeTrivia", BOOLEAN)
                            .defaultValue("true")
                            .build()
                    )
                    .returns(STRING)
                    .addStatement(
                        "return toDebugTreeString(\n" +
                            "    includeTrivia = includeTrivia,\n" +
                            "    nodeNameLookup = %T::getKindName,\n" +
                            "    tokenNameLookup = %T::getKindName,\n" +
                            ")",
                        nodeKindsClassName,
                        tokenKindsClassName,
                    )
                    .build()
            )
        }

        if (config.generateNavigators) {
            fileBuilder.addFunction(
                FunSpec.builder("as${schema.name}Node")
                    .receiver(CN_CONCRETE_SYNTAX_TREE)
                    .addKdoc("Wraps the node index in a generic [CstNode].")
                    .addParameter("nodeIndex", INT)
                    .returns(CN_CST_NODE)
                    .addStatement("return %T(nodeIndex)", CN_CST_NODE)
                    .build()
            )
            fileBuilder.addProperty(
                PropertySpec.builder("root${schema.name}Node", CN_CST_NODE)
                    .receiver(CN_CONCRETE_SYNTAX_TREE)
                    .addKdoc("Returns the root node wrapped as a [CstNode], or [CstNode.NONE] if the tree is empty.")
                    .getter(FunSpec.getterBuilder().addStatement("return root").build())
                    .build()
            )
            if (config.generateTokenHandles) {
                fileBuilder.addFunction(
                    FunSpec.builder("as${schema.name}Token")
                        .receiver(CN_CONCRETE_SYNTAX_TREE)
                        .addKdoc("Wraps the token index in a generic [CstToken].")
                        .addParameter("tokenIndex", INT)
                        .returns(CN_CST_TOKEN)
                        .addStatement("return %T(tokenIndex)", CN_CST_TOKEN)
                        .build()
                )
            }
        }

        if (config.generateBuilderExtensions) {
            val blockLambdaType = LambdaTypeName.get(
                parameters = listOf(ParameterSpec.builder("nodeIndex", INT).build()),
                returnType = UNIT,
            )

            for (node in schema.nodes) {
                val handleClassName = ClassName(schema.packageName, nodeHandleName(node.name))
                val upperNode = toUpperSnake(node.name)

                fileBuilder.addFunction(
                    FunSpec.builder("add${node.name}")
                        .addModifiers(KModifier.INLINE)
                        .receiver(CN_CONCRETE_SYNTAX_BUILDER)
                        .addKdoc("Adds a [%L] node to this [ConcreteSyntaxBuilder].", handleClassName.simpleName)
                        .addParameter(ParameterSpec.builder("startOffset", INT).defaultValue("-1").build())
                        .addParameter(ParameterSpec.builder("endOffset", INT).defaultValue("-1").build())
                        .addParameter(ParameterSpec.builder("parentNode", INT).defaultValue("%M", M_NULL_NODE_INDEX).build())
                        .addParameter(ParameterSpec.builder("slot", INT).defaultValue("%M.toInt()", M_NO_SLOT).build())
                        .addParameter(ParameterSpec.builder("block", blockLambdaType).defaultValue("{}").build())
                        .returns(handleClassName)
                        .addStatement(
                            "val idx = node(\n" +
                                "    kind = %T.%L,\n" +
                                "    parentNode = parentNode,\n" +
                                "    slot = slot,\n" +
                                "    startOffset = startOffset,\n" +
                                "    endOffset = endOffset,\n" +
                                "    block = block,\n" +
                                ")",
                            nodeKindsClassName,
                            upperNode,
                        )
                        .addStatement("return %T(idx)", handleClassName)
                        .build()
                )

                fileBuilder.addFunction(
                    FunSpec.builder("addMissing${node.name}")
                        .receiver(CN_CONCRETE_SYNTAX_BUILDER)
                        .addKdoc("Adds a missing [%L] recovery node to this [ConcreteSyntaxBuilder].", handleClassName.simpleName)
                        .addParameter("offset", INT)
                        .addParameter(ParameterSpec.builder("parentNode", INT).defaultValue("%M", M_NULL_NODE_INDEX).build())
                        .addParameter(ParameterSpec.builder("slot", INT).defaultValue("%M.toInt()", M_NO_SLOT).build())
                        .addParameter(ParameterSpec.builder("diagnosticCode", STRING.copy(nullable = true)).defaultValue("null").build())
                        .addParameter(ParameterSpec.builder("diagnosticMessage", STRING.copy(nullable = true)).defaultValue("null").build())
                        .returns(handleClassName)
                        .addStatement(
                            "val idx = addMissingNode(\n" +
                                "    kind = %T.%L,\n" +
                                "    offset = offset,\n" +
                                "    parentNode = parentNode,\n" +
                                "    slot = slot,\n" +
                                "    diagnosticCode = diagnosticCode,\n" +
                                "    diagnosticMessage = diagnosticMessage,\n" +
                                ")",
                            nodeKindsClassName,
                            upperNode,
                        )
                        .addStatement("return %T(idx)", handleClassName)
                        .build()
                )
            }

            if (config.generateTokenHandles) {
                for (token in schema.tokens) {
                    val tokHandle = ClassName(schema.packageName, tokenHandleName(token.name))
                    val baseName = stripTokenSuffix(token.name)
                    val upperTok = toUpperSnake(token.name)

                    fileBuilder.addFunction(
                        FunSpec.builder("add${baseName}Token")
                            .receiver(CN_CONCRETE_SYNTAX_BUILDER)
                            .addKdoc("Adds a [%L] token to this [ConcreteSyntaxBuilder].", tokHandle.simpleName)
                            .addParameter("startOffset", INT)
                            .addParameter("endOffset", INT)
                            .addParameter(ParameterSpec.builder("parentNode", INT).defaultValue("%M", M_NULL_NODE_INDEX).build())
                            .addParameter(ParameterSpec.builder("slot", INT).defaultValue("%M.toInt()", M_NO_SLOT).build())
                            .returns(tokHandle)
                            .addStatement(
                                "val idx = addToken(\n" +
                                "    schemaTokenId = %T.%L,\n" +
                                "    startOffset = startOffset,\n" +
                                "    endOffset = endOffset,\n" +
                                "    parentNode = parentNode,\n" +
                                "    slot = slot,\n" +
                                "    isTrivia = %L,\n" +
                                ")",
                            tokenKindsClassName,
                            upperTok,
                            token.isTrivia,
                            )
                            .addStatement("return %T(idx)", tokHandle)
                            .build()
                    )

                    fileBuilder.addFunction(
                        FunSpec.builder("addMissing${baseName}Token")
                            .receiver(CN_CONCRETE_SYNTAX_BUILDER)
                            .addKdoc("Adds a missing [%L] token to this [ConcreteSyntaxBuilder].", tokHandle.simpleName)
                            .addParameter("offset", INT)
                            .addParameter(ParameterSpec.builder("parentNode", INT).defaultValue("%M", M_NULL_NODE_INDEX).build())
                            .addParameter(ParameterSpec.builder("slot", INT).defaultValue("%M.toInt()", M_NO_SLOT).build())
                            .addParameter(ParameterSpec.builder("diagnosticCode", STRING.copy(nullable = true)).defaultValue("null").build())
                            .addParameter(ParameterSpec.builder("diagnosticMessage", STRING.copy(nullable = true)).defaultValue("null").build())
                            .returns(tokHandle)
                            .addStatement(
                                "val idx = addMissingToken(\n" +
                                    "    schemaTokenId = %T.%L,\n" +
                                    "    offset = offset,\n" +
                                    "    parentNode = parentNode,\n" +
                                    "    slot = slot,\n" +
                                    "    diagnosticCode = diagnosticCode,\n" +
                                    "    diagnosticMessage = diagnosticMessage,\n" +
                                    ")",
                                tokenKindsClassName,
                                upperTok,
                            )
                            .addStatement("return %T(idx)", tokHandle)
                            .build()
                    )
                }
            }
        }

        return fileBuilder.build()
    }

    // =========================================================================
    // KotlinPoet Helper Builders
    // =========================================================================

    private fun valueClassBuilder(className: ClassName, indexParamName: String): TypeSpec.Builder =
        TypeSpec.classBuilder(className)
            .addModifiers(KModifier.PUBLIC, KModifier.VALUE)
            .addAnnotation(JvmInline::class)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(indexParamName, INT)
                    .build()
            )
            .addProperty(
                PropertySpec.builder(indexParamName, INT, KModifier.PUBLIC)
                    .initializer(indexParamName)
                    .build()
            )

    private fun addCommonNodeDelegatedMembers(
        typeBuilder: TypeSpec.Builder,
    ) {
        typeBuilder.addProperty(
            simpleGetterProperty(
                name = "isValid",
                type = BOOLEAN,
                kdoc = "True if this handle points to a valid node index (>= 0).",
                format = "nodeIndex >= 0",
            )
        )
        typeBuilder.addProperty(
            simpleGetterProperty(
                name = "isNone",
                type = BOOLEAN,
                kdoc = "True if this handle is the absent sentinel ([NONE]).",
                format = "nodeIndex < 0",
            )
        )
        typeBuilder.addProperty(
            simpleGetterProperty(
                name = "asNode",
                type = CN_CST_NODE,
                kdoc = "Converts this specialized handle into a generic [CstNode].",
                format = "%T(nodeIndex)",
                args = arrayOf(CN_CST_NODE),
            )
        )
    }

    private fun addCommonTokenDelegatedMembers(typeBuilder: TypeSpec.Builder) {
        typeBuilder.addProperty(
            simpleGetterProperty(
                name = "isValid",
                type = BOOLEAN,
                kdoc = "True if this handle points to a valid token index (>= 0).",
                format = "tokenIndex >= 0",
            )
        )
        typeBuilder.addProperty(
            simpleGetterProperty(
                name = "isNone",
                type = BOOLEAN,
                kdoc = "True if this handle is the absent sentinel ([NONE]).",
                format = "tokenIndex < 0",
            )
        )
        typeBuilder.addProperty(
            simpleGetterProperty(
                name = "asToken",
                type = CN_CST_TOKEN,
                kdoc = "Converts this specialized handle into a generic [CstToken].",
                format = "%T(tokenIndex)",
                args = arrayOf(CN_CST_TOKEN),
            )
        )
        typeBuilder.addProperty(
            simpleGetterProperty(
                name = "asNode",
                type = CN_CST_NODE,
                kdoc = "Converts this specialized handle into a generic [CstNode].",
                format = "%T(tokenIndex)",
                args = arrayOf(CN_CST_NODE),
            )
        )
    }

    @OptIn(com.squareup.kotlinpoet.ExperimentalKotlinPoetApi::class)
    private fun contextProperty(
        name: String,
        type: TypeName,
        receiver: TypeName? = null,
    ): PropertySpec.Builder {
        val builder = PropertySpec.builder(name, type, KModifier.PUBLIC)
            .contextParameter("tree", CN_CONCRETE_SYNTAX_TREE)
        if (receiver != null) builder.receiver(receiver)
        return builder
    }

    @OptIn(com.squareup.kotlinpoet.ExperimentalKotlinPoetApi::class)
    private fun contextFun(
        name: String,
        receiver: TypeName? = null,
    ): FunSpec.Builder {
        val builder = FunSpec.builder(name)
            .contextParameter("tree", CN_CONCRETE_SYNTAX_TREE)
        if (receiver != null) builder.receiver(receiver)
        return builder
    }

    private fun simpleGetterProperty(
        name: String,
        type: TypeName,
        format: String,
        vararg args: Any,
    ): PropertySpec = simpleGetterProperty(
        name = name,
        type = type,
        receiver = null,
        kdoc = null,
        format = format,
        args = args,
    )

    private fun simpleGetterProperty(
        name: String,
        type: TypeName,
        receiver: TypeName? = null,
        kdoc: String? = null,
        format: String,
        args: Array<out Any> = emptyArray(),
    ): PropertySpec {
        val builder = PropertySpec.builder(name, type, KModifier.PUBLIC)
            .getter(FunSpec.getterBuilder().addStatement("return $format", *args).build())
        if (receiver != null) builder.receiver(receiver)
        if (kdoc != null) builder.addKdoc("%L", kdoc)
        return builder.build()
    }

    private fun contextGetterProperty(
        name: String,
        type: TypeName,
        format: String,
        vararg args: Any,
    ): PropertySpec = contextGetterProperty(
        name = name,
        type = type,
        receiver = null,
        kdoc = null,
        format = format,
        args = args,
    )

    private fun contextGetterProperty(
        name: String,
        type: TypeName,
        receiver: TypeName? = null,
        kdoc: String? = null,
        format: String,
        args: Array<out Any> = emptyArray(),
    ): PropertySpec {
        val builder = contextProperty(name, type, receiver)
            .getter(FunSpec.getterBuilder().addStatement("return $format", *args).build())
        if (kdoc != null) builder.addKdoc("%L", kdoc)
        return builder.build()
    }

    private fun handleToStringFun(handleSimpleName: String, indexProp: String): FunSpec =
        FunSpec.builder("toString")
            .addModifiers(KModifier.OVERRIDE)
            .returns(STRING)
            .addStatement(
                "return if (isValid) %P else %S",
                "$handleSimpleName(\$$indexProp)",
                "$handleSimpleName.NONE",
            )
            .build()

    internal fun nodeHandleName(nodeName: String): String = "${nodeName}Handle"

    internal fun categoryHandleName(categoryName: String): String = "${categoryName}Handle"

    internal fun stripTokenSuffix(tokenName: String): String =
        if (tokenName.endsWith("Token") && tokenName.length > 5) tokenName.removeSuffix("Token") else tokenName

    internal fun tokenHandleName(tokenName: String): String =
        "${stripTokenSuffix(tokenName)}TokenHandle"

    internal fun tokenCategoryHandleName(tokenCategoryName: String): String =
        "${stripTokenSuffix(tokenCategoryName)}TokenHandle"

    private fun toUpperSnake(name: String): String {
        val result = StringBuilder()
        for (i in name.indices) {
            val c = name[i]
            if (c.isUpperCase() && i > 0 && (name[i - 1].isLowerCase() || (i + 1 < name.length && name[i + 1].isLowerCase()))) {
                result.append('_')
            }
            result.append(c.uppercaseChar())
        }
        return result.toString()
    }

    private companion object {
        private const val CST_PKG = "dev.dialector.cst"
        val CN_CONCRETE_SYNTAX_TREE = ClassName(CST_PKG, "ConcreteSyntaxTree")
        val CN_CONCRETE_SYNTAX_BUILDER = ClassName(CST_PKG, "ConcreteSyntaxBuilder")
        val CN_CST_NODE = ClassName(CST_PKG, "CstNode")
        val CN_CST_TOKEN = ClassName(CST_PKG, "CstToken")

        val M_ERROR_NODE_KIND = MemberName(CST_PKG, "ERROR_NODE_KIND")
        val M_NULL_NODE_INDEX = MemberName(CST_PKG, "NULL_NODE_INDEX")
        val M_NULL_TOKEN_INDEX = MemberName(CST_PKG, "NULL_TOKEN_INDEX")
        val M_NO_SLOT = MemberName(CST_PKG, "NO_SLOT")
        val M_IS_TOKEN_KIND = MemberName(CST_PKG, "isTokenKind")
        val M_DECODE_TOKEN_SCHEMA_ID = MemberName(CST_PKG, "decodeTokenSchemaId")
    }
}
