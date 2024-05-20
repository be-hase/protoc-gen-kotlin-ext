package dev.hsbrysk.protoc.gen.util

import com.squareup.kotlinpoet.FileSpec
import java.nio.file.Path
import java.nio.file.Paths

/**
 * The file path to write to
 */
internal val FileSpec.path: Path
    get() = if (packageName.isEmpty()) {
        Paths.get("$name.kt")
    } else {
        val parts = buildList {
            addAll(packageName.split("."))
            add("$name.kt")
        }.toTypedArray()
        // Since `Paths.get(*parts)` doesn't match the signature well, we have no choice but to...
        Paths.get(parts[0], *parts.sliceArray(1 until parts.size))
    }
