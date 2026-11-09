# Dialector CST Gradle Plugin (`dev.dialector.cst`)

The **Dialector CST Gradle Plugin** compiles Dialector Concrete Syntax Tree schema files (`.cst`) into type-safe Kotlin source files. It simplifies building compilers, interpreters, language servers, formatters, and IDE tooling by generating strongly typed syntax node handles, token wrappers, category definitions, visitor helpers, debug utilities, and tree builder DSLs.

---

## Features

- **Schema to Kotlin Code Generation**: Compiles declarative `.cst` grammar schemas into zero-overhead Kotlin `value class` wrappers and typed AST/CST abstractions.
- **Incremental & Cacheable**: Implements Gradle's `@CacheableTask` and incremental input tracking for optimal build performance.
- **Isolated Execution**: Runs the generator via Gradle's `WorkerExecutor` with classloader isolation to prevent classpath pollution and memory leaks.
- **Zero-Configuration Defaults**: Automatically detects schemas under `src/main/cst` and `src/test/cst`, placing generated sources in standard build directories.
- **Multiplatform & JVM Ready**: Automatically configures Kotlin JVM, Kotlin Multiplatform (`commonMain`/`commonTest`), and Java source sets.

---

## Installation

Add the plugin to your `build.gradle.kts` (or `build.gradle`):

### Kotlin DSL (`build.gradle.kts`)

```kotlin
plugins {
    kotlin("jvm") version "2.4.10" // or kotlin("multiplatform")
    id("dev.dialector.cst") version "0.2.0"
}

dependencies {
    // Runtime dependency for ConcreteSyntaxTree and CST node abstractions
    implementation("dev.dialector:dialector-cst:0.2.0")
}
```

### Groovy DSL (`build.gradle`)

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '2.4.10'
    id 'dev.dialector.cst' version '0.2.0'
}

dependencies {
    implementation 'dev.dialector:dialector-cst:0.2.0'
}
```

---

## Quick Start

1. Create a schema file at `src/main/cst/SimpleLang.cst`:

```cst
schema SimpleLang
package com.example.simple

token Plus = '+'
token Minus = '-'
token Number = '[0-9]+'
token Identifier = '[a-zA-Z_][a-zA-Z0-9_]*'

category BinaryOp = Plus | Minus
category Expr = BinaryExpr | LiteralExpr | VariableExpr

node BinaryExpr {
    left: Expr
    op: BinaryOp
    right: Expr
}

node LiteralExpr {
    value: Number
}

node VariableExpr {
    name: Identifier
}
```

2. Run your Gradle build or the `generateCst` task:

```bash
./gradlew generateCst
```

3. The generated Kotlin files will be created in `build/generated/source/cst/main/kotlin` and automatically added to the Kotlin compilation classpath. You can immediately use the generated classes:

```kotlin
package com.example.simple

import dev.dialector.cst.ConcreteSyntaxTree

