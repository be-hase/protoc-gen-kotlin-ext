plugins {
    id("conventions.kotlin")
    id("conventions.ktlint")
    id("conventions.detekt")
    id("conventions.maven-publish")
    application
}

description = "A protoc compiler plugin that generates extension code for Kotlin"

dependencies {
    implementation(libs.protobuf.java)
    implementation(libs.kotlinpoet)
}

application {
    mainClass.set("dev.hsbrysk.protoc.gen.GeneratorRunner")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    // Include the runtime class files in the jar (i.e., create a fat jar).
    dependsOn(configurations.runtimeClasspath)
    from(configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) })

    // Specify this because `META-INF/versions/9/module-info.class` and others become duplicates.
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
