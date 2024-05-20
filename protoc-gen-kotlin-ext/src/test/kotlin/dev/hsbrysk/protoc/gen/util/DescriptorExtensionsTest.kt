package dev.hsbrysk.protoc.gen.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.google.protobuf.ByteString
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.DescriptorProtos.FileOptions
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Descriptors.EnumDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import com.google.protobuf.Descriptors.FileDescriptor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class DescriptorExtensionsTest {
    @Test
    fun `FileDescriptor javaPackage`() {
        assertThat(
            FileDescriptor.buildFrom(
                FileDescriptorProto.newBuilder().setOptions(FileOptions.newBuilder().build()).setPackage("com.example")
                    .build(),
                arrayOf(),
            ).javaPackage,
        ).isEqualTo("com.example")

        assertThat(
            FileDescriptor.buildFrom(
                FileDescriptorProto.newBuilder()
                    .setOptions(FileOptions.newBuilder().setJavaPackage("com.example.java").build())
                    .setPackage("com.example").build(),
                arrayOf(),
            ).javaPackage,
        ).isEqualTo("com.example.java")
    }

    @Test
    fun `FileDescriptor protoFileName`() {
        assertThat(
            FileDescriptor.buildFrom(
                FileDescriptorProto.newBuilder().setName("hoge.proto").build(),
                arrayOf(),
            ).protoFileName,
        ).isEqualTo("hoge")

        assertThat(
            FileDescriptor.buildFrom(
                FileDescriptorProto.newBuilder().setName("com/example/hoge.proto").build(),
                arrayOf(),
            ).protoFileName,
        ).isEqualTo("hoge")
    }

    @Test
    fun `Descriptor javaPackage`() {
        val fileDescriptor = FileDescriptor.buildFrom(
            FileDescriptorProto.newBuilder().setPackage("com.example")
                .addMessageType(DescriptorProto.newBuilder().setName("Hoge").build())
                .setOptions(FileOptions.newBuilder().setJavaMultipleFiles(true).build()).build(),
            arrayOf(),
        )

        assertThat(fileDescriptor.messageTypes.first().javaPackage).isEqualTo("com.example")
    }

    @Test
    fun `Descriptor javaPackage outerClass`() {
        val fileDescriptor = FileDescriptor.buildFrom(
            FileDescriptorProto.newBuilder().setPackage("com.example")
                .addMessageType(DescriptorProto.newBuilder().setName("Bar").build()).setOptions(
                    FileOptions.newBuilder().setJavaMultipleFiles(false).setJavaOuterClassname("Hoge").build(),
                ).build(),
            arrayOf(),
        )

        assertThat(fileDescriptor.messageTypes.first().javaPackage).isEqualTo("com.example.Hoge")
    }

    @Test
    fun `Descriptor javaPackage same outerClass`() {
        val fileDescriptor = FileDescriptor.buildFrom(
            FileDescriptorProto.newBuilder().setPackage("com.example")
                .addMessageType(DescriptorProto.newBuilder().setName("Hoge").build()).setOptions(
                    FileOptions.newBuilder().setJavaMultipleFiles(false).setJavaOuterClassname("Hoge").build(),
                ).build(),
            arrayOf(),
        )

        assertThat(fileDescriptor.messageTypes.first().javaPackage).isEqualTo("com.example.HogeOuterClass")
    }

    @Test
    fun `Descriptor javaPackage recursively`() {
        val fileDescriptor = FileDescriptor.buildFrom(
            FileDescriptorProto.newBuilder().setPackage("com.example")
                .setOptions(FileOptions.newBuilder().setJavaMultipleFiles(true).build()).build(),
            arrayOf(),
        )
        val descriptor1: Descriptor = mockk {
            every { name } returns "Descriptor1"
            every { containingType } returns null
        }
        val descriptor2: Descriptor = mockk {
            every { name } returns "Descriptor2"
            every { containingType } returns descriptor1
        }
        val descriptor3: Descriptor = mockk {
            every { file } returns fileDescriptor
            every { name } returns "Descriptor3"
            every { containingType } returns descriptor2
        }

        assertThat(descriptor3.javaPackage).isEqualTo("com.example.Descriptor1.Descriptor2")
    }

    @Test
    fun `FieldDescriptor javaName`() {
        assertThat(
            mockk<FieldDescriptor> {
                every { isMapField } returns false
                every { isRepeated } returns false
                every { name } returns "hoge_bar"
            }.javaName,
        ).isEqualTo("hogeBar")

        // When repeated, a List suffix is added.
        assertThat(
            mockk<FieldDescriptor> {
                every { isMapField } returns false
                every { isRepeated } returns true
                every { name } returns "hoge_bar"
            }.javaName,
        ).isEqualTo("hogeBarList")

        // When map, a Map suffix is added.
        assertThat(
            mockk<FieldDescriptor> {
                every { isMapField } returns true
                every { name } returns "hoge_bar"
            }.javaName,
        ).isEqualTo("hogeBarMap")
    }

    @TestFactory
    fun `FieldDescriptor typeName basic 2`(): List<DynamicTest> {
        data class TestCase(
            val javaType: JavaType,
            val typeName: TypeName,
        )

        val testCases = listOf(
            TestCase(JavaType.BOOLEAN, Boolean::class.asTypeName()),
            TestCase(JavaType.INT, Int::class.asTypeName()),
            TestCase(JavaType.LONG, Long::class.asTypeName()),
            TestCase(JavaType.FLOAT, Float::class.asTypeName()),
            TestCase(JavaType.DOUBLE, Double::class.asTypeName()),
            TestCase(JavaType.STRING, String::class.asTypeName()),
            TestCase(JavaType.BYTE_STRING, ByteString::class.asTypeName()),
        )

        return testCases.map { tc ->
            DynamicTest.dynamicTest("$tc") {
                assertThat(
                    mockk<FieldDescriptor> {
                        every { isMapField } returns false
                        every { isRepeated } returns false
                        every { hasPresence() } returns false
                        every { javaType } returns tc.javaType
                    }.typeName,
                ).isEqualTo(tc.typeName)
            }
        }
    }

    @Test
    fun `FieldDescriptor typeName enum`() {
        val fileDescriptor = FileDescriptor.buildFrom(
            FileDescriptorProto.newBuilder().setPackage("com.example").build(),
            arrayOf(),
        )
        val enumDescriptor: EnumDescriptor = mockk {
            every { file } returns fileDescriptor
            every { name } returns "HogeEnum"
            every { containingType } returns null
        }
        assertThat(
            mockk<FieldDescriptor> {
                every { isMapField } returns false
                every { isRepeated } returns false
                every { hasPresence() } returns false
                every { javaType } returns JavaType.ENUM
                every { enumType } returns enumDescriptor
            }.typeName.toString(),
        ).isEqualTo(ClassName("com.example", "HogeEnum").toString())
    }

    @Test
    fun `FieldDescriptor typeName message`() {
        val fileDescriptor = FileDescriptor.buildFrom(
            FileDescriptorProto.newBuilder().setPackage("com.example").build(),
            arrayOf(),
        )
        val messageDescriptor: Descriptor = mockk {
            every { file } returns fileDescriptor
            every { name } returns "Hoge"
            every { containingType } returns null
        }
        assertThat(
            mockk<FieldDescriptor> {
                every { isMapField } returns false
                every { isRepeated } returns false
                every { hasPresence() } returns false
                every { javaType } returns JavaType.MESSAGE
                every { messageType } returns messageDescriptor
            }.typeName.toString(),
        ).isEqualTo(ClassName("com.example", "Hoge").toString())
    }

    @Test
    fun `FieldDescriptor typeName map`() {
        val keyDescriptor: FieldDescriptor = mockk {
            every { javaType } returns JavaType.STRING
        }
        val valueDescriptor: FieldDescriptor = mockk {
            every { javaType } returns JavaType.INT
        }
        val messageDescriptor: Descriptor = mockk {
            every { findFieldByName("key") } returns keyDescriptor
            every { findFieldByName("value") } returns valueDescriptor
        }
        assertThat(
            mockk<FieldDescriptor> {
                every { isMapField } returns true
                every { messageType } returns messageDescriptor
            }.typeName,
        ).isEqualTo(MAP.parameterizedBy(String::class.asTypeName(), Int::class.asTypeName()))
    }

    @Test
    fun `FieldDescriptor typeName repeated`() {
        assertThat(
            mockk<FieldDescriptor> {
                every { isMapField } returns false
                every { isRepeated } returns true
                every { javaType } returns JavaType.STRING
            }.typeName,
        ).isEqualTo(LIST.parameterizedBy(String::class.asTypeName()))
    }

    @Test
    fun `FieldDescriptor typeName hasPresence`() {
        assertThat(
            mockk<FieldDescriptor> {
                every { isMapField } returns false
                every { isRepeated } returns false
                every { hasPresence() } returns true
                every { javaType } returns JavaType.STRING
            }.typeName,
        ).isEqualTo(String::class.asTypeName().copy(nullable = true))
    }
}
