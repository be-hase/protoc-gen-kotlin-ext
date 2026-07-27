package example.protocol

import assertk.assertThat
import assertk.assertions.isNull
import com.google.protobuf.ByteString
import org.junit.jupiter.api.Test

@Suppress("RedundantExplicitType")
class AllTypeMessageTest {
    @Test
    @Suppress("LongMethod")
    fun compileCheck() {
        // Compile-level check that sint32 fields map to Int (not Long).
        val sint32Value: Int = 0
        val opSint32Value: Int? = null
        val obj: AllTypeMessage = AllTypeMessage(
            doubleFd = 0.0,
            opDoubleFd = null,
            rpDoubleFdList = listOf(),
            floatFd = 0.0f,
            opFloatFd = null,
            rpFloatFdList = listOf(),
            int32Fd = 0,
            opInt32Fd = null,
            rpInt32FdList = listOf(),
            int64Fd = 0,
            opInt64Fd = null,
            rpInt64FdList = listOf(),
            uint32Fd = 0,
            opUint32Fd = null,
            rpUint32FdList = listOf(),
            uint64Fd = 0,
            opUint64Fd = null,
            rpUint64FdList = listOf(),
            sint32Fd = sint32Value,
            opSint32Fd = opSint32Value,
            rpSint32FdList = listOf(sint32Value),
            sint64Fd = 0,
            opSint64Fd = null,
            rpSint64FdList = listOf(),
            fixed32Fd = 0,
            opFixed32Fd = null,
            rpFixed32FdList = listOf(),
            fixed64Fd = 0,
            opFixed64Fd = null,
            rpFixed64FdList = listOf(),
            sfixed32Fd = 0,
            opSfixed32Fd = null,
            rpSfixed32FdList = listOf(),
            sfixed64Fd = 0,
            opSfixed64Fd = null,
            rpSfixed64FdList = listOf(),
            boolFd = false,
            opBoolFd = null,
            rpBoolFdList = listOf(),
            stringFd = "",
            opStringFd = null,
            rpStringFdList = listOf(),
            bytesFd = ByteString.EMPTY,
            opBytesFd = null,
            rpBytesFdList = listOf(),
            timestampFd = null,
            opTimestampFd = null,
            rpTimestampFdList = listOf(),
            oneOfA = null,
            oneOfB = null,
            enumFd = AllTypeMessageEnum.ALL_UNSPECIFIED,
            opEnumFd = null,
            rpEnumFdList = listOf(),
            mapFdMap = mapOf(),
        )
        assertThat(obj.opDoubleFdOrNull).isNull()
        assertThat(obj.opFloatFdOrNull).isNull()
        assertThat(obj.opInt32FdOrNull).isNull()
        assertThat(obj.opInt64FdOrNull).isNull()
        assertThat(obj.opUint32FdOrNull).isNull()
        assertThat(obj.opUint64FdOrNull).isNull()
        val opSint32FdOrNull: Int? = obj.opSint32FdOrNull
        assertThat(opSint32FdOrNull).isNull()
        assertThat(obj.opSint64FdOrNull).isNull()
        assertThat(obj.opFixed32FdOrNull).isNull()
        assertThat(obj.opFixed64FdOrNull).isNull()
        assertThat(obj.opSfixed32FdOrNull).isNull()
        assertThat(obj.opSfixed64FdOrNull).isNull()
        assertThat(obj.opBoolFdOrNull).isNull()
        assertThat(obj.opStringFdOrNull).isNull()
        assertThat(obj.opBytesFdOrNull).isNull()
        assertThat(obj.timestampFdOrNull).isNull()
        assertThat(obj.opTimestampFdOrNull).isNull()
        assertThat(obj.oneOfAOrNull).isNull()
        assertThat(obj.oneOfBOrNull).isNull()
        assertThat(obj.opEnumFdOrNull).isNull()
    }
}
