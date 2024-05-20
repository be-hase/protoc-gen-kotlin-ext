# protoc-gen-kotlin-ext

A protoc compiler plugin that generates useful extension code for Kotlin.

## Features

- Generate `***OrNull` extension functions for field presence
    - In `protoc-gen-kotlin(protobuf-kotlin)`, this extension function is only provided for message types. We will
      provide it for scalar
      types as well.
    - https://github.com/protocolbuffers/protobuf/issues/12935
- Generate factory functions that resemble data class constructors

### Example

Let's assume we have the following proto file.

```protobuf
message Person {
  string first_name = 1;
  string last_name = 2;
  optional string middle_name = 3;
  Gender gender = 4;
  optional string nickname = 5;
  Address primary_address = 6;
}

enum Gender {
  GENDER_UNSPECIFIED = 0;
  MALE = 1;
  FEMALE = 2;
  OTHERS = 3;
}

message Address {
  string country = 1;
  string state = 2;
  string city = 3;
  string address_line_1 = 4;
  optional string address_line_2 = 5;
}
```

Using the generated code, you can write the following:

```kotlin
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
```

## How to use?

### Gradle

When used with `protoc-gen-kotlin(protobuf-kotlin)`:

```kotlin
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:<version>"
    }
    plugins {
        id("kotlin-ext") {
            artifact = "dev.hsbrysk:protoc-gen-kotlin-ext:<version>"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("kotlin-ext") {
                    outputSubDir = "kotlin"
                }
            }
            task.builtins {
                id("kotlin")
            }
        }
    }
}
```

When used standalone:

```kotlin
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:<version>"
    }
    plugins {
        id("kotlin-ext") {
            artifact = "dev.hsbrysk:protoc-gen-kotlin-ext:<version>"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("kotlin-ext") {
                    outputSubDir = "kotlin"
                    option("messageOrNullGetter+")
                }
            }
        }
    }
}
```

### Maven

TBD

## Motivation

### Challenges with Field Presence

From protobuf 3.12, the long-awaited optional (Field Presence) has been reintroduced. This allows the representation of
null (as in java/kotlin).

Before this, we had to use message wrappers like StringValue or Option to express this.
https://protobuf.dev/reference/protobuf/google.protobuf/#option

```protobuf
message Sample {
  optional string hoge = 2;
  optional int32 bar = 3;
}
```

However, this optional feature is somewhat tricky. When you retrieve a value with a getter, it returns the default
value (e.g., "" for strings, 0 for int32). This means that for strings, you cannot distinguish whether the value has
been explicitly set to "" or not set at all. Instead, optional fields provide a hasXXX method to check if the value has
been set, which you should use to determine if it is null.

If you are unaware of this specification, you might mistakenly treat an empty string or 0 as a valid value, potentially
causing bugs.

```kotlin
Sample.newBuilder().build().also {
    println("[1] hoge: ${it.hoge}")
    println("[1] hasHoge: ${it.hasHoge()}")
    println("[1] bar: ${it.bar}")
    println("[1] hasBar: ${it.hasBar()}")
}
Sample.newBuilder().setHoge("hoge").setBar(1).build().also {
    println("[2] hoge: ${it.hoge}")
    println("[2] hasHoge: ${it.hasHoge()}")
    println("[2] bar: ${it.bar}")
    println("[2] hasBar: ${it.hasBar()}")
}
```

```
[1] hoge:
[1] hasHoge: false
[1] bar: 0
[1] hasBar: false
[2] hoge: hoge
[2] hasHoge: true
[2] bar: 1
[2] hasBar: true
```

To address this for Kotlin, we will auto-generate xxxOrNull extension properties. When implementing, seeing `※※※OrNull`
through autocompletion should help avoid some issues. Additionally, this allows the use of the Elvis operator, resulting
in smoother code.

```kotlin
val BlahBlah.hogeOrNull: kotlin.String?
    get() = if (hasHoge()) hoge else null
```

Interestingly, for message types, using protoc-gen-kotlin already generates similar extension properties.
https://github.com/protocolbuffers/protobuf/blob/b30f3de12f946b6d610c21bc605726bf56ea889f/src/google/protobuf/compiler/java/message.cc#L1352C33-L1370

An issue has been raised about why this isn't done for optional scalar types, but it seems Google has no plans to
address it.
https://github.com/protocolbuffers/protobuf/issues/12935

### Challenges with Java Builder

Protobuf uses builders to set values. However, since this code is written in Java, there is no type checking for
non-null/nullable. When used from Kotlin, it is not uncommon to accidentally pass null, resulting in an NPE.

```
Sample.newBuilder()
    .setHoge("hoge")
    .setBar(1)
    .build();
```

When using protoc-gen-kotlin, the following DSL is generated for Kotlin. This is beneficial because it includes
non-null/nullable type checking.

```kotlin
sample {
    hoge = "hoge"
    bar = 1
}
```

However, in Kotlin, it feels more natural to write using constructors with named arguments, like data classes.

Additionally, since both methods use a builder style, when a field is added later, it doesn't result in a compile error,
and values might be forgotten to be set. Opinions may differ on whether this is a disadvantage or an advantage.

If you prioritize a more robust programming style, it is preferable to have a compile error indicating that a value has
been forgotten.

Therefore, we will auto-generate the following Factory function. Note that this Sample() is not a constructor but a
function.
It can be written similarly to a data class, and when a field is added later, it will result in a compile error,
indicating that a value might be forgotten to be set. Since it is defined in Kotlin, nullable type checking is also
enabled.

```kotlin
val sample = Sample(
    hoge = "hoge",
    bar = 1
)
```

```kotlin
public fun Sample(hoge: String?, bar: Int?): Sample {
  val _builder = Sample.newBuilder().apply {
    hoge?.let { setHoge(it) }
    bar?.let { setBar(it) }
  }
  return _builder.build()
}
```

We considered adding Factory functions with default arguments, but we decided not to create them at this time. If there are default arguments, adding a field later would not result in a compile error.
We believe it is better to use the Java builder and Kotlin DSL for this.
