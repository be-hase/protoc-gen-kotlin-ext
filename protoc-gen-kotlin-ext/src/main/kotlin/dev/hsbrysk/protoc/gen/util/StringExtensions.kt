package dev.hsbrysk.protoc.gen.util

/**
 * Convert kebab case or snake case to Pascal case (a.k.a. upper camel case).
 */
internal fun String.pascalCase(): String = buildString {
    var capitalizeNext = true
    for (c in hyphenToUnderscore()) {
        when {
            c == '_' -> {
                capitalizeNext = true
            }
            c.isDigit() -> {
                append(c)
                capitalizeNext = true
            }
            else -> {
                append(if (capitalizeNext) c.uppercase() else c)
                capitalizeNext = false
            }
        }
    }
}

/**
 * Convert kebab case or snake case to camel case.
 */
internal fun String.camelCase(): String = buildString {
    var capitalizeNext = false
    for ((i, c) in hyphenToUnderscore().withIndex()) {
        when {
            c == '_' -> {
                capitalizeNext = true
            }
            i == 0 -> {
                append(c.lowercase())
            }
            c.isDigit() -> {
                append(c)
                capitalizeNext = true
            }
            else -> {
                append(if (capitalizeNext) c.uppercase() else c)
                capitalizeNext = false
            }
        }
    }
}

// We want to support both `-` and `_`, so...
private fun String.hyphenToUnderscore() = replace("-", "_")
