package dev.dialector.semantic

import dev.dialector.semantic.type.IdentityType
import dev.dialector.semantic.type.Type
import dev.dialector.semantic.type.inference.DataGraph
import dev.dialector.semantic.type.inference.new.InferenceConstraint
import dev.dialector.semantic.type.inference.new.InferenceConstraintSystem
import dev.dialector.semantic.type.inference.new.InferenceContext
import dev.dialector.semantic.type.inference.new.InferenceOrigin
import dev.dialector.semantic.type.inference.new.InferenceResult
import dev.dialector.semantic.type.inference.new.SimpleConstraintCreator
import dev.dialector.semantic.type.inference.new.VariableConstraint
import dev.dialector.semantic.type.inference.new.VariableConstraintKind
import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeClause
import dev.dialector.syntax.NodeReference
import dev.dialector.util.ClassifierClause
import dev.dialector.util.InstanceClause
import dev.dialector.util.TypesafeClause

/**
 * Scope graph constraints represent edges in the scope graph, therefore contribute directly to the scope graph
 * Type constraints contribute to the bounds systems
 *
 * Program flow, Scope graph, Types - all need to be represented in the same overall system as they each impact the other
 *
 * Semantic constraints map an arbitrary program in an arbitrary language down to an arbitrary set of common constraints
 * The solver's job is to take the arbitrary set of constraints and produce a solution.
 *
 * This process happens in two phases - reduction and incorporation.
 *
 * The reduction process applies reduction rules to each constraint. Reduction rules may
 * contribute to the solution.
 *
 * The incorporation process applies incorporation rules to the solution. Incorporation rules may
 * introduce new constraints or further refine the solution.
 *
 * If, at the end of a phase, there are unreduced constraints, the process repeats.
 *
 */

/**
 * A variable in the semantic constraint system.
 */
interface SemanticVariable

/**
 * A constraint on one or more elements in the semantic constraint system.
 */
interface SemanticConstraint

interface Origin

interface ConstraintCreator

interface TypeVariable : Type, SemanticVariable {
    val id: String
}

interface Scope : SemanticVariable

interface Namespace {
    val name: String
}

class SimpleNamespace(override val name: String): Namespace

enum class TypeRelation(val symbol: String) {
    SUBTYPE("<"),
    SUPERTYPE(">"),
    EQUIVALENT("=");

    fun opposite(): TypeRelation = when (this) {
        SUBTYPE -> SUPERTYPE
        SUPERTYPE -> SUBTYPE
        EQUIVALENT -> EQUIVALENT
    }

    override fun toString(): String = symbol
}

enum class VariableConstraintKind {
    PULL_UP,
    PUSH_DOWN
}

/**
 * A constraint indicating how a type variable should be resolved.
 *
 * PULL_UP indicates the result should be the lowest common supertype of all upper bounds.
 * PUSH_DOWN indicates the result should be the greatest common supertype of all lower bounds.
 */
data class TypeVariableConstraint(
    val variable: TypeVariable,
    val kind: VariableConstraintKind,
) : SemanticConstraint

/**
 * A constraint describing a relation between two types.
 *
 * If mutual is false, the relation is assumed to propagate only from the left to the right.
 * If mutual is true, the relation propagates bidirectionally.
 */
data class TypeRelationConstraint(
    val relation: TypeRelation,
    val left: Type,
    val right: Type,
    val mutual: Boolean = false
) : SemanticConstraint

object Types : ConstraintCreator {
    /**
     * Indicates that the variable's optimal solution should resolve using its upper bounds.
     */
    fun pullUp(variable: TypeVariable): TypeVariableConstraint =
        TypeVariableConstraint(variable, VariableConstraintKind.PULL_UP)

    /**
     * Indicates that the variable's optimal solution should resolve using its lower bounds.
     */
    fun pushDown(variable: TypeVariable): TypeVariableConstraint =
        TypeVariableConstraint(variable, VariableConstraintKind.PUSH_DOWN)

    /**
     * Creates a constraint between the two types with the context [TypeRelation]
     */
    fun relate(left: Type, relation: TypeRelation, right: Type): TypeRelationConstraint =
        TypeRelationConstraint(relation, left, right)

    /**
     * Indicates that two types should be considered equivalent.
     */
    infix fun Type.equal(type: Type): TypeRelationConstraint =
        TypeRelationConstraint(TypeRelation.EQUIVALENT, this, type)

