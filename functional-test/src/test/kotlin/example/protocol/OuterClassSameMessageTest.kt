package example.protocol

import assertk.assertThat
import assertk.assertions.isNull
import example.protocol.OuterClassSameMessageExtensions.Nested1
import example.protocol.OuterClassSameMessageExtensions.Nested1Extensions.Nested2
import example.protocol.OuterClassSameMessageOuterClass.OuterClassSameMessage
import example.protocol.OuterClassSameMessageOuterClass.OuterClassSameMessage.Nested1
import example.protocol.OuterClassSameMessageOuterClass.OuterClassSameMessage.Nested1.Nested2
import org.junit.jupiter.api.Test

@Suppress("RedundantExplicitType")
class OuterClassSameMessageTest {
    @Test
    fun compileCheck() {
        // If there is a class with the same name directly under the outer class, it will be named {OuterClassName}OuterClass.

        val outerClass: OuterClassSameMessage = OuterClassSameMessage(null, null)
        assertThat(outerClass.nameOrNull).isNull()
        assertThat(outerClass.timestampOrNull).isNull()

        val nested1: Nested1 = Nested1(null, null)
        assertThat(nested1.nameOrNull).isNull()
        assertThat(nested1.timestampOrNull).isNull()

        val nested2: Nested2 = Nested2(null, null)
        assertThat(nested2.nameOrNull).isNull()
        assertThat(nested2.timestampOrNull).isNull()
    }
}
