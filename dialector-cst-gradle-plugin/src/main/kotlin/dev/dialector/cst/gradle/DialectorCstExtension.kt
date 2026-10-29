package dev.dialector.cst.gradle

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Gradle project extension (`dialectorCst { ... }`) for configuring CST schema code generation.
 */
abstract class DialectorCstExtension @Inject constructor(
    objects: ObjectFactory,
) {
    /**
     * Source directories or individual `.cst` schema files to compile for the main source set.
     * Defaults to `src/main/cst`.
     */
    val sourceDirectories: ConfigurableFileCollection = objects.fileCollection()

    /**
     * Target directory where generated main Kotlin files are written.
     * Defaults to `build/generated/source/cst/main/kotlin`.
     */
    abstract val outputDirectory: DirectoryProperty

    /**
     * Source directories or individual `.cst` schema files to compile for the test source set.
     * Defaults to `src/test/cst`.
     */
    val testSourceDirectories: ConfigurableFileCollection = objects.fileCollection()

    /**
     * Target directory where generated test Kotlin files are written.
     * Defaults to `build/generated/source/cst/test/kotlin`.
     */
    abstract val testOutputDirectory: DirectoryProperty

    /**
     * Optional package name override (or default if a `.cst` file omits the `package` header).
     */
    abstract val packageName: Property<String>

    /**
     * Optional schema name override (or default if a `.cst` file omits the `schema` header).
     */
    abstract val schemaName: Property<String>

    /**
     * Whether to generate value-class handles for individual tokens. Defaults to `true`.
     */
    abstract val generateTokenHandles: Property<Boolean>

    /**
     * Whether to generate value-class handles for node and token categories. Defaults to `true`.
     */
    abstract val generateCategoryHandles: Property<Boolean>

    /**
     * Whether to generate tree debug formatting and dump helpers. Defaults to `true`.
     */
    abstract val generateDebugHelpers: Property<Boolean>

    /**
     * Whether to generate type-safe `ConcreteSyntaxBuilder` extension functions. Defaults to `true`.
     */
    abstract val generateBuilderExtensions: Property<Boolean>

    /**
     * Whether to generate typed tree navigation and visitor helpers. Defaults to `true`.
     */
    abstract val generateNavigators: Property<Boolean>

    /**
     * Suffix appended to generated CST node value class names. Defaults to `"Syntax"`.
     */
    abstract val nodeSuffix: Property<String>

    /**
     * Suffix appended to generated CST node category value class names. Defaults to `"Syntax"`.
     */
    abstract val categorySuffix: Property<String>

    /**
     * Suffix appended to generated CST token value class names. Defaults to `"Token"`.
     */
    abstract val tokenSuffix: Property<String>

    /**
     * Suffix appended to generated CST token category value class names. Defaults to `"Token"`.
     */
    abstract val tokenCategorySuffix: Property<String>

    /**
     * Optional custom classpath for the dialector-cst CLI tool.
     * Defaults to the `dialectorCstCli` configuration.
     */
    val cliClasspath: ConfigurableFileCollection = objects.fileCollection()

    init {
        generateTokenHandles.convention(true)
        generateCategoryHandles.convention(true)
        generateDebugHelpers.convention(true)
        generateBuilderExtensions.convention(true)
        generateNavigators.convention(true)
        nodeSuffix.convention("Syntax")
        categorySuffix.convention("Syntax")
        tokenSuffix.convention("Token")
        tokenCategorySuffix.convention("Token")
    }
}