    /**
     * Indicates that the left-hand-side type should be a subtype of the right-hand-side type.
     */
    infix fun Type.subtype(type: Type): TypeRelationConstraint =
        TypeRelationConstraint(TypeRelation.SUBTYPE, this, type)

    /**
     * Indicates that the left-hand-side type should be a supertype of the right-hand-side type.
     */
    infix fun Type.supertype(type: Type): TypeRelationConstraint =
        TypeRelationConstraint(TypeRelation.SUPERTYPE, this, type)
}

/**
 * Declares that this scope will inherit elements from the given scope.
 */
data class InheritScopeConstraint(
    val scope: Scope,
    val inheritFrom: Scope,
    val label: String
) : SemanticConstraint

/**
 * Declares an element in the context of the scope and namespace.
 */
data class DeclareElementConstraint(
    val scope: Scope,
    val namespace: Namespace,
    val element: Node,
    val name: String
) : SemanticConstraint

/**
 * Declares a namespaced alias for an element in the given scope and namespace.
 */
data class AliasElementConstraint(
    val scope: Scope,
    val namespace: Namespace,
    val original: String,
    val aliasNamespace: Namespace,
    val alias: String
) : SemanticConstraint

/**
 * Declares a named reference in the context of a scope and namespace.
 */
data class ReferenceIdentifierConstraint(
    /** The [Scope] the reference should be looked up in */
    val scope: Scope,
    /** The [Namespace] the search should use */
    val namespace: Namespace,
    /** The [NodeReference] to resolve */
    val reference: NodeReference<out Node>
) : SemanticConstraint

object Scopes : ConstraintCreator {
    fun Scope.inherit(inheritFrom: Scope, label: String): InheritScopeConstraint =
        InheritScopeConstraint(this, inheritFrom, label)

    fun Scope.declare(namespace: Namespace, element: Node, name: String): DeclareElementConstraint =
        DeclareElementConstraint(this, namespace, element, name)

    fun Scope.alias(namespace: Namespace, original: String, aliasNamespace: Namespace, alias: String): AliasElementConstraint =
        AliasElementConstraint(this, namespace, original, aliasNamespace, alias)

    fun Scope.reference(namespace: Namespace, reference: NodeReference<out Node>): ReferenceIdentifierConstraint =
        ReferenceIdentifierConstraint(this, namespace, reference)
}

/**
 * A constraint indicating that a type variable's value should be assigned to the result
 * of a type scope lookup.
 */
data class ReferencedTypeConstraint(
    val scope: Scope,
    val namespace: Namespace,
    val reference: NodeReference<out Node>,
    val variable: TypeVariable
) : SemanticConstraint

/**
 * A constraint indicating that a scope should inherit elements from the
 * given type's published scope.
 */
data class TypeScopeConstraint(
    /** The scope that will inherit from the type's scope. */
    val scope: Scope,
    /** The type from which to derive the scope. */
    val type: Type,
    /** Describes this inheritance relation for debugging purposes. */
    val label: String
) : SemanticConstraint

/**
 * Declares an element's type in the context of the scope and namespace.
 *
 * Can be used to override the type of an element previously specified in a parent scope.
 */
data class DeclareElementTypeConstraint(
    val scope: Scope,
    val namespace: Namespace,
    val name: String,
    val type: Type
) : SemanticConstraint

object TypeScopes : ConstraintCreator {
    fun Scope.referenceType(namespace: Namespace, target: NodeReference<out Node>, variable: TypeVariable): ReferencedTypeConstraint =
        ReferencedTypeConstraint(this, namespace, target, variable)

    fun Scope.inheritTypeScope(type: Type, label: String): TypeScopeConstraint =
        TypeScopeConstraint(this, type, label)

    fun Scope.declareTypeElement(namespace: Namespace, name: String, type: Type): DeclareElementTypeConstraint =
        DeclareElementTypeConstraint(this, namespace, name, type)
}

object ScopeConstraintCreator {
    fun inheritScope(scope: Scope, inheritFrom: Scope, label: String): InheritScopeConstraint =
        InheritScopeConstraint(scope, inheritFrom, label)

    fun declareElement(scope: Scope, namespace: Namespace, element: Node, name: String): DeclareElementConstraint =
        DeclareElementConstraint(scope, namespace, element, name)

    fun aliasElement(scope: Scope, namespace: Namespace, original: String, aliasNamespace: Namespace, alias: String): AliasElementConstraint =
        AliasElementConstraint(scope, namespace, original, aliasNamespace, alias)

