pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.10.1"
        id("org.jetbrains.kotlin.android") version "2.1.21"
        id("org.jetbrains.kotlin.jvm") version "2.1.21"
        id("org.jetbrains.kotlin.plugin.compose") version "2.1.21"
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HPNews"

// Das reine Kotlin-Modul "core" (Nachrichten laden, Text extrahieren, KI-Vereinfachung)
// lässt sich ohne Android SDK bauen und testen: ./gradlew -PcoreOnly :core:test
include(":core")
if (!providers.gradleProperty("coreOnly").isPresent) {
    include(":app")
}
