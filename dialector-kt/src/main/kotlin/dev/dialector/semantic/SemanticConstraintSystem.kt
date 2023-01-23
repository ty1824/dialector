package dev.dialector.semantic

import dev.dialector.semantic.type.Type
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
public interface SemanticVariable

/**
 * A constraint on one or more elements in the semantic constraint system.
 */
public interface SemanticConstraint

public interface Origin

public interface ConstraintCreator

public interface SemanticRule<T : Node> {
    public val name: String
    public val isValidFor: NodeClause<T>
    public val rule: SemanticRuleContext.(node: T) -> Unit

    @Suppress("UNCHECKED_CAST")
    public operator fun invoke(context: SemanticRuleContext, node: Node) {
        if (isValidFor(node)) context.rule(node as T)
    }
}

public fun <T : Node> NodeClause<T>.evaluateSemantics(name: String, semantics: SemanticRuleContext.(node: T) -> Unit): SemanticRule<T> =
    object : SemanticRule<T> {
        override val name = name
        override val isValidFor = this@evaluateSemantics
        override val rule = semantics
    }

public interface Namespace {
    public val name: String
}

public class SimpleNamespace(override val name: String): Namespace


/**
 * A constraint indicating that a type variable's value should be assigned to the result
 * of a type scope lookup.
 */
public data class ReferencedTypeConstraint(
    val scope: ScopeVariable,
    val namespace: Namespace,
    val reference: NodeReference<out Node>,
    val variable: TypeVariable
) : SemanticConstraint

/**
 * A constraint indicating that a scope should inherit elements from the
 * given type's published scope.
 */