    fun referenceElement(scope: Scope, namespace: Namespace, reference: NodeReference<out Node>): ReferenceIdentifierConstraint =
        ReferenceIdentifierConstraint(scope, namespace, reference)
}
object TypeScopeConstraintCreator {
    fun declareTypeElement(scope: Scope, namespace: Namespace, name: String, type: Type): DeclareElementTypeConstraint =
        DeclareElementTypeConstraint(scope, namespace, name, type)

    fun referenceType(scope: Scope, namespace: Namespace, target: NodeReference<out Node>, variable: TypeVariable): ReferencedTypeConstraint =
        ReferencedTypeConstraint(scope, namespace, target, variable)

    fun inheritTypeScope(scope: Scope, type: Type, label: String): TypeScopeConstraint =
        TypeScopeConstraint(scope, type, label)
}

/**
 * Describes how scopes can be passed between nodes.
 */
interface PropagationType

/**
 * A propagation type where a parent node passes a scope to a child node.
 */
object Parent : PropagationType

interface SemanticRuleContext {

    val node: Node?

    /**
     * Creates a new [Scope].
     */
    fun scope(name: String?): Scope

    /**
     * Propagates a given [Scope] to a [Node].
     *
     * @param scope The [Scope] to propagate
     * @param node The [Node] to propagate the scope to.
     * @param type The [PropagationType] of scope being propagated. Defaults to [Parent]
     */
    fun propagateScope(scope: Scope, node: Node, type: PropagationType = Parent)

    /**
     * Receives a propagated [Scope] of the given type.
     */
    fun receiveScope(type: PropagationType = Parent): Scope

    /**
     * Creates a new [TypeVariable]
     */
    fun typeVar(): TypeVariable

    /**
     * Creates or retrieves the [TypeVariable] for the given node.
     */
    fun typeOf(node: Node): TypeVariable

    fun propagateTypeScope(scope: Scope, type: Type)

    /**
     * Retrieves the [Scope] associated with a [Type]
     */
    fun typeScope(type: Type): Scope

    /**
     * Creates a new [SemanticConstraint] using the specified creator.
     */
    fun <T : ConstraintCreator> constraint(creator: T, routine: T.() -> SemanticConstraint)
}

class SemanticConstraintSystem(val constraints: Map<Node, List<SemanticConstraint>>)

/**
 * A clause that matches against [SemanticConstraint]s.
 */
interface ConstraintClause<T : SemanticConstraint> : TypesafeClause<T> {
    operator fun invoke(candidate: SemanticConstraint): Boolean =
        clauseClass.isInstance(candidate) && constraint(candidate as T)
}

/**
 * Creates a [ConstraintClause] that matches against a specific [SemanticConstraint] instance.
 */
fun <T : SemanticConstraint> given(forNode: T): ConstraintClause<T> =
    object : InstanceClause<T>(forNode), ConstraintClause<T> {
        override fun invoke(candidate: SemanticConstraint): Boolean = forNode == candidate
    }

/**
 * Creates a [ConstraintClause] that matches against a subclass of [SemanticConstraint].
 */
inline fun <reified T : SemanticConstraint> given(): ConstraintClause<T> =
    object : ClassifierClause<T>(T::class), ConstraintClause<T> {
        override fun invoke(candidate: SemanticConstraint): Boolean = clauseClass.isInstance(candidate)
    }

/**
 * Creates a [ConstraintClause] that matches constraints against a given predicate.
 */
inline fun <reified T : SemanticConstraint> given(crossinline predicate: (T) -> Boolean) = object : ConstraintClause<T> {
    override val clauseClass = T::class
    override fun constraint(candidate: T): Boolean = predicate(candidate)
}

interface ReductionContext {
    fun constraint(routine: ConstraintCreator.() -> TypeRelationConstraint)
    fun bound(routine: BoundCreator.() -> Bound)
    val scopeGraph: ScopeGraph
}

interface ReductionRule<T : SemanticConstraint> {
    val name: String
    val isValidFor: ConstraintClause<T>
    val reduce: ReductionContext.(constraint: T) -> Unit
}

class ConstraintSolver {
    fun reduce() {

    }

    fun incorporate() {}
}

interface SemanticRule<T : Node> {
    val name: String
    val isValidFor: NodeClause<T>
    val rule: SemanticRuleContext.(node: T) -> Unit

