/*
 * Copyright (c) 2023-2024 LY Corporation. All rights reserved.
 * LY Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package example.protocol

import assertk.assertThat
import assertk.assertions.isNull
import example.protocol.EnclosingExtensions.Nested1
import example.protocol.EnclosingExtensions.Nested1Extensions.Nested2
import example.protocol.Nested.Enclosing
import example.protocol.Nested.Enclosing.Nested1
import example.protocol.Nested.Enclosing.Nested1.Nested2
import org.junit.jupiter.api.Test

@Suppress("RedundantExplicitType")
class NestedTest {
    @Test
    fun compileCheck() {
        val enclosing: Enclosing = Enclosing(null, null)
        assertThat(enclosing.nameOrNull).isNull()
        assertThat(enclosing.timestampOrNull).isNull()

        val nested1: Nested1 = Nested1(null, null)
        assertThat(nested1.nameOrNull).isNull()
        assertThat(nested1.timestampOrNull).isNull()

        val nested2: Nested2 = Nested2(null, null)
        assertThat(nested2.nameOrNull).isNull()
        assertThat(nested2.timestampOrNull).isNull()
    }
}
