import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes

class DialectorCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = "dialector.modelgen"
    override val pluginOptions: Collection<CliOption> = listOf((
        CliOption("enabled", "<true|false>", "whether plugin is enabled"),
        CliOption(
            "debugLogAnnotation", "<fqname>", "debug-log annotation names",
            required = true, allowMultipleOccurrences = true))
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        super.processOption(option, value, configuration)
    }
}



class DebugLogClassGenerationInterceptor(
    val debugLogAnnotations: List<String>
) : ClassBuilderInterceptorExtension {
    override fun interceptClassBuilderFactory(
        interceptedFactory: ClassBuilderFactory,
        bindingContext: BindingContext,
        diagnostics: DiagnosticSink
    ): ClassBuilderFactory = TODO()
}

private class DebugLogClassBuilder(
    val annotations: List<String>,
    delegateBuilder: ClassBuilder
) : DelegatingClassBuilder(delegateBuilder) {

    override fun newMethod(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val original = super.newMethod(origin, access, name, desc, signature, exceptions)
        val function = origin.descriptor as? FunctionDescriptor ?: return original
        if (annotations.none { function.annotations.hasAnnotation(it) }) {
            return original
        }
        return object : MethodVisitor(Opcodes.ASM5, original) {
        }
    }
}