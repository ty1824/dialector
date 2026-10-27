package dev.dialector.cst.gradle

import dev.dialector.cst.generator.CstCodeGenerator
import dev.dialector.cst.generator.CstGeneratorConfig
import dev.dialector.cst.schema.CstSchemaException
import dev.dialector.cst.schema.dsl.CstSchemaParser
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction

/**
 * Cacheable Gradle task that compiles `.cst` schema files into CST Kotlin source files.
 */
@CacheableTask
public abstract class GenerateCstTask : DefaultTask() {

    /**
     * Source directories or `.cst` files to scan.
     */
    @get:Internal
    public abstract val sourceDirectories: ConfigurableFileCollection

    /**
     * Filtered file tree of all `.cst` files found in [sourceDirectories].
     */
    @get:InputFiles
    @get:SkipWhenEmpty
    @get:IgnoreEmptyDirectories
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public val cstSources: FileTree
        get() = sourceDirectories.asFileTree.matching {
            it.include("**/*.cst")
        }

    /**
     * Target directory where generated Kotlin files are written.
     */
    @get:OutputDirectory
    public abstract val outputDirectory: DirectoryProperty

    /**
     * Optional package name override or default.
     */
    @get:Input
    @get:Optional
    public abstract val packageName: Property<String>

    /**
     * Optional schema name override or default.
     */
    @get:Input
    @get:Optional
    public abstract val schemaName: Property<String>

    /**
     * Whether to generate value-class handles for individual tokens.
     */
    @get:Input
    public abstract val generateTokenHandles: Property<Boolean>

    /**
     * Whether to generate value-class handles for node and token categories.
     */
    @get:Input
    public abstract val generateCategoryHandles: Property<Boolean>

    /**
     * Whether to generate tree debug formatting and dump helpers.
     */
    @get:Input
    public abstract val generateDebugHelpers: Property<Boolean>

    /**
     * Whether to generate type-safe `ConcreteSyntaxBuilder` extension functions.
     */
    @get:Input
    public abstract val generateBuilderExtensions: Property<Boolean>

    /**
     * Whether to generate typed tree navigation and visitor helpers.
     */
    @get:Input
    public abstract val generateNavigators: Property<Boolean>

    init {
        group = "dialector"
        description = "Generates CST Kotlin source files from Dialector .cst schema files."
        generateTokenHandles.convention(true)
        generateCategoryHandles.convention(true)
        generateDebugHelpers.convention(true)
        generateBuilderExtensions.convention(true)
        generateNavigators.convention(true)
    }

    @TaskAction
    public fun generate() {
        val outDir = outputDirectory.get().asFile
        if (outDir.exists()) {
            outDir.deleteRecursively()
        }
        outDir.mkdirs()

        val files = cstSources.files.sortedBy { it.invariantSeparatorsPath }
        if (files.isEmpty()) {
            logger.info("No .cst files found to process.")
            return
        }

        val config = CstGeneratorConfig(
            generateTokenHandles = generateTokenHandles.get(),
            generateCategoryHandles = generateCategoryHandles.get(),
            generateDebugHelpers = generateDebugHelpers.get(),
            generateBuilderExtensions = generateBuilderExtensions.get(),
            generateNavigators = generateNavigators.get(),
        )
        val generator = CstCodeGenerator(config)

        val pkgOverride = packageName.orNull?.takeIf { it.isNotBlank() }
        val nameOverride = schemaName.orNull?.takeIf { it.isNotBlank() }

        val errors = mutableListOf<String>()

        for (file in files) {
            try {
                val source = file.readText()
                val fallbackSchemaName = nameOverride
                    ?: file.nameWithoutExtension.replaceFirstChar { it.uppercaseChar() }.ifBlank { "DialectorCst" }
                val fallbackPackageName = pkgOverride ?: "dev.dialector.cst.generated"

                val parsed = CstSchemaParser.parse(
                    source = source,
                    defaultSchemaName = fallbackSchemaName,
                    defaultPackageName = fallbackPackageName,
                )
                val schema = parsed.copy(
                    name = nameOverride ?: parsed.name,
                    packageName = pkgOverride ?: parsed.packageName,
                ).validate()

                val result = generator.generate(schema)
                result.writeTo(outDir)
                logger.info(
                    "Generated ${result.files.size} CST source file(s) for schema '${schema.name}' from ${file.name}"
                )
            } catch (e: CstSchemaException) {
                val detail = e.errors.joinToString("\n") { "  - $it" }
                errors.add("Invalid CST schema in ${file.path}:\n$detail")
            } catch (e: Exception) {
                errors.add("Failed to generate CST from ${file.path}: ${e.message}")
            }
        }

        if (errors.isNotEmpty()) {
            throw GradleException(errors.joinToString("\n\n"))
        }
    }
}
