package dev.hsbrysk.protoc.gen.util

import com.google.protobuf.ByteString
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Descriptors.EnumDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import com.google.protobuf.Descriptors.FileDescriptor
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName

internal val FileDescriptor.javaPackage: String
    get() = if (options.hasJavaPackage()) {
        options.javaPackage
    } else {
        `package`
    }

/**
 * The proto filename without the extension
 */
internal val FileDescriptor.protoFileName: String
    get() = name.substringAfterLast('/').removeSuffix(".proto")

/**
 * Gets the Java package of the class corresponding to the descriptor.
 *
 * e.g.
 * com.example.Hoge -> com.example
 * com.example.Hoge.Bar -> com.example.Hoge
 */
internal val Descriptor.javaPackage: String
    get() = javaPackage(MessageDescriptorWrapper(this))

/**
 * Gets the Java package of the enum corresponding to the descriptor.
 *
 * e.g.
 * com.example.Hoge -> com.example
 * com.example.Hoge.Bar -> com.example.Hoge
 */
internal val EnumDescriptor.javaPackage: String
    get() = javaPackage(EnumDescriptorWrapper(this))

private fun javaPackage(descriptor: DescriptorWrapper): String {
    var javaPackage = descriptor.file.javaPackage

    // When `java_multiple_files = false`, a class using the proto filename is generated.
    // At this time, if there is an inner class with the same name, it will be named `xxxOuterClass`.
    if (!descriptor.file.options.javaMultipleFiles) {
        var outerClassName = descriptor.file.options.javaOuterClassname.ifEmpty {
            descriptor.file.protoFileName.pascalCase()
        }
        val foundDuplicate = descriptor.file.enumTypes.any { it.name == outerClassName } ||
            descriptor.file.messageTypes.any { it.name == outerClassName } ||
            descriptor.file.services.any { it.name == outerClassName }
        if (foundDuplicate) {
            outerClassName += "OuterClass"
        }
        javaPackage += ".$outerClassName"
    }

    val enclosingClassNames = enclosingClassNames(mutableListOf(), descriptor)
    if (enclosingClassNames.isNotEmpty()) {
        javaPackage += ".${enclosingClassNames.joinToString(".")}"
    }

    return javaPackage
}

// The method signature is not ideal...
private fun enclosingClassNames(
    result: MutableList<String>,
    descriptor: DescriptorWrapper,
): List<String> {
    if (descriptor.containingType == null) {
        return result.reversed()
    }
    val containingType = checkNotNull(descriptor.containingType)
    result.add(containingType.name)
    return enclosingClassNames(result, MessageDescriptorWrapper(containingType))
}

/**
 * The field name in Java
 *
 * e.g.
 * string first_name -> firstName
 * repeated string first_name -> firstNameList
 */
internal val FieldDescriptor.javaName: String
    get() {
        val name = escapedCamelName + conflictSuffix
        return when {
            isMapField -> name + "Map" // Actually, maps are internally repeated, so they should be checked first.
            isRepeated -> name + "List"
            else -> name
        }
    }

/**
 * The PascalCase field name used to build Java accessor method names
 * (e.g. `set{Name}`, `has{Name}`, `addAll{Name}`).
 */
internal val FieldDescriptor.javaPascalName: String
    get() = escapedPascalName + conflictSuffix

private val FieldDescriptor.escapedCamelName: String
    get() = if (isForbiddenJavaName) {
        name.camelCase() + "_"
    } else {
        name.camelCase()
    }

private val FieldDescriptor.escapedPascalName: String
    get() = if (isForbiddenJavaName) {
        name.pascalCase() + "_"
    } else {
        name.pascalCase()
    }

// protoc's Java generator appends the field number to the accessors of both fields when
// two fields in the same message would generate conflicting accessor names
// (e.g. `string foo_list = 1;` and `repeated string foo = 2;` both generate `getFooList()`).
// Keep in sync with `InitializeFieldGeneratorInfoForFields` / `IsConflicting` in
// protobuf's src/google/protobuf/compiler/java/context.cc
private val FieldDescriptor.conflictSuffix: String
    get() {
        val myName = escapedPascalName
        val conflicting = containingType.fields.any { other ->
            other !== this && isConflicting(this, myName, other, other.escapedPascalName)
        }
        return if (conflicting) number.toString() else ""
    }

