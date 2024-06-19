package example.protocol

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

@Suppress("RedundantExplicitType")
class KeywordTest {
    @Test
    fun `compileCheck BasicClass`() {
        val result: BasicClass = BasicClass(class_ = "hoge")
        assertThat(result.class_).isEqualTo("hoge")
    }

    @Test
    fun `compileCheck OptionalClass`() {
        val result: OptionalClass = OptionalClass(class_ = null)
        assertThat(result.class_).isEqualTo("")
        assertThat(result.class_OrNull).isEqualTo(null)
    }

    @Test
    fun `compileCheck RepeatedClass`() {
        val result: RepeatedClass = RepeatedClass(class_List = listOf("hoge"))
        assertThat(result.class_List).isEqualTo(listOf("hoge"))
    }

    @Test
    fun `compileCheck MapClass`() {
        val result: MapClass = MapClass(class_Map = mapOf("hoge" to "hogeVal"))
        assertThat(result.class_Map).isEqualTo(mapOf("hoge" to "hogeVal"))
    }

    @Test
    fun `compileCheck Keyword`() {
        val result: Keyword = Keyword(`return` = "hoge", `if` = null)
        assertThat(result.`return`).isEqualTo("hoge")
        assertThat(result.`if`).isEqualTo("")
        assertThat(result.ifOrNull).isEqualTo(null)
    }
}
