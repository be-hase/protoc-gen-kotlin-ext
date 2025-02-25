allprojects {
    group = "dev.hsbrysk"

    val defaultVersion = "latest-SNAPSHOT"
    version = providers.gradleProperty("publishVersion").orNull
        ?: providers.environmentVariable("PUBLISH_VERSION").orNull
            ?: defaultVersion
}
