package dev.hsbrysk.protoc.gen

import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Descriptors.FieldDescriptor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import dev.hsbrysk.protoc.gen.util.javaName
import dev.hsbrysk.protoc.gen.util.javaPackage
import dev.hsbrysk.protoc.gen.util.pascalCase
import dev.hsbrysk.protoc.gen.util.typeName

abstract class AbstractOrNullGetterGenerator : Generator {
    /**
     * Whether it is a field for which `orNull` should be generated.
     */
    abstract fun predicate(fieldDescriptor: FieldDescriptor): Boolean

    override fun apply(
        fileSpecBuilder: FileSpec.Builder,
        fileDescriptor: Descriptors.FileDescriptor,
    ) {
        fileDescriptor.messageTypes.forEach { messageDescriptor ->
            applyRecursively(fileSpecBuilder, messageDescriptor)
        }
    }

    // Process recursively, including nested types.
    private fun applyRecursively(
        builder: FileSpec.Builder,
        messageDescriptor: Descriptor,
    ) {
        messageDescriptor.fields.filter { it.hasPresence() && predicate(it) }.forEach { fieldDescriptor ->
            builder.addProperty(buildPropertySpec(messageDescriptor, fieldDescriptor))
        }

        messageDescriptor.nestedTypes
            // Maps are internally defined as nested types, so exclude them.
            // ref: https://protobuf.com/docs/language-spec#maps
            .filterNot { it.options.mapEntry }
            .forEach { nestedMessageDescriptor ->
                applyRecursively(builder, nestedMessageDescriptor)
            }
    }

    private fun buildPropertySpec(
        messageDescriptor: Descriptor,
        fieldDescriptor: FieldDescriptor,
    ): PropertySpec {
        return PropertySpec.builder(fieldDescriptor.javaName + "OrNull", fieldDescriptor.typeName).apply {
            receiver(ClassName(messageDescriptor.javaPackage, messageDescriptor.name))
            getter(
                FunSpec.getterBuilder()
                    .addStatement(
                        "return if (${fieldDescriptor.hasMethodName}()) ${fieldDescriptor.javaName} else null",
                    )
                    .build(),
            )
        }.build()
    }

    private val FieldDescriptor.hasMethodName: String
        get() = "has${name.pascalCase()}"
}
