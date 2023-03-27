package dev.dialector.syntax

public interface RootId

/**
 * An abstract syntactic representation of a program.
 */
public interface SyntacticModel {
    public fun getRoots(): Sequence<Node>
    public fun getRoot(id: RootId): Node?
}