    suspend operator fun invoke(context: SemanticRuleContext, node: Node) {
        if (isValidFor(node)) context.rule(node as T)
    }
}

fun <T : Node> NodeClause<T>.evaluateSemantics(name: String, semantics: SemanticRuleContext.(node: T) -> Unit) =
    object : SemanticRule<T> {
        override val name = name
        override val isValidFor = this@evaluateSemantics
        override val rule = semantics
    }

object InferredTopType : IdentityType("InferredTop")

object InferredBottomType : IdentityType("InferredBottom")

data class InferredGreatestLowerBound(val types: List<Type>) : Type {
    override fun getComponents(): Sequence<Type> {
        return types.asSequence()
    }

    override fun toString(): String = "InferredUnion<${types.joinToString(" | ")}>"
}

data class InferredLeastUpperBound(val types: List<Type>) : Type {
    override fun getComponents(): Sequence<Type> {
        return types.asSequence()
    }

    override fun toString(): String = "InferredIntersection<${types.joinToString(" & ")}>"
}

interface BoundCreator {
    infix fun TypeVariable.exactBound(type: Type)
    infix fun TypeVariable.upperBound(type: Type)
    infix fun TypeVariable.lowerBound(type: Type)
}

object SimpleBoundCreator : BoundCreator {
    override fun TypeVariable.exactBound(type: Type) {
        BaseBound(TypeRelation.EQUIVALENT, this, type)
    }

    override fun TypeVariable.upperBound(type: Type) {
        BaseBound(TypeRelation.SUPERTYPE, this, type)
    }

    override fun TypeVariable.lowerBound(type: Type) {
        BaseBound(TypeRelation.SUBTYPE, this, type)
    }
}

typealias RelationalConstraintClause = (TypeRelationConstraint) -> Boolean
typealias ReductionRoutine = ReductionContext.(constraint: TypeRelationConstraint) -> Unit

private class SimpleReductionRule<T : SemanticConstraint>(
    override val name: String,
    override val isValidFor: ConstraintClause<T>,
    override val reduce: ReductionContext.(T) -> Unit
) : ReductionRule<T>

fun <T : SemanticConstraint> ConstraintClause<T>.reducesTo(name: String, routine: ReductionContext.(T) -> Unit): ReductionRule<T> =
    SimpleReductionRule(name, this, routine)

val redundantElimination: ReductionRule<TypeRelationConstraint> =
    given<TypeRelationConstraint> { it.left == it.right }.reducesTo("identity") {
        /* Nothing */
    }

val leftReduction: ReductionRule<TypeRelationConstraint> =
    given<TypeRelationConstraint> { it.left is TypeVariable }.reducesTo("leftReduction") {
        bound { BaseBound(it.relation, it.left as TypeVariable, it.right)}
    }
val rightReduction: ReductionRule<TypeRelationConstraint> =
    given<TypeRelationConstraint> { it.right is TypeVariable }.reducesTo("rightReduction") {
        bound { BaseBound(it.relation.opposite(), it.right as TypeVariable, it.left)}
    }

val inheritScope: ReductionRule<InheritScopeConstraint> =
    given<InheritScopeConstraint>().reducesTo("inheritScope") {
        scopeGraph
    }

val declareElement: ReductionRule<DeclareElementConstraint> =
    given<DeclareElementConstraint>().reducesTo("declareElement") {

    }

val aliasElement: ReductionRule<AliasElementConstraint> =
    given<AliasElementConstraint>().reducesTo("aliasElement") {

    }

val referenceIdentifier: ReductionRule<ReferenceIdentifierConstraint> =
    given<ReferenceIdentifierConstraint>().reducesTo("referenceIdentifier") {

    }

val declareTypeElement: ReductionRule<DeclareElementTypeConstraint> =
    given<DeclareElementTypeConstraint>().reducesTo("declareElementType") {

    }

private class BaseInferenceVariable(override val id: String) : TypeVariable {
    override fun equals(other: Any?): Boolean = this === other

    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String = "tv.$id"
}

sealed class ScopeGraphNode
class ScopeNode() : ScopeGraphNode()
class ReferenceNode(id: String): ScopeGraphNode()

sealed class ScopeGraphEdge

class Declaring()
class Inheriting(label: String): ScopeGraphEdge()
class Referencing(namespace: Namespace, target: NodeReference<out Node>): ScopeGraphEdge()
class ReferencingType(namespace: Namespace, target: NodeReference<out Node>): ScopeGraphEdge()

