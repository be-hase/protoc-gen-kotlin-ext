package dev.hsbrysk.protoc.gen

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.google.protobuf.compiler.PluginProtos
import dev.hsbrysk.protoc.gen.CompileOption.FACTORY
import dev.hsbrysk.protoc.gen.CompileOption.MESSAGE_OR_NULL_GETTER
import dev.hsbrysk.protoc.gen.CompileOption.OR_NULL_GETTER
import org.junit.jupiter.api.Test

class CompileOptionTest {
    @Test
    fun parseOptions() {
        // The default is `FACTORY, SCALAR_OR_NULL_GETTER`.
        assertThat(
            CompileOption.parseOptions(
                PluginProtos.CodeGeneratorRequest.newBuilder()
                    .setParameter("")
                    .build(),
            ),
        ).isEqualTo(setOf(FACTORY, OR_NULL_GETTER))

        // It's okay to specify explicitly (though it's meaningless).
        assertThat(
            CompileOption.parseOptions(
                PluginProtos.CodeGeneratorRequest.newBuilder()
                    .setParameter("factory+, orNullGetter+")
                    .build(),
            ),
        ).isEqualTo(setOf(FACTORY, OR_NULL_GETTER))

        // Enable all.
        assertThat(
            CompileOption.parseOptions(
                PluginProtos.CodeGeneratorRequest.newBuilder()
                    .setParameter("messageOrNullGetter+")
                    .build(),
            ),
        ).isEqualTo(setOf(FACTORY, OR_NULL_GETTER, MESSAGE_OR_NULL_GETTER))

        // Disable all.
        assertThat(
            CompileOption.parseOptions(
                PluginProtos.CodeGeneratorRequest.newBuilder()
                    .setParameter("factory-, orNullGetter-")
                    .build(),
            ),
        ).isEqualTo(setOf())
    }

    @Test
    fun `parseOptions error`() {
        assertFailure {
            CompileOption.parseOptions(
                PluginProtos.CodeGeneratorRequest.newBuilder()
                    .setParameter("invalid")
                    .build(),
            )
        }.isInstanceOf(IllegalArgumentException::class).hasMessage("option name must be followed by + or -")

        assertFailure {
            CompileOption.parseOptions(
                PluginProtos.CodeGeneratorRequest.newBuilder()
                    .setParameter("invalid+")
                    .build(),
            )
        }.isInstanceOf(IllegalArgumentException::class).hasMessage("invalid option name")
    }
}
