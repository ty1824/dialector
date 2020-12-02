package dev.dialector.model

Key assertions that Dialector makes about models:

1) Models must be generically traversable. A consumer of a model should be able to traverse a tree without
knowledge of the concrete node types.

2) Contrasting with 1), models will generally be consumed by definition-aware code. This means that models
should be optimized to support this use case. Generic traversals should be implemented on top of the core
model definition rather than the opposite.

Combining 1 and 2, the resulting models should use language-level constructs where possible (e.g. fields) and
wrap generic traversal support around them. The other approach would suggest using data-level constructs
(e.g. Maps & other data structures) for the model's structure & expose language-level definitions as a convenience.
If Dialector becomes a cross-language specification, this distinction may be reconsidered.