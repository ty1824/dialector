package dev.dialector.cst.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.file
import dev.dialector.cst.generator.CstCodeGenerator
import dev.dialector.cst.generator.CstGeneratorConfig
import dev.dialector.cst.generator.CstGeneratorResult
import dev.dialector.cst.schema.CstSchema
import dev.dialector.cst.schema.CstSchemaException
import dev.dialector.cst.schema.dsl.CstSchemaParser
import java.io.File

/**
 * Root command for the Dialector CST CLI (`dialector-cst`).
 */
public class DialectorCstCli : CliktCommand(name = "dialector-cst") {
    init {
        subcommands(
            GenerateCommand(),
            ValidateCommand(),
        )
    }

    override fun help(context: Context): String =
        "Command-line interface for the Dialector Flat CST schema validator and code generator."

    override fun run(): Unit = Unit
}

/**
 * Subcommand that parses `.cst` schema files/directories and generates Kotlin CST source files.
 */
public class GenerateCommand : CliktCommand(name = "generate") {
    override fun help(context: Context): String =
        "Generate Flat CST Kotlin source files from .cst schema files or directories."

    private val inputs: List<File> by argument(
        name = "inputs",
        help = "One or more .cst schema files or directories containing .cst files.",
    ).file(mustExist = true, canBeFile = true, canBeDir = true, mustBeReadable = true).multiple(required = true)

    private val outputDir: File by option(
        "-o",
        "--output",
        help = "Output directory where generated Kotlin source files will be written.",
    ).file(canBeFile = false, canBeDir = true).default(File("build/generated/source/cst/main/kotlin"))

    private val packageNameOverride: String? by option(
        "-p",
        "--package",
        help = "Override or default package name for generated sources.",
    )

    private val schemaNameOverride: String? by option(
        "-s",
        "--schema-name",
        help = "Override or default schema name (used if not specified in the .cst file).",
    )

    private val generateTokenHandles: Boolean by option(
        "--token-handles",
        help = "Generate value-class handles for individual tokens.",
    ).boolean().default(true)

    private val generateCategoryHandles: Boolean by option(
        "--category-handles",
        help = "Generate value-class handles for node and token categories.",
    ).boolean().default(true)

    private val generateDebugHelpers: Boolean by option(
        "--debug-helpers",
        help = "Generate tree debug formatting and dump helpers.",
    ).boolean().default(true)

    private val generateBuilderExtensions: Boolean by option(
        "--builder-extensions",
        help = "Generate type-safe FlatCstBuilder extension functions.",
    ).boolean().default(true)

    private val generateNavigators: Boolean by option(
        "--navigators",
        help = "Generate typed tree navigation and visitor helpers.",
    ).boolean().default(true)

    private val cleanOutput: Boolean by option(
        "--clean",
        help = "Delete existing contents of the output directory before generating.",
    ).flag(default = false)

    override fun run() {
        val cstFiles = collectCstFiles(inputs)
        if (cstFiles.isEmpty()) {
            echo("No .cst schema files found in the specified inputs.", err = true)
            throw ProgramResult(1)
        }

        if (cleanOutput && outputDir.exists()) {
            outputDir.deleteRecursively()
        }
        outputDir.mkdirs()

        val config = CstGeneratorConfig(
            generateTokenHandles = generateTokenHandles,
            generateCategoryHandles = generateCategoryHandles,
            generateDebugHelpers = generateDebugHelpers,
            generateBuilderExtensions = generateBuilderExtensions,
            generateNavigators = generateNavigators,
        )
        val generator = CstCodeGenerator(config)

        var hasErrors = false
        val generatedFiles = mutableListOf<File>()

        for (file in cstFiles) {
            try {
                val result = compileCstFile(
                    file = file,
                    outputDir = outputDir,
                    generator = generator,
                    schemaNameOverride = schemaNameOverride,
                    packageNameOverride = packageNameOverride,
                )
                for (genFile in result.files) {
                    val written = genFile.writeTo(outputDir)
                    generatedFiles.add(written)
                }
                echo("Generated ${result.files.size} file(s) for schema '${result.schema.name}' from ${file.name}")
            } catch (e: CstSchemaException) {
                hasErrors = true
                echo("Error in ${file.path}:", err = true)
                for (err in e.errors) {
                    echo("  - $err", err = true)
                }
            } catch (e: Exception) {
                hasErrors = true
                echo("Failed to process ${file.path}: ${e.message}", err = true)
            }
        }

        if (hasErrors) {
            throw ProgramResult(1)
        }
    }
}

