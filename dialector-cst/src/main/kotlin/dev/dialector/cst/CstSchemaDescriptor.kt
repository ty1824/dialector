package dev.dialector.cst

/**
 * Cardinality of a slot in a CST composite node.
 */
enum class CstSlotCardinality(
    val isRequired: Boolean,
    val isMultiple: Boolean,
) {
    /** Exactly one child required (scalar `T`). */
    REQUIRED(isRequired = true, isMultiple = false),

    /** Optional child (scalar `T?`). */
    OPTIONAL(isRequired = false, isMultiple = false),

    /** Zero or more children (list `T*`). */
    ZERO_OR_MORE(isRequired = false, isMultiple = true),

    /** One or more children required (list `T+`). */
    ONE_OR_MORE(isRequired = true, isMultiple = true),
}

/**
 * Descriptor of a typed slot on a CST node.
 *
 * @property slot 0-based index of this slot in the parent node.
 * @property name Field name as defined in the schema.
 * @property cardinality Expected slot cardinality.
 * @property isTokenSlot True if this slot holds leaf tokens, false if it holds composite nodes.
 * @property targetTypeName Human-readable name of the expected target node or token type.
 * @property allowedIds Flattened set of allowed concrete node kind IDs or token schema IDs.
 *                      If empty, any composite node (for node slots) or any non-trivia token (for token slots) is accepted.
 */
class CstSlotDescriptor(
    val slot: Int,
    val name: String,
    val cardinality: CstSlotCardinality,
    val isTokenSlot: Boolean,
    val targetTypeName: String,
    val allowedIds: IntArray = EMPTY_INT_ARRAY,
) {
    /**
     * Returns true if a child with [childKind] is permitted in this slot.
     */
    fun acceptsChildKind(childKind: Int): Boolean {
        if (isTokenSlot) {
            if (!isTokenKind(childKind) || isTriviaKind(childKind)) return false
            if (isErrorTokenKind(childKind)) return true // recovery token in valid slot
            if (allowedIds.isEmpty()) return true
            val schemaId = decodeTokenSchemaId(childKind)
            return allowedIds.contains(schemaId)
        } else {
            if (isTokenKind(childKind)) return false
            if (childKind == ERROR_NODE_KIND) return true // recovery node
            if (allowedIds.isEmpty()) return true
            return allowedIds.contains(childKind)
        }
    }

    private companion object {
        private val EMPTY_INT_ARRAY = IntArray(0)
    }
}

/**
 * Base descriptor for a composite CST node type.
 * Companion objects of generated node syntax handles extend this class.
 */
open class CstNodeDescriptor(
    val kind: Int,
    val name: String,
    val slots: Array<CstSlotDescriptor>,
) {
    fun getSlot(slot: Int): CstSlotDescriptor? {
        if (slot in slots.indices && slots[slot].slot == slot) return slots[slot]
        for (s in slots) {
            if (s.slot == slot) return s
        }
        return null
    }
}

/**
 * Base descriptor for a leaf CST token type.
 * Companion objects of generated token handles extend this class.
 */
open class CstTokenDescriptor(
    val id: Int,
    val name: String,
    val runtimeKind: Int,
    val fixedText: String? = null,
    val isTrivia: Boolean = false,
)

/**
 * Base descriptor for a node category in the CST schema.
 * Companion objects of generated category handles extend this class.
 */
open class CstCategoryDescriptor(
    val name: String,
    val memberKinds: IntArray,
) {
    fun containsKind(kindId: Int): Boolean = memberKinds.contains(kindId)
}

/**
 * Base descriptor for a token category in the CST schema.
 * Companion objects of generated token category handles extend this class.
 */
open class CstTokenCategoryDescriptor(
    val name: String,
    val memberIds: IntArray,
) {
    fun containsToken(idOrRuntimeKind: Int): Boolean {
        val schemaId = if (isTokenKind(idOrRuntimeKind)) decodeTokenSchemaId(idOrRuntimeKind) else idOrRuntimeKind
        return memberIds.contains(schemaId)
    }
}

/**
 * Base descriptor for a complete CST schema.
 * Generated `<Schema>Schema` singleton objects extend this class.
 */
open class CstSchemaDescriptor(
    val name: String,
    val nodes: Array<out CstNodeDescriptor>,
    val tokens: Array<out CstTokenDescriptor>,
    val categories: Array<out CstCategoryDescriptor> = emptyArray(),
    val tokenCategories: Array<out CstTokenCategoryDescriptor> = emptyArray(),
) {
    private val nodeByKindMap: Map<Int, CstNodeDescriptor> by lazy {
        nodes.associateBy { it.kind }
    }

    private val tokenByIdMap: Map<Int, CstTokenDescriptor> by lazy {
        tokens.associateBy { it.id }
    }

    private val categoryByNameMap: Map<String, CstCategoryDescriptor> by lazy {
        categories.associateBy { it.name }
    }

    private val tokenCategoryByNameMap: Map<String, CstTokenCategoryDescriptor> by lazy {
        tokenCategories.associateBy { it.name }
    }

    fun getNodeDescriptor(kind: Int): CstNodeDescriptor? = nodeByKindMap[kind]

    fun getTokenDescriptor(schemaId: Int): CstTokenDescriptor? = tokenByIdMap[schemaId]

    fun getCategoryDescriptor(name: String): CstCategoryDescriptor? = categoryByNameMap[name]

    fun getTokenCategoryDescriptor(name: String): CstTokenCategoryDescriptor? = tokenCategoryByNameMap[name]

    fun getKindName(kindId: Int): String {
        if (kindId == ERROR_NODE_KIND) return "ErrorNode"
        return getNodeDescriptor(kindId)?.name ?: "UnknownNodeKind($kindId)"
    }

    fun getTokenName(idOrRuntimeKind: Int): String {
        val schemaId = if (isTokenKind(idOrRuntimeKind)) decodeTokenSchemaId(idOrRuntimeKind) else idOrRuntimeKind
        if (schemaId == 0) return "UNKNOWN"
        return getTokenDescriptor(schemaId)?.name ?: "UnknownTokenKind($idOrRuntimeKind)"
    }

    fun isKnownToken(idOrRuntimeKind: Int): Boolean {
        val schemaId = if (isTokenKind(idOrRuntimeKind)) decodeTokenSchemaId(idOrRuntimeKind) else idOrRuntimeKind
        return tokenByIdMap.containsKey(schemaId)
    }

    fun isKnownKind(kindId: Int): Boolean = nodeByKindMap.containsKey(kindId)
}
