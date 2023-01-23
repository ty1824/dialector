package dev.dialector.syntax

interface RootId

/**
 * An abstract syntactic representation of a program.
 */
interface SyntacticModel {
    fun getRoots(): Sequence<Node>
    fun getRoot(id: RootId): Node?
}

/**
 * A [SyntacticModel] that changes over time.
 */
interface DynamicSyntacticModel : SyntacticModel {
    /**
     * Adds or replaces a root in the model
     */
    fun putRoot(id: RootId, root: Node)

    /**
     * Removes a root from the model
     */
    fun removeRoot(id: RootId)
}

