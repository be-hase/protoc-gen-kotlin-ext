package dev.hsbrysk.protoc.gen

import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType

class OrNullGetterGenerator : AbstractOrNullGetterGenerator() {
    override fun predicate(fieldDescriptor: FieldDescriptor): Boolean {
        return fieldDescriptor.javaType != JavaType.MESSAGE
    }
}
