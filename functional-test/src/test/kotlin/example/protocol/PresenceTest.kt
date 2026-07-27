package example.protocol

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class PresenceTest {
    @Test
    fun `null factory arguments do not set the fields`() {
        val obj = PresenceMessage(
            opInt32Fd = null,
            opBoolFd = null,
            opStringFd = null,
            opEnumFd = null,
            oneOfStringFd = null,
            messageFd = null,
        )

        assertThat(obj.hasOpInt32Fd()).isFalse()
        assertThat(obj.opInt32FdOrNull).isNull()
        assertThat(obj.hasOpBoolFd()).isFalse()
        assertThat(obj.opBoolFdOrNull).isNull()
        assertThat(obj.hasOpStringFd()).isFalse()
        assertThat(obj.opStringFdOrNull).isNull()
        assertThat(obj.hasOpEnumFd()).isFalse()
        assertThat(obj.opEnumFdOrNull).isNull()
        assertThat(obj.hasOneOfStringFd()).isFalse()
        assertThat(obj.oneOfStringFdOrNull).isNull()
        assertThat(obj.testOneOfCase).isEqualTo(PresenceMessage.TestOneOfCase.TESTONEOF_NOT_SET)
        assertThat(obj.hasMessageFd()).isFalse()
        assertThat(obj.messageFdOrNull).isNull()
    }

    @Test
    fun `fields explicitly set to their default values still have presence`() {
        val obj = PresenceMessage(
            opInt32Fd = 0,
            opBoolFd = false,
            opStringFd = "",
            opEnumFd = PresenceEnum.PRESENCE_ENUM_UNSPECIFIED,
            oneOfStringFd = "",
            messageFd = PresenceChild(value = 0),
        )

        assertThat(obj.hasOpInt32Fd()).isTrue()
        assertThat(obj.opInt32FdOrNull).isEqualTo(0)
        assertThat(obj.hasOpBoolFd()).isTrue()
        assertThat(obj.opBoolFdOrNull).isEqualTo(false)
        assertThat(obj.hasOpStringFd()).isTrue()
        assertThat(obj.opStringFdOrNull).isEqualTo("")
        assertThat(obj.hasOpEnumFd()).isTrue()
        assertThat(obj.opEnumFdOrNull).isEqualTo(PresenceEnum.PRESENCE_ENUM_UNSPECIFIED)
        assertThat(obj.hasOneOfStringFd()).isTrue()
        assertThat(obj.oneOfStringFdOrNull).isEqualTo("")
        assertThat(obj.testOneOfCase).isEqualTo(PresenceMessage.TestOneOfCase.ONE_OF_STRING_FD)
        assertThat(obj.hasMessageFd()).isTrue()
        assertThat(obj.messageFdOrNull).isEqualTo(PresenceChild(value = 0))
    }
}
