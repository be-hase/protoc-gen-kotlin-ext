package example

import assertk.assertThat
import assertk.assertions.isNull
import example.NoneJavaPackageOuterClass.NoneJavaPackage
import org.junit.jupiter.api.Test

@Suppress("RedundantExplicitType")
class NoneJavaPackageTest {
    @Test
    fun compileCheck() {
        val obj: NoneJavaPackage = NoneJavaPackage(null, null)
        assertThat(obj.nameOrNull).isNull()
        assertThat(obj.timestampOrNull).isNull()
    }
}
