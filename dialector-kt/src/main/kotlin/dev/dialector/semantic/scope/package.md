package dev.dialector.semantic.scope

Scoping & Name Resolution is a fairly involved part of language analysis. Dialector uses the Scope Graph concept to
represent program scopes and handle name resolution. It further divides this concept into two concrete providers: 
Scope Definitions and Local Scope Rules

Scope Definitions are intended to represent most scopes. These are well-defined, "publishable" scopes that can be
generated given an argument. The argument will usually be a node in a program graph, but might be a higher-level concept
or nothing at all, in the case of global scope.

Local Scope Rules are designed to represent the complexities of local lexical scoping. In a block, for example, 
define variables are not in scope prior to their definition, but are visible to the following statements. Local Scope 
Rules allow for more flexibility in defining the various nodes in a scope graph and how they interact. However, this
flexibility comes with a cost: scope nodes may be modified by different parts of a program.

Scope Definitions can be more easily referenced and cached, whereas local scopes will be more volatile. Thus local
scopes should only be used for scope nodes represented entirely by a single file/AST. Generally, this level of
complexity only is necessary for file-local code anyways, as ordering & interleaved dependencies is only relevant for
well-ordered code.
