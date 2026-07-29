rootProject.name = "FallingTree"

pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = java.net.URI.create("https://maven.fabricmc.net/")
        }
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
}

val includeFabric: String by settings

include("common")
if (includeFabric.toBoolean()) {
    include("fabric")
}
