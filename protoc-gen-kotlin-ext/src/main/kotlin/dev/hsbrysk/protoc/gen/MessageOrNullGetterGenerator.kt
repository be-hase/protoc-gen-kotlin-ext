package dev.hsbrysk.protoc.gen

import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType

class MessageOrNullGetterGenerator : AbstractOrNullGetterGenerator() {
    override fun predicate(fieldDescriptor: FieldDescriptor): Boolean = fieldDescriptor.javaType == JavaType.MESSAGE
}
