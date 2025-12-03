// Root build configuration for fks-clients (Kotlin Multiplatform)
plugins {
    kotlin("multiplatform") version "2.0.20" apply false
    kotlin("plugin.serialization") version "2.0.20" apply false
    id("org.jetbrains.compose") version "1.8.0" apply false
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
