package dev.hsbrysk.protoc.gen

import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Descriptors.FieldDescriptor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import dev.hsbrysk.protoc.gen.util.javaName
import dev.hsbrysk.protoc.gen.util.javaPackage
import dev.hsbrysk.protoc.gen.util.pascalCase
import dev.hsbrysk.protoc.gen.util.typeName

class FactoryGenerator : Generator {
    override fun apply(
        fileSpecBuilder: FileSpec.Builder,
        fileDescriptor: Descriptors.FileDescriptor,
    ) {
        fileDescriptor.messageTypes.forEach { messageDescriptor ->
            applyRecursively(FileSpecBuilder(fileSpecBuilder), messageDescriptor)
        }
    }

    // Process recursively, including nested types.
    private fun applyRecursively(
        builder: SpecBuilder,
        messageDescriptor: Descriptor,
    ) {
        builder.addFunction(buildFactoryFunSpec(messageDescriptor))

        val nestedTypes = messageDescriptor.nestedTypes
            // Maps are internally defined as nested types, so exclude them.
            // ref: https://protobuf.com/docs/language-spec#maps
            .filterNot { it.options.mapEntry }
        if (nestedTypes.isNotEmpty()) {
            // Nested types are defined under the `{parent}Extensions` object.
            // Otherwise, functions with the same name are generated, causing compilation errors.
            // (Technically, it would be fine as long as the signatures don't match, but just to be safe)
            val objectSpecBuilder = TypeSpec.objectBuilder(messageDescriptor.name + "Extensions")
            nestedTypes.forEach { nestedMessageDescriptor ->
                applyRecursively(ObjectSpecBuilder(objectSpecBuilder), nestedMessageDescriptor)
            }
            builder.addType(objectSpecBuilder.build())
        }
    }

    private fun buildFactoryFunSpec(messageDescriptor: Descriptor): FunSpec {
        val className = ClassName(messageDescriptor.javaPackage, messageDescriptor.name)

        return FunSpec.builder(messageDescriptor.name).apply {
            messageDescriptor.fields.forEach { fieldDescriptor ->
                addParameter(fieldDescriptor.javaName, fieldDescriptor.typeName)
            }
            returns(className)

            // To avoid variable name collisions, a `_` prefix is intentionally added.
            beginControlFlow("val _builder = %T.newBuilder().apply", className)
            messageDescriptor.fields.forEach { fieldDescriptor ->
                if (fieldDescriptor.typeName.isNullable) {
                    addStatement("%N?.let·{ ${fieldDescriptor.builderMethodName}(it) }", fieldDescriptor.javaName)
                } else {
                    addStatement("${fieldDescriptor.builderMethodName}(%N)", fieldDescriptor.javaName)
                }
            }
            endControlFlow()
            addStatement("return _builder.build()")
        }.build()
    }

    private val FieldDescriptor.builderMethodName: String
        get() {
            return if (name == "class") {
                when {
                    isMapField -> "putAllClass_"
                    isRepeated -> "addAllClass_"
                    else -> "setClass_"
                }
            } else {
                when {
                    isMapField -> "putAll${name.pascalCase()}"
                    isRepeated -> "addAll${name.pascalCase()}"
                    else -> "set${name.pascalCase()}"
                }
            }
        }

    // Create an interface to handle `FileSpec.Builder` and `TypeSpec.Builder` transparently in `applyRecursively`.
    private interface SpecBuilder {
        fun addFunction(funSpec: FunSpec)
        fun addType(typeSpec: TypeSpec)
    }

    private class FileSpecBuilder(private val delegate: FileSpec.Builder) : SpecBuilder {
        override fun addFunction(funSpec: FunSpec) {
            delegate.addFunction(funSpec)
        }

        override fun addType(typeSpec: TypeSpec) {
            delegate.addType(typeSpec)
        }
    }

    private class ObjectSpecBuilder(private val delegate: TypeSpec.Builder) : SpecBuilder {
        override fun addFunction(funSpec: FunSpec) {
            delegate.addFunction(funSpec)
        }

        override fun addType(typeSpec: TypeSpec) {
            delegate.addType(typeSpec)
        }
    }
}
