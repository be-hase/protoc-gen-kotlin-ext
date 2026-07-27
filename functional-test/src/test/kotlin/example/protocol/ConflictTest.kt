package example.protocol

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

@Suppress("RedundantExplicitType")
class ConflictTest {
    @Test
    fun `compileCheck ListConflict`() {
        val result: ListConflict = ListConflict(fooList1 = "hoge", foo2List = listOf("bar"))
        assertThat(result.fooList1).isEqualTo("hoge")
        assertThat(result.foo2List).isEqualTo(listOf("bar"))
    }

    @Test
    fun `compileCheck CountConflict`() {
        val result: CountConflict = CountConflict(barCount1 = null, bar2List = listOf("bar"))
        assertThat(result.barCount1).isEqualTo("")
        assertThat(result.barCount1OrNull).isEqualTo(null)
        assertThat(result.bar2List).isEqualTo(listOf("bar"))
    }

    @Test
    fun `compileCheck MapConflict`() {
        val result: MapConflict = MapConflict(bazCount1 = "hoge", baz2Map = mapOf("hoge" to "hogeVal"))
        assertThat(result.bazCount1).isEqualTo("hoge")
        assertThat(result.baz2Map).isEqualTo(mapOf("hoge" to "hogeVal"))
    }

    @Test
    fun `compileCheck EnumValueConflict`() {
        val result: EnumValueConflict = EnumValueConflict(
            color1 = ConflictColor.CONFLICT_COLOR_RED,
            colorValue2 = "hoge",
        )
        assertThat(result.color1).isEqualTo(ConflictColor.CONFLICT_COLOR_RED)
        assertThat(result.colorValue2).isEqualTo("hoge")
    }
}
