package dev.dialector.cst.schema

import dev.dialector.cst.TRIVIA_KIND_MASK
import dev.dialector.cst.encodeTokenKind

/**
 * Exception thrown when a [CstSchema] fails validation.
 */
class CstSchemaException(
    val errors: List<String>,
) : IllegalArgumentException("Invalid CST Schema:\n${errors.joinToString("\n") { " - $it" }}")

/**
 * A concrete syntax tree schema defining node types, node categories, token categories, fields, and token types.
 */
data class CstSchema(
    val name: String,
    val packageName: String,
    val tokens: List<TokenSchema> = emptyList(),
    val tokenCategories: List<TokenCategorySchema> = emptyList(),
    val categories: List<CategorySchema> = emptyList(),
    val nodes: List<NodeSchema> = emptyList(),
) {
    private fun directSuperCategoriesOf(catName: String, categoryMap: Map<String, CategorySchema>): Set<String> {
        val result = linkedSetOf<String>()
        categoryMap[catName]?.superCategories?.let { result.addAll(it) }
        for (cat in categories) {
            if (catName in cat.members) {
                result.add(cat.name)
            }
        }
        return result
    }

    private fun directSuperTokenCategoriesOf(catName: String, categoryMap: Map<String, TokenCategorySchema>): Set<String> {
        val result = linkedSetOf<String>()
        categoryMap[catName]?.superCategories?.let { result.addAll(it) }
        for (cat in tokenCategories) {
            if (catName in cat.members) {
                result.add(cat.name)
            }
        }
        return result
    }

    /**
     * Returns all node categories that [nodeName] belongs to (directly or transitively via super-categories).
     */
    fun getNodeCategories(nodeName: String): Set<String> {
        val node = nodes.find { it.name == nodeName } ?: return emptySet()
        val result = mutableSetOf<String>()
        val categoryMap = categories.associateBy { it.name }

        fun collectSuperCategories(catName: String) {
            if (result.add(catName)) {
                directSuperCategoriesOf(catName, categoryMap).forEach { collectSuperCategories(it) }
            }
        }

        for (cat in node.categories) {
            collectSuperCategories(cat)
        }
        for (cat in categories) {
            if (nodeName in cat.members) {
                collectSuperCategories(cat.name)
            }
        }
        return result
    }

    /**
     * Returns all super-categories that node category [categoryName] belongs to (transitively, excluding itself).
     */
    fun getSuperCategories(categoryName: String): Set<String> {
        val result = mutableSetOf<String>()
        val categoryMap = categories.associateBy { it.name }

        fun collect(catName: String) {
            directSuperCategoriesOf(catName, categoryMap).forEach { superCat ->
                if (result.add(superCat)) {
                    collect(superCat)
                }
            }
        }

        collect(categoryName)
        return result
    }

    /**
     * Returns all nodes belonging to [categoryName] (directly or transitively).
     */
    fun getCategoryNodes(categoryName: String): Set<NodeSchema> {
        val result = mutableSetOf<NodeSchema>()
        for (node in nodes) {
            if (getNodeCategories(node.name).contains(categoryName)) {
                result.add(node)
            }
        }
        return result
    }

    /**
     * Returns all token categories that [tokenName] belongs to (directly or transitively via super-categories).
     */
    fun getTokenCategories(tokenName: String): Set<String> {
        val token = tokens.find { it.name == tokenName } ?: return emptySet()
        val result = mutableSetOf<String>()
        val categoryMap = tokenCategories.associateBy { it.name }

        fun collectSuperCategories(catName: String) {
            if (result.add(catName)) {
                directSuperTokenCategoriesOf(catName, categoryMap).forEach { collectSuperCategories(it) }
            }
        }

        for (cat in token.categories) {
            collectSuperCategories(cat)
        }
        for (cat in tokenCategories) {
            if (tokenName in cat.members) {
                collectSuperCategories(cat.name)
            }
        }
        return result
    }

    /**
     * Returns all super-categories that token category [tokenCategoryName] belongs to (transitively, excluding itself).
     */
    fun getSuperTokenCategories(tokenCategoryName: String): Set<String> {
        val result = mutableSetOf<String>()
        val categoryMap = tokenCategories.associateBy { it.name }

        fun collect(catName: String) {
            directSuperTokenCategoriesOf(catName, categoryMap).forEach { superCat ->
                if (result.add(superCat)) {
                    collect(superCat)
                }
            }
        }

        collect(tokenCategoryName)
        return result
    }

    /**
     * Returns all tokens belonging to [tokenCategoryName] (directly or transitively).
     */
    fun getTokenCategoryTokens(tokenCategoryName: String): Set<TokenSchema> {
        val result = mutableSetOf<TokenSchema>()
        for (token in tokens) {
            if (getTokenCategories(token.name).contains(tokenCategoryName)) {
                result.add(token)
            }
        }
        return result
    }

    /**
     * Validates the schema consistency, category hierarchies, and field integrity.
     */
    fun validate(): CstSchema {
        val errors = mutableListOf<String>()
        if (name.isBlank()) errors.add("Schema name cannot be blank")
        if (packageName.isBlank()) errors.add("Schema package name cannot be blank")

        val tokenCategoryNames = mutableSetOf<String>()
        val tokenCategoryMap = tokenCategories.associateBy { it.name }
        for (tokenCat in tokenCategories) {
            if (tokenCat.name.isBlank()) errors.add("Token category name cannot be blank")
            if (!tokenCategoryNames.add(tokenCat.name)) {
                errors.add("Duplicate token category name '${tokenCat.name}'")
            }
            if (tokenCat.isOpen && tokenCat.members.isNotEmpty()) {
                errors.add("Open token category '${tokenCat.name}' cannot define a '= ...' member union")
            } else if (!tokenCat.isOpen && tokenCat.members.isEmpty()) {
                errors.add("Closed token category '${tokenCat.name}' must define members with '= ...' or be declared 'open'")
            }
            for (superCat in tokenCat.superCategories) {
                val targetCat = tokenCategoryMap[superCat]
                if (targetCat == null) {
                    errors.add("Token category '${tokenCat.name}' references unknown super-token-category '$superCat'")
                } else if (!targetCat.isOpen) {
                    errors.add("Token category '${tokenCat.name}' cannot extend closed token category '$superCat' via ': $superCat'; mark '$superCat' as 'open' or include '${tokenCat.name}' in its '= ...' union")
                }
            }
        }

        // Check for token category inheritance cycles
        val visitedTokenCategories = mutableSetOf<String>()
        val tokenRecursionStack = mutableSetOf<String>()
        fun checkTokenCategoryCycle(catName: String): Boolean {
            visitedTokenCategories.add(catName)
            tokenRecursionStack.add(catName)
            if (!tokenCategoryMap.containsKey(catName)) {
                tokenRecursionStack.remove(catName)
                return false
            }
            var hasCycle = false
            for (superCat in directSuperTokenCategoriesOf(catName, tokenCategoryMap)) {
                if (tokenRecursionStack.contains(superCat)) {
                    errors.add("Cyclic inheritance detected in token categories: '$catName' -> '$superCat'")
                    hasCycle = true
                } else if (!visitedTokenCategories.contains(superCat)) {
                    if (checkTokenCategoryCycle(superCat)) hasCycle = true
                }
            }
            tokenRecursionStack.remove(catName)
            return hasCycle
        }

        for (tokenCat in tokenCategories) {
            if (!visitedTokenCategories.contains(tokenCat.name)) {
                checkTokenCategoryCycle(tokenCat.name)
            }
        }

        val tokenNames = mutableSetOf<String>()
        val tokenIds = mutableSetOf<Int>()
        for (token in tokens) {
            if (token.name.isBlank()) errors.add("Token name cannot be blank")
            if (!tokenNames.add(token.name)) errors.add("Duplicate token name '${token.name}'")
            if (tokenCategoryNames.contains(token.name)) {
                errors.add("Name '${token.name}' is used by both a TokenSchema and a TokenCategorySchema")
            }
            if (token.id <= 0 || token.id >= TRIVIA_KIND_MASK) {
                errors.add("Token '${token.name}' id must be in 1..<0x40000000 (got ${token.id})")
            }
            if (!tokenIds.add(token.id)) errors.add("Duplicate token id '${token.id}' on token '${token.name}'")
            for (cat in token.categories) {
                val targetCat = tokenCategoryMap[cat]
                if (targetCat == null) {
                    errors.add("Token '${token.name}' references unknown token category '$cat'")
                } else if (!targetCat.isOpen) {
                    errors.add("Token '${token.name}' cannot extend closed token category '$cat' via ': $cat'; mark '$cat' as 'open' or include '${token.name}' in its '= ...' union")
                }
            }
        }

        for (tokenCat in tokenCategories) {
            for (member in tokenCat.members) {
                if (!tokenNames.contains(member) && !tokenCategoryMap.containsKey(member)) {
                    errors.add("Token category '${tokenCat.name}' references unknown token or token category '$member'")
                }
            }
        }

        val categoryNames = mutableSetOf<String>()
        val categoryMap = categories.associateBy { it.name }
        for (category in categories) {
            if (category.name.isBlank()) errors.add("Category name cannot be blank")
            if (!categoryNames.add(category.name)) errors.add("Duplicate category name '${category.name}'")
            if (tokenNames.contains(category.name)) {
                errors.add("Name '${category.name}' is used by both a TokenSchema and a CategorySchema")
            }
            if (tokenCategoryNames.contains(category.name)) {
                errors.add("Name '${category.name}' is used by both a TokenCategorySchema and a CategorySchema")
            }
            if (category.isOpen && category.members.isNotEmpty()) {
                errors.add("Open category '${category.name}' cannot define a '= ...' member union")
            } else if (!category.isOpen && category.members.isEmpty()) {
                errors.add("Closed category '${category.name}' must define members with '= ...' or be declared 'open'")
            }
            for (superCat in category.superCategories) {
                val targetCat = categoryMap[superCat]
                if (targetCat == null) {
                    errors.add("Category '${category.name}' references unknown super-category '$superCat'")
                } else if (!targetCat.isOpen) {
                    errors.add("Category '${category.name}' cannot extend closed category '$superCat' via ': $superCat'; mark '$superCat' as 'open' or include '${category.name}' in its '= ...' union")
                }
            }
        }

        // Check for category inheritance cycles
        val visitedCategories = mutableSetOf<String>()
        val recursionStack = mutableSetOf<String>()
        fun checkCategoryCycle(catName: String): Boolean {
            visitedCategories.add(catName)
            recursionStack.add(catName)
            if (!categoryMap.containsKey(catName)) {
                recursionStack.remove(catName)
                return false
            }
            var hasCycle = false
            for (superCat in directSuperCategoriesOf(catName, categoryMap)) {
                if (recursionStack.contains(superCat)) {
                    errors.add("Cyclic inheritance detected in categories: '$catName' -> '$superCat'")
                    hasCycle = true
                } else if (!visitedCategories.contains(superCat)) {
                    if (checkCategoryCycle(superCat)) hasCycle = true
                }
            }
            recursionStack.remove(catName)
            return hasCycle
        }

        for (category in categories) {
            if (!visitedCategories.contains(category.name)) {
                checkCategoryCycle(category.name)
            }
        }

        val nodeNames = mutableSetOf<String>()
        val nodeIds = mutableSetOf<Int>()
        val nodeMap = nodes.associateBy { it.name }
        for (node in nodes) {
            if (node.name.isBlank()) errors.add("Node name cannot be blank")
            if (!nodeNames.add(node.name)) errors.add("Duplicate node name '${node.name}'")
            if (node.id <= 0) errors.add("Node '${node.name}' has non-positive id: ${node.id} (0 is reserved for ErrorNode)")
            if (!nodeIds.add(node.id)) errors.add("Duplicate node id '${node.id}' on node '${node.name}'")
            if (tokenNames.contains(node.name)) {
                errors.add("Name '${node.name}' is used by both a TokenSchema and a NodeSchema")
            }
            if (tokenCategoryNames.contains(node.name)) {
                errors.add("Name '${node.name}' is used by both a TokenCategorySchema and a NodeSchema")
            }
            if (categoryNames.contains(node.name)) {
                errors.add("Name '${node.name}' is used by both a CategorySchema and a NodeSchema")
            }
            for (cat in node.categories) {
                val targetCat = categoryMap[cat]
                if (targetCat == null) {
                    errors.add("Node '${node.name}' references unknown category '$cat'")
                } else if (!targetCat.isOpen) {
                    errors.add("Node '${node.name}' cannot extend closed category '$cat' via ': $cat'; mark '$cat' as 'open' or include '${node.name}' in its '= ...' union")
                }
            }

            val fieldNames = mutableSetOf<String>()
            val usedSlots = mutableMapOf<Int, String>()
            for (field in node.fields) {
                if (field.name.isBlank()) errors.add("Field name cannot be blank on node '${node.name}'")
                if (!fieldNames.add(field.name)) {
                    errors.add("Duplicate field '${field.name}' on node '${node.name}'")
                }
                if (field.slot < 0 || field.slot > Short.MAX_VALUE) {
                    errors.add("Field '${node.name}.${field.name}' must have a valid non-negative 16-bit slot (got ${field.slot})")
                } else {
                    val prevSlotOwner = usedSlots.put(field.slot, "${node.name}.${field.name}")
                    if (prevSlotOwner != null) {
                        errors.add("Duplicate slot index '${field.slot}' for '${node.name}.${field.name}' (conflicts with '$prevSlotOwner')")
                    }
                }

                when (val type = field.type) {
                    is FieldType.ChildNode -> {
                        if (!nodeMap.containsKey(type.nodeTypeName) && !categoryMap.containsKey(type.nodeTypeName) && type.nodeTypeName != "CstNode") {
                            errors.add("Field '${node.name}.${field.name}' references unknown node or category type '${type.nodeTypeName}'")
                        }
                    }
                    is FieldType.ChildNodeList -> {
                        if (!nodeMap.containsKey(type.nodeTypeName) && !categoryMap.containsKey(type.nodeTypeName) && type.nodeTypeName != "CstNode") {
                            errors.add("Field '${node.name}.${field.name}' references unknown node or category type '${type.nodeTypeName}'")
                        }
                    }
                    is FieldType.ChildToken -> {
                        val tName = type.tokenTypeName
                        if (tName != null && !tokenNames.contains(tName) && !tokenCategoryMap.containsKey(tName) && tName != "CstToken") {
                            errors.add("Field '${node.name}.${field.name}' references unknown token type '$tName'")
                        }
                    }
                    is FieldType.ChildTokenList -> {
                        val tName = type.tokenTypeName
                        if (tName != null && !tokenNames.contains(tName) && !tokenCategoryMap.containsKey(tName) && tName != "CstToken") {
                            errors.add("Field '${node.name}.${field.name}' references unknown token type '$tName'")
                        }
                    }
                }
            }
        }

        for (category in categories) {
            for (member in category.members) {
                if (!nodeMap.containsKey(member) && !categoryMap.containsKey(member)) {
                    errors.add("Category '${category.name}' references unknown node or category '$member'")
                }
            }
        }

        if (errors.isNotEmpty()) {
            throw CstSchemaException(errors)
        }
        return this
    }
}

