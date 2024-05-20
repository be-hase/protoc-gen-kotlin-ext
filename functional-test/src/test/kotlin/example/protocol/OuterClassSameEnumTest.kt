package example.protocol

import assertk.assertThat
import assertk.assertions.isNull
import example.protocol.OuterClassSameEnumOuterClass.OuterClassSameEnum
import example.protocol.OuterClassSameEnumOuterClass.OuterClassSameEnumMessage
import org.junit.jupiter.api.Test

@Suppress("RedundantExplicitType")
class OuterClassSameEnumTest {
    @Test
    fun compileCheck() {
        // If there is a class with the same name directly under the outer class, it will be named {OuterClassName}OuterClass.

        val outerClass: OuterClassSameEnumMessage =
            OuterClassSameEnumMessage(null, null, OuterClassSameEnum.UNSPECIFIED)
        assertThat(outerClass.nameOrNull).isNull()
        assertThat(outerClass.timestampOrNull).isNull()
    }
}
