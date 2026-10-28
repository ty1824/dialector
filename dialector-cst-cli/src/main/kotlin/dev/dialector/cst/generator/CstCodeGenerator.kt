package dev.dialector.cst.generator

import com.squareup.kotlinpoet.FileSpec
import dev.dialector.cst.schema.CstSchema
import java.io.File
import java.nio.file.Path

/**
 * Configuration options for CST code generation.
 */
public data class CstGeneratorConfig(
    public val generateTokenHandles: Boolean = true,
    public val generateCategoryHandles: Boolean = true,
    public val generateDebugHelpers: Boolean = true,
    public val generateBuilderExtensions: Boolean = true,
    public val generateNavigators: Boolean = true,
    public val generateValidators: Boolean = true,
    public val nodeSuffix: String = "Syntax",
    public val categorySuffix: String = "Syntax",
    public val tokenSuffix: String = "Token",
    public val tokenCategorySuffix: String = "Token",
)

/**
 * Represents a single generated Kotlin source file backed by a KotlinPoet [FileSpec].
 */
public data class GeneratedSourceFile(
    public val fileName: String,
    public val packageName: String,
    public val content: String,
    public val fileSpec: FileSpec? = null,
) {
    /**
     * Writes this file into the target source [directory].
     */
    public fun writeTo(directory: File): File {
        val packageDir = if (packageName.isEmpty()) {
            directory
        } else {
            File(directory, packageName.replace('.', File.separatorChar))
        }
        packageDir.mkdirs()
        val file = File(packageDir, fileName)
        file.writeText(content)
        return file
    }

    /**
     * Writes this file into the target source [directory].
     */
    public fun writeTo(directory: Path): Path =
        writeTo(directory.toFile()).toPath()
}

/**
 * Result bundle containing all generated files from a [CstSchema].
 */
public data class CstGeneratorResult(
    public val files: List<GeneratedSourceFile>,
    public val schema: CstSchema,
) {
    /**
     * Returns the underlying KotlinPoet [FileSpec]s for all generated files.
     */
    public val fileSpecs: List<FileSpec>
        get() = files.mapNotNull { it.fileSpec }

    /**
     * Writes all generated files into the target source [directory].
     */
    public fun writeTo(directory: File): List<File> =
        files.map { it.writeTo(directory) }

    /**
     * Writes all generated files into the target source [directory].
     */
    public fun writeTo(directory: Path): List<Path> =
        writeTo(directory.toFile()).map { it.toPath() }

    /**
     * Returns the combined code string of all generated files.
     */
    public fun getCombinedSource(): String =
        files.joinToString("\n\n// ==========================================\n\n") {
            "// File: ${it.fileName}\n${it.content}"
        }
}

/**
 * Generator that transforms a [CstSchema] into high-performance, flat-tree Kotlin code
 * with value-class handles and context-parameter getters using [CstKotlinPoetGenerator].
 */
public class CstCodeGenerator(
    public val config: CstGeneratorConfig = CstGeneratorConfig(),
) {
    private val poetGenerator = CstKotlinPoetGenerator(config)

    /**
     * Generates Kotlin source files from the provided [schema] via KotlinPoet [FileSpec]s.
     */
    public fun generate(schema: CstSchema): CstGeneratorResult {
        val specs = poetGenerator.generateFileSpecs(schema)
        val files = specs.map { spec ->
            val sb = StringBuilder()
            spec.writeTo(sb)
            GeneratedSourceFile(
                fileName = "${spec.name}.kt",
                packageName = spec.packageName,
                content = sb.toString(),
                fileSpec = spec,
            )
        }
        return CstGeneratorResult(files = files, schema = schema)
    }

    /**
     * Directly returns the generated KotlinPoet [FileSpec]s for [schema].
     */
    public fun generateFileSpecs(schema: CstSchema): List<FileSpec> =
        poetGenerator.generateFileSpecs(schema)
}