/**
 * Definition of a composite node category in the CST schema.
 * Categories group discrete CST node types without requiring explicit JVM inheritance.
 */
data class CategorySchema(
    val name: String,
    val superCategories: List<String> = emptyList(),
    val members: List<String> = emptyList(),
    val isOpen: Boolean = false,
    val doc: String? = null,
)

/**
 * Definition of a token category (or union of tokens, e.g. binary operators) in the CST schema.
 * Groups discrete [TokenSchema] types and/or child token categories without requiring JVM boxing.
 */
data class TokenCategorySchema(
    val name: String,
    val superCategories: List<String> = emptyList(),
    val members: List<String> = emptyList(),
    val isOpen: Boolean = false,
    val doc: String? = null,
)

/**
 * Definition of a token type in the CST schema.
 * @property id Positive 1-based schema ID. Use [runtimeKind] when storing in [dev.dialector.cst.ConcreteSyntaxTree].
 * @property fixedText Fixed text representation of the token (e.g. "+", "val", "=="), or null for variable text.
 * @property isTrivia True if this token represents whitespace or comments.
 * @property categories List of [TokenCategorySchema] names this token belongs to.
 */
data class TokenSchema(
    val name: String,
    val id: Int,
    val fixedText: String? = null,
    val isTrivia: Boolean = false,
    val categories: List<String> = emptyList(),
    val doc: String? = null,
) {
    /**
     * Negative runtime kind integer stored in `ConcreteSyntaxTree.nodeKinds`.
     */
    val runtimeKind: Int
        get() = encodeTokenKind(id, isTrivia)
}

