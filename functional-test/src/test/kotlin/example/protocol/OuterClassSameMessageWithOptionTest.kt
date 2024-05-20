package example.protocol

import assertk.assertThat
import assertk.assertions.isNull
import example.protocol.OuterClassSameMessageWithOptionEnclosing.OuterClassSameMessageWithOption
import example.protocol.OuterClassSameMessageWithOptionEnclosing.OuterClassSameMessageWithOption.Nested1
import example.protocol.OuterClassSameMessageWithOptionExtensions.Nested1
import org.junit.jupiter.api.Test

@Suppress("RedundantExplicitType")
class OuterClassSameMessageWithOptionTest {
    @Test
    fun compileCheck() {
        val outerClass: OuterClassSameMessageWithOption = OuterClassSameMessageWithOption(null, null)
        assertThat(outerClass.nameOrNull).isNull()
        assertThat(outerClass.timestampOrNull).isNull()

        val nested1: Nested1 = Nested1(null, null)
        assertThat(nested1.nameOrNull).isNull()
        assertThat(nested1.timestampOrNull).isNull()
    }
}
