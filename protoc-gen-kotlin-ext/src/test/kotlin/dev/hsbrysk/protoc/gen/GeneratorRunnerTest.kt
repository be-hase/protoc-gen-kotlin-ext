package dev.hsbrysk.protoc.gen

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.DescriptorProtos.FileOptions
import com.google.protobuf.DescriptorProtos.OneofDescriptorProto
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.Feature
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class GeneratorRunnerTest {
    @Test
    fun main() {
        val response = runMain(request(""))

        assertThat(response.supportedFeatures).isEqualTo(Feature.FEATURE_PROTO3_OPTIONAL_VALUE.toLong())
        // The proto2 file is skipped, so only the proto3 file is generated.
        assertThat(response.fileList.map { it.name }).isEqualTo(listOf("com/example/PersonKtExtensions.kt"))

        val content = response.getFile(0).content
        assertThat(content).contains("public fun Person(firstName: String, nickname: String?): Person")
        assertThat(content).contains("nicknameOrNull")
    }

    @Test
    fun `main compile options are applied`() {
        val response = runMain(request("factory-"))

        val content = response.getFile(0).content
        assertThat(content).doesNotContain("public fun Person(")
        assertThat(content).contains("nicknameOrNull")
    }

    private fun request(parameter: String): CodeGeneratorRequest = CodeGeneratorRequest.newBuilder()
        .setParameter(parameter)
        .addAllFileToGenerate(listOf("person.proto", "legacy.proto"))
        .addProtoFile(PERSON_PROTO)
        .addProtoFile(LEGACY_PROTO)
        .build()

    private fun runMain(request: CodeGeneratorRequest): CodeGeneratorResponse {
        val originalIn = System.`in`
        val originalOut = System.out
        val stdout = ByteArrayOutputStream()
        try {
            System.setIn(ByteArrayInputStream(request.toByteArray()))
            System.setOut(PrintStream(stdout))
            GeneratorRunner.main(arrayOf())
        } finally {
            System.setIn(originalIn)
            System.setOut(originalOut)
        }
        return CodeGeneratorResponse.parseFrom(stdout.toByteArray())
    }

    companion object {
        private val PERSON_PROTO: FileDescriptorProto = FileDescriptorProto.newBuilder()
            .setName("person.proto")
            .setSyntax("proto3")
            .setPackage("com.example")
            .setOptions(FileOptions.newBuilder().setJavaMultipleFiles(true))
            .addMessageType(
                DescriptorProto.newBuilder()
                    .setName("Person")
                    .addField(
                        FieldDescriptorProto.newBuilder()
                            .setName("first_name")
                            .setNumber(1)
                            .setType(FieldDescriptorProto.Type.TYPE_STRING)
                            .setLabel(FieldDescriptorProto.Label.LABEL_OPTIONAL),
                    )
                    // `optional` in proto3 is represented as a synthetic oneof.
                    .addOneofDecl(OneofDescriptorProto.newBuilder().setName("_nickname"))
                    .addField(
                        FieldDescriptorProto.newBuilder()
                            .setName("nickname")
                            .setNumber(2)
                            .setType(FieldDescriptorProto.Type.TYPE_STRING)
                            .setLabel(FieldDescriptorProto.Label.LABEL_OPTIONAL)
                            .setProto3Optional(true)
                            .setOneofIndex(0),
                    ),
            )
            .build()

        private val LEGACY_PROTO: FileDescriptorProto = FileDescriptorProto.newBuilder()
            .setName("legacy.proto")
            .setSyntax("proto2")
            .setPackage("com.example.legacy")
            .addMessageType(
                DescriptorProto.newBuilder()
                    .setName("LegacyMessage")
                    .addField(
                        FieldDescriptorProto.newBuilder()
                            .setName("name")
                            .setNumber(1)
                            .setType(FieldDescriptorProto.Type.TYPE_STRING)
                            .setLabel(FieldDescriptorProto.Label.LABEL_OPTIONAL),
                    ),
            )
            .build()
    }
}