/**
 * Subcommand that parses and validates `.cst` schema files without writing code.
 */
public class ValidateCommand : CliktCommand(name = "validate") {
    override fun help(context: Context): String =
        "Parse and validate .cst schema files or directories."

    private val inputs: List<File> by argument(
        name = "inputs",
        help = "One or more .cst schema files or directories containing .cst files.",
    ).file(mustExist = true, canBeFile = true, canBeDir = true, mustBeReadable = true).multiple(required = true)

    private val packageNameOverride: String? by option(
        "-p",
        "--package",
        help = "Default package name if not declared in the .cst file.",
    )

    private val schemaNameOverride: String? by option(
        "-s",
        "--schema-name",
        help = "Default schema name if not declared in the .cst file.",
    )

    override fun run() {
        val cstFiles = collectCstFiles(inputs)
        if (cstFiles.isEmpty()) {
            echo("No .cst schema files found in the specified inputs.", err = true)
            throw ProgramResult(1)
        }

        var hasErrors = false
        for (file in cstFiles) {
            try {
                val schema = parseCstFile(
                    file = file,
                    schemaNameOverride = schemaNameOverride,
                    packageNameOverride = packageNameOverride,
                )
                echo(
                    "Valid schema '${schema.name}' (${schema.packageName}) in ${file.name}: " +
                        "${schema.nodes.size} nodes, ${schema.categories.size} categories, " +
                        "${schema.tokens.size} tokens, ${schema.tokenCategories.size} token categories"
                )
            } catch (e: CstSchemaException) {
                hasErrors = true
                echo("Invalid schema in ${file.path}:", err = true)
                for (err in e.errors) {
                    echo("  - $err", err = true)
                }
            } catch (e: Exception) {
                hasErrors = true
                echo("Failed to validate ${file.path}: ${e.message}", err = true)
            }
        }

        if (hasErrors) {
            throw ProgramResult(1)
        }
    }
}

/**
 * Discovers all `.cst` files from a list of input files or directories in deterministic order.
 */
public fun collectCstFiles(inputs: Iterable<File>): List<File> {
    val result = linkedSetOf<File>()
    for (input in inputs) {
        if (input.isFile) {
            result.add(input.canonicalFile)
        } else if (input.isDirectory) {
            input.walkTopDown()
                .filter { it.isFile && it.extension.equals("cst", ignoreCase = true) }
                .sortedBy { it.invariantSeparatorsPath }
                .forEach { result.add(it.canonicalFile) }
        }
    }
    return result.toList()
}

/**
 * Parses and validates a single `.cst` file into a [CstSchema].
 */
public fun parseCstFile(
    file: File,
    schemaNameOverride: String? = null,
    packageNameOverride: String? = null,
): CstSchema {
    val source = file.readText()
    val fallbackSchemaName = schemaNameOverride
        ?: file.nameWithoutExtension.replaceFirstChar { it.uppercaseChar() }.ifBlank { "DialectorCst" }
    val fallbackPackageName = packageNameOverride ?: "dev.dialector.cst.generated"

    val parsed = CstSchemaParser.parse(
        source = source,
        defaultSchemaName = fallbackSchemaName,
        defaultPackageName = fallbackPackageName,
    )

    val finalSchema = parsed.copy(
        name = schemaNameOverride ?: parsed.name,
        packageName = packageNameOverride ?: parsed.packageName,
    )
    return finalSchema.validate()
}

/**
 * Parses a `.cst` file and runs [CstCodeGenerator.generate] on the resulting schema.
 */
public fun compileCstFile(
    file: File,
    outputDir: File,
    generator: CstCodeGenerator = CstCodeGenerator(),
    schemaNameOverride: String? = null,
    packageNameOverride: String? = null,
): CstGeneratorResult {
    val schema = parseCstFile(
        file = file,
        schemaNameOverride = schemaNameOverride,
        packageNameOverride = packageNameOverride,
    )
    return generator.generate(schema)
}

public fun main(args: Array<String>) {
    DialectorCstCli().main(args)
}