/**
 * Definition of a composite node type in the CST schema.
 * @property categories List of category names this node belongs to.
 */
data class NodeSchema(
    val name: String,
    val id: Int,
    val categories: List<String> = emptyList(),
    val fields: List<FieldSchema> = emptyList(),
    val doc: String? = null,
)

/**
 * Definition of a field on a node. Every field (including list fields) has a non-negative [slot]
 * stored in `ConcreteSyntaxTree.childSlots`.
 */
data class FieldSchema(
    val name: String,
    val type: FieldType,
    val slot: Int,
    val doc: String? = null,
)

/**
 * Sealed hierarchy of supported field types in CST nodes.
 */
sealed interface FieldType {
    data class ChildNode(
        val nodeTypeName: String,
        val isOptional: Boolean = false,
    ) : FieldType

    data class ChildNodeList(
        val nodeTypeName: String,
        val isNonEmpty: Boolean = false,
    ) : FieldType

    /**
     * @property tokenTypeName Name of a [TokenSchema], a [TokenCategorySchema], or null/"CstToken" for any token.
     */
    data class ChildToken(
        val tokenTypeName: String? = null,
        val isOptional: Boolean = false,
    ) : FieldType

    /**
     * @property tokenTypeName Name of a [TokenSchema], a [TokenCategorySchema], or null/"CstToken" for any token.
     */
    data class ChildTokenList(
        val tokenTypeName: String? = null,
        val isNonEmpty: Boolean = false,
    ) : FieldType
}