fun analyze(tree: ConcreteSyntaxTree) {
    val root = SimpleLangSyntax(tree.root)
    println("Root node: ${root.dumpTree()}")
}
```

---

## Project Layout & Conventions

By default, the plugin adheres to standard Gradle directory conventions:

| Scope | Schema Source Directory | Output Target Directory | Associated Task |
| :--- | :--- | :--- | :--- |
| **Main** | `src/main/cst` (scans `**/*.cst`) | `build/generated/source/cst/main/kotlin` | `generateCst` |
| **Test** | `src/test/cst` (scans `**/*.cst`) | `build/generated/source/cst/test/kotlin` | `generateTestCst` |

Generated directories are automatically registered into:
- **Kotlin JVM**: `kotlin.sourceSets.main` and `kotlin.sourceSets.test`
- **Kotlin Multiplatform**: `kotlin.sourceSets.commonMain` (or `main`) and `kotlin.sourceSets.commonTest` (or `test`)
- **Java**: `sourceSets.main.java` and `sourceSets.test.java`

---

## Configuration Reference

You can customize code generation by configuring the `dialectorCst` extension block in your build script:

```kotlin
dialectorCst {
    // Custom source directories or files
    sourceDirectories.setFrom("src/main/grammar", "schemas/Core.cst")
    outputDirectory.set(layout.buildDirectory.dir("generated/custom/cst"))

    // Test source configuration
    testSourceDirectories.setFrom("src/test/grammar")
    testOutputDirectory.set(layout.buildDirectory.dir("generated/custom/test-cst"))

    // Package and schema name overrides (defaults come from .cst file headers)
    packageName.set("com.example.generated.cst")
    schemaName.set("MyLanguageCst")

    // Code generation toggles (all default to true)
    generateTokenHandles.set(true)
    generateCategoryHandles.set(true)
    generateDebugHelpers.set(true)
    generateBuilderExtensions.set(true)
    generateNavigators.set(true)

    // Type naming suffixes
    nodeSuffix.set("Syntax")          // Suffix for CST node value classes (e.g. BinaryExprSyntax)
    categorySuffix.set("Syntax")      // Suffix for category value classes (e.g. ExprSyntax)
    tokenSuffix.set("Token")          // Suffix for token value classes (e.g. PlusToken)
    tokenCategorySuffix.set("Token")  // Suffix for token category value classes

    // CLI version override (defaults to the plugin version)
    cliVersion.set("0.2.0")
}
```

### Property Details

| Property | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `sourceDirectories` | `ConfigurableFileCollection` | `src/main/cst` | Source directories or `.cst` files to compile for the main source set. |
| `outputDirectory` | `DirectoryProperty` | `build/generated/source/cst/main/kotlin` | Target directory where main generated Kotlin files are written. |
| `testSourceDirectories` | `ConfigurableFileCollection` | `src/test/cst` | Source directories or `.cst` files to compile for the test source set. |
| `testOutputDirectory` | `DirectoryProperty` | `build/generated/source/cst/test/kotlin` | Target directory where test generated Kotlin files are written. |
| `packageName` | `Property<String>` | None (from file) | Package name fallback or override for generated Kotlin files. |
| `schemaName` | `Property<String>` | None (from file) | Schema name fallback or override for generated Kotlin files. |
| `generateTokenHandles` | `Property<Boolean>` | `true` | Generate value-class handles for individual tokens. |
| `generateCategoryHandles` | `Property<Boolean>` | `true` | Generate value-class handles for node and token categories. |
| `generateDebugHelpers` | `Property<Boolean>` | `true` | Generate tree debug formatting and dump helpers (`dumpTree`). |
| `generateBuilderExtensions` | `Property<Boolean>` | `true` | Generate type-safe `ConcreteSyntaxBuilder` extension functions. |
| `generateNavigators` | `Property<Boolean>` | `true` | Generate typed tree navigation and visitor helpers. |
| `nodeSuffix` | `Property<String>` | `"Syntax"` | Suffix appended to generated CST node value class names. |
| `categorySuffix` | `Property<String>` | `"Syntax"` | Suffix appended to generated CST node category value class names. |
| `tokenSuffix` | `Property<String>` | `"Token"` | Suffix appended to generated CST token value class names. |
| `tokenCategorySuffix` | `Property<String>` | `"Token"` | Suffix appended to generated CST token category value class names. |
| `cliVersion` | `Property<String>` | Plugin version | Version of `dev.dialector:dialector-cst-cli` artifact used for compilation. |
| `cliClasspath` | `ConfigurableFileCollection` | `dialectorCstCli` configuration | Custom classpath for running the CLI generator tool. |

---

## Tasks

| Task Name | Group | Description |
| :--- | :--- | :--- |
| `generateCst` | `dialector` | Compiles main `.cst` schemas into Kotlin source files. |
| `generateTestCst` | `dialector` | Compiles test `.cst` schemas into Kotlin source files. |

Both tasks are configured to run automatically before `compileKotlin`, `compileTestKotlin`, and `compileJava`.

---

## Links & Resources

- **Project Repository**: [GitHub (ty1824/dialector)](https://github.com/ty1824/dialector)
- **Issue Tracker**: [GitHub Issues](https://github.com/ty1824/dialector/issues)
- **Documentation**: [Dialector Documentation](https://github.com/ty1824/dialector#readme)
- **License**: [GNU General Public License v3.0](https://github.com/ty1824/dialector/blob/main/LICENSE)
