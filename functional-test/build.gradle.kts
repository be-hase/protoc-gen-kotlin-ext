import com.google.protobuf.gradle.id

plugins {
    id("conventions.kotlin")
    id("conventions.ktlint")
    id("conventions.detekt")
    alias(libs.plugins.protobuf)
}

description = "Function test module for protoc-gen-kotlin-ext"

val withProtocGenKotlin = providers.gradleProperty("withProtocGenKotlin").isPresent

dependencies {
    implementation(libs.protobuf.java)
    if (withProtocGenKotlin) {
        implementation(libs.protobuf.kotlin)
    }
    implementation(libs.grpc.java.protobuf)
    implementation(libs.grpc.kotlin.stub)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    plugins {
        id("kotlin-ext") {
            path = project(":protoc-gen-kotlin-ext").layout.buildDirectory.asFile.get()
                .resolve("install/protoc-gen-kotlin-ext/bin/protoc-gen-kotlin-ext").path
        }
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpc.java.get()}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${libs.versions.grpc.kotlin.get()}:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.dependsOn(":protoc-gen-kotlin-ext:installDist")

            if (withProtocGenKotlin) {
                task.builtins {
                    id("kotlin")
                }
            }
            task.plugins {
                id("kotlin-ext") {
                    outputSubDir = "kotlin"
                    if (!withProtocGenKotlin) {
                        option("messageOrNullGetter+")
                    }
                }
                // Test to ensure there are no issues when using gen-grpc-java.
                id("grpc") {
                    outputSubDir = "java"
                }
                // Test to ensure there are no issues when using gen-grpc-kotlin.
                id("grpckt") {
                    outputSubDir = "kotlin"
                }
            }
        }
    }
}
