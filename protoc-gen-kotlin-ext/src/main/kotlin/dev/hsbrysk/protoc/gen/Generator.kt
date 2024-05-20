package dev.hsbrysk.protoc.gen

import com.google.protobuf.Descriptors.FileDescriptor
import com.squareup.kotlinpoet.FileSpec

interface Generator {
    /**
     * Add processing to `FileSpec` based on the `FileDescriptor`.
     */
    fun apply(
        fileSpecBuilder: FileSpec.Builder,
        fileDescriptor: FileDescriptor,
    )
}