class ScopeGraph {
    private val graph: DataGraph<ScopeGraphNode, ScopeGraphEdge> = DataGraph()
//    val scopes: Map<Scope, ScopeNode>
}

interface Bound {
    val relation: TypeRelation
    val variable: TypeVariable
    val boundingType: Type

    fun lowerType(): Type = if (relation == TypeRelation.SUBTYPE || relation == TypeRelation.EQUIVALENT) variable else boundingType
    fun upperType(): Type = if (relation == TypeRelation.SUBTYPE || relation == TypeRelation.EQUIVALENT) boundingType else variable
}

data class BaseBound(
    override val relation: TypeRelation,
    override val variable: TypeVariable,
    override val boundingType: Type,
) : Bound

sealed class BoundGraphNode {
    abstract val type: Type
    abstract fun isProper(): Boolean
}

class TypeNode(override val type: Type) : BoundGraphNode() {
    override fun isProper(): Boolean = true
}

class VariableNode(val variable: TypeVariable) : BoundGraphNode() {
    val equivalentTo: MutableSet<Pair<BoundGraphNode, Bound>> = mutableSetOf()
    val upperBounds: MutableSet<Pair<BoundGraphNode, Bound>> = mutableSetOf()
    val lowerBounds: MutableSet<Pair<BoundGraphNode, Bound>> = mutableSetOf()
    /**
     * Determines whether the solution for this variable should be the greatest lower bound rather than the least upper bound.
     */
    var pushDown: Boolean = false

    override val type: Type
        get() = variable

    fun getDependencies(): Sequence<VariableNode> =
        if (pushDown) {
            lowerBounds.asSequence().filter {
                !it.first.isProper()
            }.map {
                it.first as VariableNode
            }
        } else {
            upperBounds.asSequence().filter {
                !it.first.isProper()
            }.map {
                it.first as VariableNode
            }
        } + equivalentTo.asSequence().filter {
            !it.first.isProper()
        }.map {
            it.first as VariableNode
        }

    override fun isProper(): Boolean = equivalentTo.any { it.first is TypeNode }
}

class BoundSystemGraph {
    private val graph: DataGraph<BoundGraphNode, Bound> = DataGraph()
    private val typeNodes: MutableMap<Type, TypeNode> = mutableMapOf()
    val variableNodes: MutableMap<TypeVariable, VariableNode> = mutableMapOf()

    fun addVariable(variable: TypeVariable, pushDown: Boolean = false) {
        val data = nodeFor(variable)
        data.pushDown = pushDown
        graph.addNode(data)
    }

    fun addBound(bound: Bound, origin: InferenceOrigin) {
        val lower = nodeFor(bound.lowerType())
        val upper = nodeFor(bound.upperType())
        graph.addEdge(bound, lower, upper, true)
        if (bound.relation != TypeRelation.EQUIVALENT) {
            if (lower is VariableNode) lower.upperBounds += upper to bound
            if (upper is VariableNode) upper.lowerBounds += lower to bound
        } else {
            if (lower is VariableNode) lower.equivalentTo += upper to bound
            if (upper is VariableNode) upper.equivalentTo += lower to bound
        }
    }

    fun setPushDown(variable: TypeVariable, pushDown: Boolean) {
        nodeFor(variable).pushDown = pushDown
    }

    fun getAllVariables(): Sequence<VariableNode> = variableNodes.values.asSequence()

    fun isResolved(variable: TypeVariable): Boolean = nodeFor(variable).isProper()

    fun getUnresolvedVariables(): Sequence<VariableNode> =
        variableNodes.values.asSequence()
            .filter { !it.isProper() }

    fun getBounds(variable: TypeVariable): Sequence<Bound> = graph.getEdges(nodeFor(variable)).map { it.data }

    fun getDependencyGroups(): Sequence<Set<VariableNode>> = sequence {
        val simpleNodes = getUnresolvedVariables().filter {
            it.getDependencies().count() == 0
        }.toList()

        // Return nodes with no dependencies first
        if (simpleNodes.isNotEmpty()) {
            for (node in simpleNodes) {
                yield(setOf(node))
            }
        }

        // Form dependency groups
    }

    private fun nodeFor(type: Type): BoundGraphNode =
        if (type is TypeVariable) {
            variableNodes.computeIfAbsent(type) { VariableNode(type) }
        } else {
            typeNodes.computeIfAbsent(type) { TypeNode(type) }
        }

