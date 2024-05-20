package dev.hsbrysk.protoc.gen

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest

enum class CompileOption(private val option: String) {
    /**
     * Whether to generate a factory (default: on)
     */
    FACTORY("factory"),

    /**
     * Whether to generate `orNull` extension functions for optional scalar fields (default: on)
     */
    OR_NULL_GETTER("orNullGetter"),

    /**
     * Whether to generate `orNull` extension functions for optional message fields (default: off)
     * Not needed when using protobuf-kotlin.
     */
    MESSAGE_OR_NULL_GETTER("messageOrNullGetter"),
    ;

    companion object {
        fun parseOptions(request: CodeGeneratorRequest): Set<CompileOption> {
            val result = mutableSetOf(FACTORY, OR_NULL_GETTER)

            // `request.parameter` will be a comma-separated list like "name1+,name2-".
            // The suffix `+`/`-` toggles enabled/disabled.
            // (I actually wanted to use a prefix, but using `-` as a prefix didn't work)
            request.parameter.split(",").map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { parameter ->
                    require(parameter.endsWith("+") || parameter.endsWith("-")) {
                        "option name must be followed by + or -"
                    }
                    val isOn = parameter.takeLast(1) == "+"
                    val option = parameter.dropLast(1)

                    // TODO: Since `Enum.entries` was added in Kotlin 1.8.20 and is relatively new, we won't use it yet.
                    // https://zenn.dev/maxfie1d/articles/b121c8893ea759
                    @Suppress("EnumValuesSoftDeprecate")
                    val optionEnum = requireNotNull(CompileOption.values().singleOrNull { it.option == option }) {
                        "invalid option name"
                    }
                    optionEnum to isOn
                }
                .forEach {
                    if (it.second) {
                        result.add(it.first)
                    } else {
                        result.remove(it.first)
                    }
                }

            return result
        }
    }
}

fun CompileOption.generator(): Generator {
    return when (this) {
        CompileOption.FACTORY -> FactoryGenerator()
        CompileOption.OR_NULL_GETTER -> OrNullGetterGenerator()
        CompileOption.MESSAGE_OR_NULL_GETTER -> MessageOrNullGetterGenerator()
    }
}
