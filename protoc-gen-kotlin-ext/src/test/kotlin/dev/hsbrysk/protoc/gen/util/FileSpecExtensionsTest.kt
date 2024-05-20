package dev.hsbrysk.protoc.gen.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.squareup.kotlinpoet.FileSpec
import org.junit.jupiter.api.Test

class FileSpecExtensionsTest {
    @Test
    fun path() {
        assertThat(FileSpec.builder("", "Hoge").build().path.toString())
            .isEqualTo("Hoge.kt")
        assertThat(FileSpec.builder("com", "Hoge").build().path.toString())
            .isEqualTo("com/Hoge.kt")
        assertThat(FileSpec.builder("com.example", "Hoge").build().path.toString())
            .isEqualTo("com/example/Hoge.kt")
    }
}
