import com.google.protobuf.gradle.id

plugins {
    id("conventions.kotlin")
    id("conventions.ktlint")
    id("conventions.detekt")
    alias(libs.plugins.protobuf)
}

description = "Function test module for protoc-gen-kotlin-ext"

dependencies {
    implementation(libs.protobuf.java)
    implementation("com.google.protobuf:protobuf-kotlin:3.25.3")
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
    }
    generateProtoTasks {
        all().forEach { task ->
            task.dependsOn(":protoc-gen-kotlin-ext:installDist")

            task.plugins {
                id("kotlin-ext") {
                    outputSubDir = "kotlin"
                    option("messageOrNullGetter+")
                }
            }
        }
    }
}
