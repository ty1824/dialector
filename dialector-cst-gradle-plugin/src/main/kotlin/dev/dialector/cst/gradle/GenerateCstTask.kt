package dev.dialector.cst.gradle

import dev.dialector.cst.cli.generateCstSources
import dev.dialector.cst.generator.CstGeneratorConfig
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
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
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

/**
 * Cacheable Gradle task that compiles `.cst` schema files into CST Kotlin source files
 * by submitting work actions to Gradle's [WorkerExecutor] with classloader isolation.
 */
@CacheableTask
abstract class GenerateCstTask : DefaultTask() {

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor

    /**
     * Classpath containing the dialector-cst generator and its dependencies.
     * When not explicitly set, defaults to the `dialectorCstCli` configuration.
     */
    @get:Classpath
    @get:Optional
    abstract val cliClasspath: ConfigurableFileCollection

    /**
     * Source directories or `.cst` files to scan.
     */
    @get:Internal
    abstract val sourceDirectories: ConfigurableFileCollection

    /**
     * Filtered file tree of all `.cst` files found in [sourceDirectories].
     */
    @get:InputFiles
    @get:SkipWhenEmpty
    @get:IgnoreEmptyDirectories
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val cstSources: FileTree
        get() = sourceDirectories.asFileTree.matching {
            it.include("**/*.cst")
        }

    /**
     * Target directory where generated Kotlin files are written.
     */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    /**
     * Optional package name override or default.
     */
    @get:Input
    @get:Optional
    abstract val packageName: Property<String>

    /**
     * Optional schema name override or default.
     */
    @get:Input
    @get:Optional
    abstract val schemaName: Property<String>

    /**
     * Whether to generate value-class handles for individual tokens.
     */
    @get:Input
    abstract val generateTokenHandles: Property<Boolean>

    /**
     * Whether to generate value-class handles for node and token categories.
     */
    @get:Input
    abstract val generateCategoryHandles: Property<Boolean>

    /**
     * Whether to generate tree debug formatting and dump helpers.
     */
    @get:Input
    abstract val generateDebugHelpers: Property<Boolean>

    /**
     * Whether to generate type-safe `ConcreteSyntaxBuilder` extension functions.
     */
    @get:Input
    abstract val generateBuilderExtensions: Property<Boolean>

    /**
     * Whether to generate typed tree navigation and visitor helpers.
     */
    @get:Input
    abstract val generateNavigators: Property<Boolean>

    /**
     * Suffix appended to generated CST node value class names.
     */
    @get:Input
    abstract val nodeSuffix: Property<String>

    /**
     * Suffix appended to generated CST node category value class names.
     */
    @get:Input
    abstract val categorySuffix: Property<String>

    /**
     * Suffix appended to generated CST token value class names.
     */
    @get:Input
    abstract val tokenSuffix: Property<String>

    /**
     * Suffix appended to generated CST token category value class names.
     */
    @get:Input
    abstract val tokenCategorySuffix: Property<String>

    init {
        group = "dialector"
        description = "Generates CST Kotlin source files from Dialector .cst schema files via Gradle WorkerExecutor."
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

    @TaskAction
    fun generate() {
        val files = cstSources.files.sortedBy { it.invariantSeparatorsPath }
        if (files.isEmpty()) {
            logger.info("No .cst files found to process.")
            return
        }

        val workQueue = workerExecutor.classLoaderIsolation { spec ->
            spec.classpath.from(cliClasspath)
        }

        workQueue.submit(GenerateCstWorkAction::class.java) { params ->
            params.cstSources.from(files)
            params.outputDirectory.set(outputDirectory)
            params.packageName.set(packageName)
            params.schemaName.set(schemaName)
            params.generateTokenHandles.set(generateTokenHandles)
            params.generateCategoryHandles.set(generateCategoryHandles)
            params.generateDebugHelpers.set(generateDebugHelpers)
            params.generateBuilderExtensions.set(generateBuilderExtensions)
            params.generateNavigators.set(generateNavigators)
            params.nodeSuffix.set(nodeSuffix)
            params.categorySuffix.set(categorySuffix)
            params.tokenSuffix.set(tokenSuffix)
            params.tokenCategorySuffix.set(tokenCategorySuffix)
        }
    }
}

/**
 * Parameters for [GenerateCstWorkAction].
 */
interface GenerateCstWorkParameters : WorkParameters {
    val cstSources: ConfigurableFileCollection
    val outputDirectory: DirectoryProperty
    val packageName: Property<String>
    val schemaName: Property<String>
    val generateTokenHandles: Property<Boolean>
    val generateCategoryHandles: Property<Boolean>
    val generateDebugHelpers: Property<Boolean>
    val generateBuilderExtensions: Property<Boolean>
    val generateNavigators: Property<Boolean>
    val nodeSuffix: Property<String>
    val categorySuffix: Property<String>
    val tokenSuffix: Property<String>
    val tokenCategorySuffix: Property<String>
}

/**
 * Converts [GenerateCstWorkParameters] to a [CstGeneratorConfig].
 */
fun GenerateCstWorkParameters.toGeneratorConfig(): CstGeneratorConfig = CstGeneratorConfig(
    generateTokenHandles = generateTokenHandles.get(),
    generateCategoryHandles = generateCategoryHandles.get(),
    generateDebugHelpers = generateDebugHelpers.get(),
    generateBuilderExtensions = generateBuilderExtensions.get(),
    generateNavigators = generateNavigators.get(),
    nodeSuffix = nodeSuffix.get(),
    categorySuffix = categorySuffix.get(),
    tokenSuffix = tokenSuffix.get(),
    tokenCategorySuffix = tokenCategorySuffix.get(),
)

/**
 * Work action executing CST code generation inside Gradle's worker queue.
 */
abstract class GenerateCstWorkAction : WorkAction<GenerateCstWorkParameters> {

    override fun execute() {
        val outDir = parameters.outputDirectory.get().asFile
        val files = parameters.cstSources.files.sortedBy { it.invariantSeparatorsPath }
        if (files.isEmpty()) return

        val result = generateCstSources(
            files = files,
            outputDir = outDir,
            config = parameters.toGeneratorConfig(),
            schemaNameOverride = parameters.schemaName.orNull?.takeIf { it.isNotBlank() },
            packageNameOverride = parameters.packageName.orNull?.takeIf { it.isNotBlank() },
        )

        if (!result.isSuccess) {
            throw GradleException(result.errors.joinToString("\n"))
        }
    }
}