private fun isConflicting(
    field1: FieldDescriptor,
    name1: String,
    field2: FieldDescriptor,
    name2: String,
): Boolean = name1 == name2 ||
    isConflictingOneWay(field1, name1, field2, name2) ||
    isConflictingOneWay(field2, name2, field1, name1)

private fun isConflictingOneWay(
    field1: FieldDescriptor,
    name1: String,
    field2: FieldDescriptor,
    name2: String,
): Boolean = isRepeatedFieldConflicting(field1, name1, field2, name2) ||
    isEnumFieldConflicting(field1, name1, name2)

// Both a repeated field `foo` and a singular field `foo_count`/`foo_list` would generate
// `getFooCount()`/`getFooList()`.
private fun isRepeatedFieldConflicting(
    field1: FieldDescriptor,
    name1: String,
    field2: FieldDescriptor,
    name2: String,
): Boolean = field1.isRepeated && !field2.isRepeated && (name2 == name1 + "Count" || name2 == name1 + "List")

// Both an open enum field `foo` and a regular field `foo_value` would generate `getFooValue()`.
private fun isEnumFieldConflicting(
    field1: FieldDescriptor,
    name1: String,
    name2: String,
): Boolean = field1.type == FieldDescriptor.Type.ENUM &&
    !field1.legacyEnumFieldTreatedAsClosed() &&
    name2 == name1 + "Value"

// protoc's Java generator appends a trailing "_" to fields whose PascalCase name would
// collide with methods of the generated message's base classes (java.lang.Object,
// MessageLite, MessageOrBuilder).
// Keep in sync with `kForbiddenNames` in protobuf's src/google/protobuf/compiler/java/names.cc
private val forbiddenJavaNames = setOf(
    // java.lang.Object:
    "Class",
    // com.google.protobuf.MessageLiteOrBuilder:
    "DefaultInstanceForType",
    // com.google.protobuf.MessageLite:
    "ParserForType",
    "SerializedSize",
    // com.google.protobuf.MessageOrBuilder:
    "AllFields",
    "DescriptorForType",
    "InitializationErrorString",
    "UnknownFields",
    // obsolete. kept for backwards compatibility of generated code
    "CachedSize",
)

private val FieldDescriptor.isForbiddenJavaName: Boolean
    get() = name.pascalCase() in forbiddenJavaNames

/**
 * kotlinpoetのTypeName
 */
internal val FieldDescriptor.typeName: TypeName
    get() = when {
        isMapField -> {
            val keyDescriptor = messageType.findFieldByName("key")
            val valueDescriptor = messageType.findFieldByName("value")
            // TODO: Consider nullable.
            MAP.parameterizedBy(typeName(keyDescriptor), typeName(valueDescriptor))
        }
        isRepeated -> LIST.parameterizedBy(typeName(this))
        hasPresence() -> typeName(this).copy(nullable = true)
        else -> typeName(this)
    }

private fun typeName(descriptor: FieldDescriptor): TypeName {
    @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
    return when (descriptor.javaType) {
        JavaType.BOOLEAN -> BOOLEAN
        JavaType.INT -> INT
        JavaType.LONG -> LONG
        JavaType.FLOAT -> FLOAT
        JavaType.DOUBLE -> DOUBLE
        JavaType.STRING -> STRING
        JavaType.BYTE_STRING -> ByteString::class.asTypeName()
        JavaType.ENUM -> ClassName(descriptor.enumType.javaPackage, descriptor.enumType.name)
        JavaType.MESSAGE -> ClassName(descriptor.messageType.javaPackage, descriptor.messageType.name)
    }
}

private interface DescriptorWrapper {
    val file: FileDescriptor
    val containingType: Descriptor?
}

class MessageDescriptorWrapper(private val delegate: Descriptor) : DescriptorWrapper {
    override val file: FileDescriptor
        get() = delegate.file
    override val containingType: Descriptor?
        get() = delegate.containingType
}

class EnumDescriptorWrapper(private val delegate: EnumDescriptor) : DescriptorWrapper {
    override val file: FileDescriptor
        get() = delegate.file
    override val containingType: Descriptor?
        get() = delegate.containingType
}