    private fun nodeFor(variable: TypeVariable): VariableNode =
        variableNodes.computeIfAbsent(variable) { VariableNode(variable) }

    override fun toString(): String {
        val builder = StringBuilder("Bounds:\n")

        this.graph.getAllEdges().groupBy { it.data.lowerType() }.forEach { (_, value) ->
            value.forEach {
                if (it.data.variable == it.data.lowerType()) {
                    builder.append("\t${it.data.lowerType()} ${it.data.relation} ${it.data.upperType()}\n")
                } else {
                    builder.append("\t${it.data.lowerType()} ${it.data.relation.opposite()} ${it.data.upperType()}\n")
                }
            }
        }

        return builder.toString()
    }
}

class BaseInferenceContext(
    val createVariable: () -> TypeVariable,
    val addConstraint: (InferenceConstraint) -> Unit
) : InferenceContext, ConstraintCreator by SimpleConstraintCreator {
    override fun typeVar(): TypeVariable = createVariable()

    override fun constraint(routine: ConstraintCreator.() -> InferenceConstraint) {
        addConstraint(routine())
    }
}

data class ReducedFromConstraint(val constraint: InferenceConstraint, val rule: ReductionRule) : InferenceOrigin

class BaseReductionContext(
    constraint: RelationalConstraint,
    rule: ReductionRule,
    private val addConstraint: (RelationalConstraint, InferenceOrigin) -> Unit,
    private val addBound: (Bound, InferenceOrigin) -> Unit
) : ReductionContext {
    private val origin = ReducedFromConstraint(constraint, rule)
    override fun constraint(routine: ConstraintCreator.() -> RelationalConstraint) {
        addConstraint(SimpleConstraintCreator.routine(), origin)
    }

    override fun bound(routine: BoundCreator.() -> Bound) {
        addBound(SimpleBoundCreator.routine(), origin)
    }
}

class ConstraintSystem(initialConstraints: Iterable<InferenceConstraint>) {
    private val seenConstraints: MutableSet<InferenceConstraint> = initialConstraints.toMutableSet()
    private val unresolvedConstraints: MutableList<InferenceConstraint> = initialConstraints.toMutableList()

    fun add(constraint: InferenceConstraint) {
        if (!seenConstraints.contains(constraint)) {
            seenConstraints += constraint
            unresolvedConstraints += constraint
        }
    }

    fun reduce(reducer: (InferenceConstraint) -> Unit) {
        while (anyUnresolved()) {
            reducer(unresolvedConstraints.removeFirst())
        }
    }

    fun anyUnresolved(): Boolean = unresolvedConstraints.isNotEmpty()

    override fun toString(): String = "Constraints:\n\t${unresolvedConstraints.joinToString("\n\t")}"
}

class BaseInferenceConstraintSystem : InferenceConstraintSystem {
    private val variables: MutableSet<TypeVariable> = mutableSetOf()
    private val inferenceConstraints: MutableSet<InferenceConstraint> = mutableSetOf()
    private var variableIdCounter = 0

    override fun getInferenceVariables(): Set<TypeVariable> = variables.toSet()

    override fun getInferenceConstraints(): Set<InferenceConstraint> = inferenceConstraints.toSet()

    fun createVariable(): TypeVariable = BaseInferenceVariable("${variableIdCounter++}")

    fun registerConstraint(constraint: InferenceConstraint) {
        inferenceConstraints += constraint
    }

    fun solve(reductionRules: List<ReductionRule>) : InferenceResult {
        println("Solving")
        val initialConstraints = getInferenceConstraints()
        println(initialConstraints)
        val constraints = ConstraintSystem(initialConstraints)

        val bounds = BoundSystemGraph()
        getInferenceVariables().forEach(bounds::addVariable)

        var iteration = 0
        while (constraints.anyUnresolved()) {
            iteration++
            var subIteration = 0
            while (constraints.anyUnresolved()) {
                subIteration++
                println("ITERATION: ${iteration}.${subIteration} - Constraints")
                println(constraints)
                println(bounds)
                reduce(reductionRules, constraints, bounds)
                println("ITERATION: ${iteration}.${subIteration} - Incorporation")
                println(constraints)
                println(bounds)
                incorporate(constraints, bounds)
            }
            println("ITERATION $iteration - Solving")
            println(constraints)
            println(bounds)
            resolve(constraints, bounds)
        }
        println("Solving complete")
        // TODO: Verify initial constraints are satisfied by the solution
        return object : InferenceResult {
            val resultTable = bounds.variableNodes.map { (key, value) ->
                key to value.equivalentTo.filter { it.first is TypeNode }.map { it.first.type }.toList()
            }.toMap()
            override fun get(variable: TypeVariable): List<Type>? = resultTable[variable]
        }
    }

