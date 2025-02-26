package conventions

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    kotlin("jvm")
    `project-report`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
        vendor = JvmVendorSpec.ADOPTIUM
    }
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    compilerOptions {
        javaParameters = true
        allWarningsAsErrors = true
        freeCompilerArgs = listOf(
            "-Xjsr305=strict",
        )
    }
}

// - https://github.com/junit-team/junit5/issues/3474
// - https://youtrack.jetbrains.com/issue/KT-54207/Kotlin-has-two-sources-tasks-kotlinSourcesJar-and-sourcesJar-that-archives-sources-to-the-same-artifact
tasks.named("kotlinSourcesJar") {
    enabled = false
}

val libs = the<LibrariesForLibs>()

dependencies {
    testImplementation(platform(libs.junit.bom))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.assertk)
    testImplementation(libs.mockk.core)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        // Make sure output from standard out or error is shown in Gradle output.
        showExceptions = true
        showCauses = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
