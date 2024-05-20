package dev.hsbrysk.protoc.gen.util

/**
 * kebab case or snake caseをpascal case(a.k.a upper camel case)に変換します
 */
internal fun String.pascalCase(): String {
    return hyphenToUnderscore().split("_").joinToString("") { value ->
        value.replaceFirstChar { it.uppercase() }
    }
}

/**
 * kebab case or snake caseをcamel caseに変換します
 */
internal fun String.camelCase(): String {
    return hyphenToUnderscore().split("_").withIndex().joinToString("") { (index, value) ->
        if (index == 0) value.replaceFirstChar { it.lowercase() } else value.replaceFirstChar { it.uppercase() }
    }
}

// We want to support both `-` and `_`, so...
private fun String.hyphenToUnderscore() = replace("-", "_")
