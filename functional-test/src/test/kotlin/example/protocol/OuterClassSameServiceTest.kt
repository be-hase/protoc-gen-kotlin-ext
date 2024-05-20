package example.protocol

import assertk.assertThat
import assertk.assertions.isNull
import example.protocol.OuterClassSameServiceOuterClass.OuterClassSameServiceMessage
import org.junit.jupiter.api.Test

@Suppress("RedundantExplicitType")
class OuterClassSameServiceTest {
    @Test
    fun compileCheck() {
        // If there is a class with the same name directly under the outer class, it will be named {OuterClassName}OuterClass.

        val outerClass: OuterClassSameServiceMessage = OuterClassSameServiceMessage(null, null)
        assertThat(outerClass.nameOrNull).isNull()
        assertThat(outerClass.timestampOrNull).isNull()
    }
}