    private fun reduce(
        reductionRules: List<ReductionRule>,
        constraints: ConstraintSystem,
        bounds: BoundSystemGraph
    ) {
        constraints.reduce { constraint ->
            when (val currentConstraint = constraint) {
                is RelationalConstraint ->
                    // Apply each rule if valid for this constraint
                    reductionRules.firstOrNull {
                        it.isValidFor(currentConstraint)
                    }?.apply {
                        val reductionContext = BaseReductionContext(
                            currentConstraint,
                            this,
                            { constraint, _ ->
                                constraints.add(constraint)
                            },
                            { bound, origin ->
                                bounds.addBound(bound, origin)
                            }
                        )
                        reductionContext.reduce(currentConstraint)
                    }
                is VariableConstraint ->
                    bounds.setPushDown(currentConstraint.variable , currentConstraint.kind == VariableConstraintKind.PUSH_DOWN)
            }
        }
    }

    private fun incorporate(
        constraints: ConstraintSystem,
        boundSystem: BoundSystemGraph
    ) {
        SimpleConstraintCreator.apply {
            // TODO: Handle constraint origin once supported
            // For each bound system
            for (variable in boundSystem.getAllVariables()) {
                // Transitive incorporation (if a == b && b == c then a == c), etc
                variable.equivalentTo.forEach { equivalentVar ->
                    // if (x == a) && (l < x) then (l < a)
                    variable.lowerBounds.forEach { lowerBound ->
                        constraints.add(lowerBound.first.type subtype equivalentVar.first.type)
                    }

                    // if (x == a) && (x < u) then (a < u)
                    variable.upperBounds.forEach { upperBound ->
                        constraints.add(equivalentVar.first.type subtype upperBound.first.type)
                    }

                    // if (x == a) && (x == b) then (a == b)
                    variable.equivalentTo.forEach { otherVar ->
                        if (equivalentVar.first != otherVar.first) {
                            constraints.add(equivalentVar.first.type equal otherVar.first.type)
                        }
                    }
                }

                // if (x < u) && (l < x) then (l > u)
                variable.upperBounds.forEach { upperBound ->
                    variable.lowerBounds.forEach {lowerBound ->
                        constraints.add(lowerBound.first.type subtype upperBound.first.type)
                    }
                }

//            // Run each incorporation rule
//            for (it in incorporationRules) {
//                it.apply {
//                    incorporationContext.incorporate(variable, boundSystem.graph.getAllEdges().map { it.data }.toSet())
//                }
//            }


            }
        }

    }

    private fun resolve(constraints: ConstraintSystem, boundSystem: BoundSystemGraph) {
        // The goal of this method is to generate equality constraints for unresolved variables.
        while (!constraints.anyUnresolved() && boundSystem.getUnresolvedVariables().any()) {
            val toSolve = boundSystem.getUnresolvedVariables().sortedWith {
                    a, b -> a.getDependencies().count() - b.getDependencies().count()
            }.first()
            SimpleConstraintCreator.apply {
                if (toSolve.pushDown) {
                    val types: List<Type> = toSolve.upperBounds.filter { it.first is TypeNode }.map { it.first.type }
                    if (types.size > 1) {
                        constraints.add(toSolve.variable equal InferredGreatestLowerBound(types))
                    } else if (types.size == 1) {
                        constraints.add(toSolve.variable equal types.first())
                    } else {
                        println("Failed to solve: ${toSolve.variable} given $types")
                        constraints.add(toSolve.variable equal InferredBottomType)
                    }
                } else {
                    val types: List<Type> = toSolve.lowerBounds.filter { it.first is TypeNode }.map { it.first.type }
                    if (types.size > 1) {
                        constraints.add(toSolve.variable equal InferredLeastUpperBound(types))
                    } else if (types.size == 1) {
                        constraints.add(toSolve.variable equal types.first())
                    } else {
                        println("Failed to solve: ${toSolve.variable} given $types")
                        constraints.add(toSolve.variable equal InferredTopType)
                    }
                }
            }
        }


    }
}


