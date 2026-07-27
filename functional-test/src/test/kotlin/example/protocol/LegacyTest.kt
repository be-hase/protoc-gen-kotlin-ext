package example.protocol

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import example.protocol.Legacy.LegacyMessage
import org.junit.jupiter.api.Test

class LegacyTest {
    @Test
    fun `proto2 files are compiled but no extension code is generated`() {
        // protoc's Java generator still processes proto2 files.
        val message = LegacyMessage.newBuilder().setName("hoge").build()
        assertThat(message.name).isEqualTo("hoge")

        // Sanity check: for proto3 files, top-level extension functions are compiled
        // into a `*KtExtensionsKt` class.
        Class.forName("example.protocol.PersonKtExtensionsKt")

        // protoc-gen-kotlin-ext skips proto2 files, so no extension file is generated.
        assertFailure { Class.forName("example.protocol.LegacyKtExtensionsKt") }
            .isInstanceOf(ClassNotFoundException::class)
    }
}
