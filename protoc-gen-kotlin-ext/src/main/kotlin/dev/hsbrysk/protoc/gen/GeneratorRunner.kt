package dev.hsbrysk.protoc.gen

import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.Feature
import com.squareup.kotlinpoet.FileSpec
import dev.hsbrysk.protoc.gen.util.javaPackage
import dev.hsbrysk.protoc.gen.util.pascalCase
import dev.hsbrysk.protoc.gen.util.path
import dev.hsbrysk.protoc.gen.util.protoFileName

/**
 * Pluginの仕組みはここで解説されています。
 * - https://buf.build/docs/reference/images
 * - https://qiita.com/yugui/items/87d00d77dee159e74886
 */
object GeneratorRunner {
    @JvmStatic
    fun main(args: Array<String>) {
        val generatorRequest = parseGeneratorRequest()

        val fileSpecs = generateFileSpecs(generatorRequest)

        val generatorResponse = CodeGeneratorResponse.newBuilder()
            .setSupportedFeatures(Feature.FEATURE_PROTO3_OPTIONAL_VALUE.toLong())
            .addAllFile(
                fileSpecs.map {
                    CodeGeneratorResponse.File.newBuilder()
                        .setName(it.path.toString())
                        .setContent(it.toString())
                        .build()
                },
            )
            .build()

        writeGeneratorResponse(generatorResponse)
    }

    /**
     * stdinからCodeGeneratorRequestをparse
     * ref: https://github.com/protocolbuffers/protobuf/blob/8f831e973a93a1c204abd27b545622ae1d82cae0/src/google/protobuf/compiler/plugin.proto
     */
    private fun parseGeneratorRequest(): CodeGeneratorRequest {
        return System.`in`.buffered().use { CodeGeneratorRequest.parseFrom(it) }
    }

    /**
     * CodeGeneratorRequestから生成するFileSpec(kotlinpoet)を複数生成する
     */
    private fun generateFileSpecs(request: CodeGeneratorRequest): List<FileSpec> {
        val compileOptions = CompileOption.parseOptions(request)
        val generators = compileOptions.map { it.generator() }

        val fileDescriptorMap = buildFileDescriptorMap(request)

        return request.fileToGenerateList
            .map { fileDescriptorMap.getValue(it) }
            .filter { it.toProto().syntax == "proto3" } // proto3のみ対象
            .map { fileDescriptor ->
                val fileSpecBuilder = FileSpec.builder(
                    // sub packageを使っておく (同名でconflictすることを避けたい)
                    fileDescriptor.javaPackage + ".kotlin.ext",
                    fileDescriptor.protoFileName.pascalCase() + "Extensions",
                )
                generators.forEach { it.apply(fileSpecBuilder, fileDescriptor) }
                fileSpecBuilder.build()
            }
    }

    /**
     * key: proto file, value FileDescriptor
     * ref: https://github.com/grpc/grpc-kotlin/blob/ffdac372efe9a289ec504677cc0e58c9ed9f87f9/compiler/src/main/java/io/grpc/kotlin/generator/protoc/CodeGenerators.kt#L28-L42
     */
    private fun buildFileDescriptorMap(request: CodeGeneratorRequest): Map<String, FileDescriptor> {
        val descriptorsByName = mutableMapOf<String, FileDescriptor>()
        for (protoFile in request.protoFileList) {
            // we should have visited all the dependencies, so they should be present in the map
            val dependencies = protoFile.dependencyList.map(descriptorsByName::getValue)

            // build and link the descriptor for this file to its dependencies
            val fileDescriptor = FileDescriptor.buildFrom(protoFile, dependencies.toTypedArray())

            descriptorsByName[protoFile.name] = fileDescriptor
        }
        return descriptorsByName
    }

    /**
     * stdoutにCodeGeneratorResponseを出力する
     */
    private fun writeGeneratorResponse(generatorResponse: CodeGeneratorResponse) {
        System.out.buffered().use { output ->
            generatorResponse.writeTo(output)
        }
    }
}
