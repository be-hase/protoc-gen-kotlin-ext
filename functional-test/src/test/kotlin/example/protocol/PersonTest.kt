package example.protocol

import assertk.assertThat
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import example.protocol.PersonOuterClass.Gender
import example.protocol.PersonOuterClass.Person
import org.junit.jupiter.api.Test

@Suppress("RedundantExplicitType")
class PersonTest {
    @Test
    fun compileCheck() {
        val person: Person = Person(
            firstName = "Ryosuke",
            lastName = "Hasebe",
            middleName = null,
            gender = Gender.MALE,
            nickname = null,
            primaryAddress = Address(
                country = "JP",
                state = "Tokyo",
                city = "blah blah blah",
                addressLine1 = "blah blah blah",
                addressLine2 = null,
            ),
        )

        assertThat(person.middleNameOrNull).isNull()
        assertThat(person.nicknameOrNull).isNull()
        assertThat(person.primaryAddress.addressLine2OrNull).isNull()
        assertThat(person.primaryAddressOrNull).isNotNull()
    }
}

fun main() {
    // Generate factory functions that resemble data class constructors
    val person = Person(
        firstName = "Ryosuke",
        lastName = "Hasebe",
        middleName = null,
        gender = Gender.MALE,
        nickname = null,
        primaryAddress = Address(
            country = "JP",
            state = "Tokyo",
            city = "blah blah blah",
            addressLine1 = "blah blah blah",
            addressLine2 = null,
        ),
    )

    // Generate ***OrNull extension functions for field presence
    println(person.middleNameOrNull)
    println(person.nicknameOrNull)
    println(person.primaryAddress.addressLine2OrNull)
    println(person.primaryAddressOrNull)
}
