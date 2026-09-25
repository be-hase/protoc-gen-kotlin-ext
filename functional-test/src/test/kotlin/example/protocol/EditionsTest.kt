package example.protocol

import assertk.assertThat
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.junit.jupiter.api.Test

@Suppress("RedundantExplicitType")
class EditionsTest {
    @Test
    fun compileCheck() {
        val message: EditionsMessage = EditionsMessage(
            implicitInt32 = 0,
            implicitString = "",
            explicitInt32 = null,
            explicitString = null,
            nested = null,
            repeatedStringList = listOf(),
            mapFieldMap = mapOf(),
            oneOfA = null,
            oneOfB = null,
            enumFd = EditionsEnum.EDITIONS_ENUM_UNSPECIFIED,
            explicitEnum = null,
        )

        assertThat(message.explicitInt32OrNull).isNull()
        assertThat(message.explicitStringOrNull).isNull()
        assertThat(message.nestedOrNull).isNull()
        assertThat(message.explicitEnumOrNull).isNull()

        val messageWithValues: EditionsMessage = EditionsMessage(
            implicitInt32 = 42,
            implicitString = "hello",
            explicitInt32 = 100,
            explicitString = "world",
            nested = EditionsNested(value = "nested_value"),
            repeatedStringList = listOf("a", "b"),
            mapFieldMap = mapOf("key" to "value"),
            oneOfA = "one_of_a",
            oneOfB = null,
            enumFd = EditionsEnum.EDITIONS_ENUM_A,
            explicitEnum = EditionsEnum.EDITIONS_ENUM_A,
        )

        assertThat(messageWithValues.explicitInt32OrNull).isNotNull()
        assertThat(messageWithValues.explicitStringOrNull).isNotNull()
        assertThat(messageWithValues.nestedOrNull).isNotNull()
        assertThat(messageWithValues.explicitEnumOrNull).isNotNull()
    }
}
