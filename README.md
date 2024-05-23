# protoc-gen-kotlin-ext

A protoc compiler plugin that generates useful extension code for Kotlin.

## Features

- Generate `*OrNull` extension properties for optional field
    - In [protoc-gen-kotlin](https://protobuf.dev/getting-started/kotlintutorial/), this extension properties is only
      provided for message types. We provide it for scalar types as well.
    - https://github.com/protocolbuffers/protobuf/issues/12935
- Generate factory functions that resemble data class constructors
- We do not generate our own code, so we can take advantage of the official ecosystem
    - It only generates additional useful code.

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
    // You can use factory functions that resemble data class constructors
    val person = Person(
        firstName = "Ryosuke",
        lastName = "Hasebe",
        middleName = null, // Optional fields become nullable
        gender = Gender.MALE,
        nickname = null, // Optional fields become nullable
        primaryAddress = Address(
            country = "JP",
            state = "Tokyo",
            city = "blah blah blah",
            addressLine1 = "blah blah blah",
            addressLine2 = null, // Optional fields become nullable
        ),
    )

    // You can use `*OrNull` extension properties for optional field
    println(person.middleNameOrNull)
    println(person.nicknameOrNull)
    println(person.primaryAddress.addressLine2OrNull)
    println(person.primaryAddressOrNull)
}
```

## How to use?

### Gradle

When used with [protoc-gen-kotlin](https://protobuf.dev/getting-started/kotlintutorial/):

```kotlin
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:<version>"
    }
    plugins {
        id("kotlin-ext") {
            artifact = "dev.hsbrysk:protoc-gen-kotlin-ext:<version>:jdk8@jar"
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
            artifact = "dev.hsbrysk:protoc-gen-kotlin-ext:<version>:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("kotlin-ext") {
                    outputSubDir = "kotlin"
                    // This option is explained later in the document
                    option("messageOrNullGetter+")
                }
            }
        }
    }
}
```

### Maven or `Manual protoc Usage`

Since the plugin is created using the same mechanism as protoc-gen-grpc-kotlin, please refer to this document.

- [Maven](https://github.com/grpc/grpc-kotlin/blob/master/compiler/README.md#maven)
- [Manual protoc Usage](https://github.com/grpc/grpc-kotlin/blob/master/compiler/README.md#manual-protoc-usage)

## Motivation

### Challenges with Pptional Field (Field Presence)

From protobuf 3.12, the long-awaited optional field (Field Presence) has been reintroduced. This allows the
representation of null (as in java/kotlin).

```protobuf
message Sample {
  optional string hoge = 2;
  optional int32 bar = 3;
}
```

However, this optional feature is somewhat tricky. When you retrieve a value with a getter, it returns the default
value (e.g., `""` for strings, `0` for int32). This means that for strings, you cannot distinguish whether the value has
been explicitly set to `""` or not set at all. Instead, optional fields provide a `has*` method to check if the value
has been set, which you should use to determine if it is `null`.

If you are unaware of this specification, you might mistakenly treat an `""` or `0` as a valid value, potentially
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

Even knowing this specification correctly would result in a large amount of boilerplate code, such as the following:

```kotlin
// 😭
val hoge = if (sample.hasHoge()) {
    sample.hoge
} else {
    null
}
```

To address this for Kotlin, we will auto-generate `*OrNull` extension properties. When implementing,
seeing `*OrNull`
through autocompletion should help avoid some issues. Additionally, this allows the use of the Elvis operator, resulting
in smoother code.

```kotlin
val BlahBlah.hogeOrNull: kotlin.String?
    get() = if (hasHoge()) hoge else null
```

Interestingly, for message types, using `protoc-gen-kotlin` already generates similar extension properties.
https://github.com/protocolbuffers/protobuf/blob/b30f3de12f946b6d610c21bc605726bf56ea889f/src/google/protobuf/compiler/java/message.cc#L1352C33-L1370

I have raised an [issue](https://github.com/protocolbuffers/protobuf/issues/12935) requesting the addition of optional
scalar types, but it is not planned to be supported by `protoc-gen-kotlin`.

### Challenges with Java Builder

Protobuf uses builders to set values. Since this code is written in Java, there is no non-null/nullable type checking.
When used from Kotlin, it is often misunderstood that it is okay to set null for optional fields, which results in
passing null and encountering NPEs (NullPointerExceptions).

```kotlin
Sample.newBuilder()
    .setHoge(null) // This code raises NPE 😭
    .setBar(1)
    .build();
```

When using protoc-gen-kotlin, the following DSL is generated for Kotlin. This is beneficial because it includes
non-null/nullable type checking. Great!

```kotlin
sample {
    hoge = null // This code results in a compile error 😀
    bar = 1
}
```

However, in Kotlin, it feels more natural to write using constructors with named arguments, like data classes.

Additionally, just like the builder, the DSL does not result in a compile error when a field is added later. This can
lead to cases where values are forgotten to be set. However, opinions may differ on whether this is seen as a
disadvantage or an advantage.

If you prioritize a more robust programming style, it is preferable to have a compile error indicating that a value has
been forgotten.

Therefore, we will auto-generate the following factory function. Note that this `Sample(...)` is not a constructor but a
function.
It can be written similarly to a data class, and when a field is added later, it will result in a compile error,
indicating that a value might be forgotten to be set. Since it is defined in Kotlin, nullable type checking is also
enabled.

```kotlin
// similar to a data class 😀
Sample(
    hoge = "hoge",
    bar = 1
)

// Of course, when it is optional, it becomes a nullable type
Sample(
    hoge = null, // This code results in a compile error 😀
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

We considered adding factory functions with default arguments, but we decided not to create them at this time. If there
are default arguments, adding a field later would not result in a compile error.
If you want to use default values, you can use java builder or kotlin DSL.

## Advanced

### Compile Options

Perhaps you only want the `*OrNull` extension property and do not need the factory function. (and vice versa).

This can be achieved by setting the compile options.

```kotlin
// Here, we will use Gradle as an example.
protobuf {
    // ...
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("kotlin-ext") {
                    // ...
                    // HERE
                    option("messageOrNullGetter+")
                }
            }
        }
    }
}
```

The following options are available. Appending + to the end of an option enables it, while appending - disables it.

- `factory`
    - Whether to generate a factory (default: on)
- `orNullGetter`
    - Whether to generate `orNull` extension functions for optional scalar fields (default: on)
- `messageOrNullGetter`
    - Whether to generate `orNull` extension functions for optional message fields (default: off)
    - Not needed when using protobuf-kotlin.

When specifying multiple options, please separate them with commas. For
example, `factory-, orNullGetter+, messageOrNullGetter+`