public data class TypeScopeConstraint(
    /** The scope that will inherit from the type's scope. */
    val scope: ScopeVariable,
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
public data class DeclareElementTypeConstraint(
    val scope: ScopeVariable,
    val namespace: Namespace,
    val name: String,
    val type: Type
) : SemanticConstraint

public object TypeScopes : ConstraintCreator {
    public fun ScopeVariable.referenceType(namespace: Namespace, target: NodeReference<out Node>, variable: TypeVariable): ReferencedTypeConstraint =
        ReferencedTypeConstraint(this, namespace, target, variable)

    public fun ScopeVariable.inheritTypeScope(type: Type, label: String): TypeScopeConstraint =
        TypeScopeConstraint(this, type, label)

    public fun ScopeVariable.declareTypeElement(namespace: Namespace, name: String, type: Type): DeclareElementTypeConstraint =
        DeclareElementTypeConstraint(this, namespace, name, type)
}

public interface SemanticRuleContext {

    public val node: Node?

    /**
     * Creates a new [ScopeVariable].
     */
    public fun scope(name: String): ScopeVariable

    /**
     * Propagates a given [ScopeVariable] to a [Node].
     *
     * @param scope The [ScopeVariable] to propagate
     * @param node The [Node] to propagate the scope to.
     * @param type The [PropagationType] of scope being propagated. Defaults to [Parent]
     */
    public fun propagateScope(scope: ScopeVariable, node: Node, type: PropagationType = Parent)

    /**
     * Receives a propagated [ScopeVariable] of the given type.
     */
    public fun receiveScope(type: PropagationType = Parent): ScopeVariable

    /**
     * Creates a new [TypeVariable]
     */
    public fun typeVar(): TypeVariable

    /**
     * Creates or retrieves the [TypeVariable] for the given node.
     */
    public fun typeOf(node: Node): TypeVariable

    public fun propagateTypeScope(scope: ScopeVariable, type: Type)

    /**
     * Retrieves the [ScopeVariable] associated with a [Type]
     */
    public fun typeScope(type: Type): ScopeVariable

    /**
     * Creates a new [SemanticConstraint] using the specified creator.
     */
    public fun <T : ConstraintCreator> constraint(creator: T, routine: T.() -> SemanticConstraint)
}

public class SemanticConstraintSystem(public val constraints: Map<Node, List<SemanticConstraint>>)

/**
 * A clause that matches against [SemanticConstraint]s.
 */
public interface ConstraintClause<T : SemanticConstraint> : TypesafeClause<T> {
    @Suppress("UNCHECKED_CAST")
    public operator fun invoke(candidate: SemanticConstraint): Boolean =
        clauseClass.isInstance(candidate) && constraint(candidate as T)
}

/**
 * Creates a [ConstraintClause] that matches against a specific [SemanticConstraint] instance.
 */
public fun <T : SemanticConstraint> given(forConstraint: T): ConstraintClause<T> =
    object : InstanceClause<T>(forConstraint), ConstraintClause<T> {
        override fun invoke(candidate: SemanticConstraint): Boolean = forConstraint == candidate
    }

/**
 * Creates a [ConstraintClause] that matches against a subclass of [SemanticConstraint].
 */
public inline fun <reified T : SemanticConstraint> given(): ConstraintClause<T> =
    object : ClassifierClause<T>(T::class), ConstraintClause<T> {
        override fun invoke(candidate: SemanticConstraint): Boolean = clauseClass.isInstance(candidate)
    }

/**
 * Creates a [ConstraintClause] that matches constraints against a given predicate.
 */
public inline fun <reified T : SemanticConstraint> given(crossinline predicate: (T) -> Boolean): ConstraintClause<T> =
    object : ConstraintClause<T> {
        override val clauseClass = T::class
        override fun constraint(candidate: T): Boolean = predicate(candidate)
    }

public interface ReductionContext {
    public fun <T : ConstraintCreator> constraint(creator: T, routine: T.() -> SemanticConstraint)
    public fun bound(routine: BoundCreator.() -> Bound)
    public fun scoping(routine: ScopeGraph.() -> Unit)
}

public interface ReductionRule<T : SemanticConstraint> {
    public val name: String
    public val isValidFor: ConstraintClause<T>
    public val reduce: ReductionContext.(constraint: T) -> Unit
}

public class ConstraintSolver {
    public fun reduce() {

    }

    public fun incorporate() {}
}

public typealias RelationalConstraintClause = (TypeRelationConstraint) -> Boolean
public typealias ReductionRoutine = ReductionContext.(constraint: TypeRelationConstraint) -> Unit

private class SimpleReductionRule<T : SemanticConstraint>(
    override val name: String,
    override val isValidFor: ConstraintClause<T>,
    override val reduce: ReductionContext.(T) -> Unit
) : ReductionRule<T>

public fun <T : SemanticConstraint> ConstraintClause<T>.reducesTo(name: String, routine: ReductionContext.(T) -> Unit): ReductionRule<T> =
    SimpleReductionRule(name, this, routine)

public val redundantElimination: ReductionRule<TypeRelationConstraint> =
    given<TypeRelationConstraint> { it.left == it.right }.reducesTo("identity") {
        /* Nothing */
    }

public val leftReduction: ReductionRule<TypeRelationConstraint> =
    given<TypeRelationConstraint> { it.left is TypeVariable }.reducesTo("leftReduction") {
        bound { BaseBound(it.relation, it.left as TypeVariable, it.right)}
    }
public val rightReduction: ReductionRule<TypeRelationConstraint> =
    given<TypeRelationConstraint> { it.right is TypeVariable }.reducesTo("rightReduction") {
        bound { BaseBound(it.relation.opposite(), it.right as TypeVariable, it.left)}
    }

public val inheritScope: ReductionRule<InheritScopeConstraint> =
    given<InheritScopeConstraint>().reducesTo("inheritScope") {
        scoping { inherit(it.scope, it.inheritFrom, it.label) }
    }

public val declareElement: ReductionRule<DeclareElementConstraint> =
    given<DeclareElementConstraint>().reducesTo("declareElement") {
        scoping { declare(it.scope, it.element, it.name, it.namespace) }
    }

public val aliasElement: ReductionRule<AliasElementConstraint> =
    given<AliasElementConstraint>().reducesTo("aliasElement") {
        TODO("Need to implement aliasing")
    }

public val referenceIdentifier: ReductionRule<ReferenceIdentifierConstraint> =
    given<ReferenceIdentifierConstraint>().reducesTo("referenceIdentifier") {
        scoping { reference(it.reference, it.scope, it.namespace) }
    }

public val declareTypeElement: ReductionRule<DeclareElementTypeConstraint> =
    given<DeclareElementTypeConstraint>().reducesTo("declareElementType") {

    }

public interface Bound {
    val relation: TypeRelation
    val variable: TypeVariable
    val boundingType: Type

    fun lowerType(): Type = if (relation == TypeRelation.SUBTYPE || relation == TypeRelation.EQUIVALENT) variable else boundingType
    fun upperType(): Type = if (relation == TypeRelation.SUBTYPE || relation == TypeRelation.EQUIVALENT) boundingType else variable
}

public data class BaseBound(
    override val relation: TypeRelation,
    override val variable: TypeVariable,
    override val boundingType: Type,
) : Bound

/*
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
 */

