package dev.hsbrysk.protoc.gen.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class StringExtensionsTest {
    @Test
    fun pascalCase() {
        assertThat("".pascalCase()).isEqualTo("")
        assertThat("hoge-bar".pascalCase()).isEqualTo("HogeBar")
        assertThat("hoge_bar".pascalCase()).isEqualTo("HogeBar")
        assertThat("Hoge-Bar".pascalCase()).isEqualTo("HogeBar")
        assertThat("Hoge_Bar".pascalCase()).isEqualTo("HogeBar")
        assertThat("あ-_い".pascalCase()).isEqualTo("あい")
    }

    @Test
    fun camelCase() {
        assertThat("".camelCase()).isEqualTo("")
        assertThat("hoge-bar".camelCase()).isEqualTo("hogeBar")
        assertThat("hoge_bar".camelCase()).isEqualTo("hogeBar")
        assertThat("Hoge-Bar".camelCase()).isEqualTo("hogeBar")
        assertThat("Hoge_Bar".camelCase()).isEqualTo("hogeBar")
        assertThat("あ-_い".camelCase()).isEqualTo("あい")
    }
}
