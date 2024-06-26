package dev.hsbrysk.protoc.gen.util

/**
 * Convert kebab case or snake case to Pascal case (a.k.a. upper camel case).
 */
internal fun String.pascalCase(): String = hyphenToUnderscore().split("_").joinToString("") { value ->
    value.replaceFirstChar { it.uppercase() }
}

/**
 * Convert kebab case or snake case to camel case.
 */
internal fun String.camelCase(): String =
    hyphenToUnderscore().split("_").withIndex().joinToString("") { (index, value) ->
        if (index == 0) value.replaceFirstChar { it.lowercase() } else value.replaceFirstChar { it.uppercase() }
    }

// We want to support both `-` and `_`, so...
private fun String.hyphenToUnderscore() = replace("-", "_")
