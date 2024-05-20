plugins {
    id("conventions.kotlin")
    id("conventions.ktlint")
    id("conventions.detekt")
    `maven-publish`
    application
    alias(libs.plugins.sonatype.central.upload)
}

description = "A protoc compiler plugin that generates useful extension code for Kotlin"

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

publishing {
    publications {
        create<MavenPublication>("maven") {
            pom {
                name = project.name
                description = project.description
                url = "https://github.com/be-hase/protoc-gen-kotlin-ext"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/license/mit"
                    }
                }
                developers {
                    developer {
                        id = "be-hase"
                        name = "Ryosuke Hasebe"
                        email = "hsb.1014@gmail.com"
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/be-hase/protoc-gen-kotlin-ext.git")
                    developerConnection.set("scm:git:ssh://github.com:be-hase/protoc-gen-kotlin-ext.git")
                    url.set("https://github.com/be-hase/protoc-gen-kotlin-ext")
                }
            }
        }
    }
}

tasks.sonatypeCentralUpload {
    dependsOn(
        tasks.clean,
        tasks.jar,
        tasks.sourcesJar,
        tasks.javadocJar,
        tasks.named("generatePomFileForMavenPublication"),
    )

    username = providers.environmentVariable("MAVEN_CENTRAL_USERNAME").orNull
    password = providers.environmentVariable("MAVEN_CENTRAL_PASSWORD").orNull

    signingKey = providers.environmentVariable("SIGNING_PGP_KEY").orNull
    signingKeyPassphrase = providers.environmentVariable("SIGNING_PGP_PASSWORD").orNull

    archives = (
        tasks.jar.get().outputs.files +
            tasks.sourcesJar.get().outputs.files +
            tasks.javadocJar.get().outputs.files
        )
    pom = file(
        tasks.named("generatePomFileForMavenPublication").get().outputs.files.single(),
    )

    publishingType = "MANUAL"
}
