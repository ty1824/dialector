# dialector
A language workbench for anyone.

Dialector provides a series of libraries and build tools designed to simplify the development of compilers, interpreters, language servers, and IDE tooling in Kotlin.

---

## Modules

- **[`dialector-cst-gradle-plugin`](dialector-cst-gradle-plugin/README.md)**: Gradle plugin (`dev.dialector.cst`) that compiles `.cst` concrete syntax tree schema files into type-safe Kotlin code.
- **`dialector-cst`**: Core runtime library providing immutable `ConcreteSyntaxTree`, `CstNode`, `CstToken`, incremental updates, and builder APIs.
- **`dialector-cst-cli`**: Command-line tool and generator used to compile `.cst` schemas into Kotlin code.
- **`dialector-kt`**: Core semantic modeling and abstract syntax tree library.
- **[`dialector-kt-processor`](dialector-kt-processor/README.md)**: KSP compiler plugin generating node implementations and DSL builders from annotated interfaces.
- **[`dialector-lsp`](dialector-lsp/README.md)**: Language Server Protocol (LSP) adapter for the Dialector framework.

---

## Dialector CST Gradle Plugin Quick Start

The `dev.dialector.cst` Gradle plugin compiles `.cst` grammar schemas into type-safe Kotlin syntax nodes, token handles, visitors, and builders.

### 1. Apply the plugin and runtime dependency

In your `build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm") version "2.4.10" // or kotlin("multiplatform")
    id("dev.dialector.cst") version "0.2.0"
}

dependencies {
    implementation("dev.dialector:dialector-cst:0.2.0")
}
```

### 2. Add a `.cst` schema

Place your `.cst` schema in `src/main/cst` (e.g. `src/main/cst/MyLanguage.cst`):

```cst
schema MyLanguage
package com.example.mylang

token Plus = '+'
token Number = '[0-9]+'
token Identifier = '[a-zA-Z_][a-zA-Z0-9_]*'

category Expr = BinaryExpr | LiteralExpr | VariableExpr

node BinaryExpr {
    left: Expr
    op: Plus
    right: Expr
}

node LiteralExpr {
    value: Number
}

node VariableExpr {
    name: Identifier
}
```

### 3. Build & Generate Sources

Run `./gradlew generateCst` or simply `./gradlew build`. Generated Kotlin source files are automatically placed in `build/generated/source/cst/main/kotlin` and registered with the Kotlin compilation source sets.

For full configuration options, test source setup, and details, see the [Dialector CST Gradle Plugin Documentation](dialector-cst-gradle-plugin/README.md).

---

## Dialector Kotlin AST Quick Start (KSP)

For projects using the annotated interface model with KSP:

Add `dialector-kt` and `dialector-kt-processor` to your dependencies:
```kotlin
val dialectorVersion = "0.2.0"
dependencies {
    api("dev.dialector:dialector-kt:$dialectorVersion")
    ksp("dev.dialector:dialector-kt-processor:$dialectorVersion")
}
```

Add the Kotlin Symbol Processor (KSP) plugin to your Gradle build:
```kotlin
plugins {
    kotlin("jvm") version "2.4.10"
    id("com.google.devtools.ksp") version "2.3.11"
}
```

Add KSP configuration to set the output package for your generated code:
```kotlin
ksp {
    arg("dev.dialector.targetPackage", "org.example.output") // Replace with a package relevant to your project
}
```

---

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
