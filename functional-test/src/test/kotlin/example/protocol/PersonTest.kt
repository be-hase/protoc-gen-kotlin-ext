package example.protocol

import assertk.assertThat
import assertk.assertions.isNull
import example.protocol.PersonOuterClass.Person
import org.junit.jupiter.api.Test

@Suppress("RedundantExplicitType")
class PersonTest {
    @Test
    fun compileCheck() {
        val person: Person = Person(
            firstName = "ryosuke",
            lastName = "hasebe",
            middleName = null,
            gender = PersonOuterClass.Gender.MALE,
            nickname = null,
            primaryAddress = Address(
                country = "JP",
                state = "Tokyo",
                city = "Shinjuku",
                addressLine1 = "Miraina",
                addressLine2 = null,
            ),
        )
        assertThat(person.middleNameOrNull).isNull()
        assertThat(person.nicknameOrNull).isNull()
        assertThat(person.primaryAddress.addressLine2OrNull).isNull()
    }
}
